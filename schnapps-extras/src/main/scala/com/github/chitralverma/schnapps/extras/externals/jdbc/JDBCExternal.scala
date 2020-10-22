/*
 *    Copyright 2020 Chitral Verma
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.chitralverma.schnapps.extras.externals.jdbc

import java.sql._
import java.util.Properties

import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.chitralverma.schnapps.extras.externals.External
import com.github.chitralverma.schnapps.extras.externals.jdbc.DatabaseUtils._
import com.github.chitralverma.schnapps.extras.orm.Column
import com.github.chitralverma.schnapps.internal.Logging
import com.github.chitralverma.schnapps.utils.Utils._
import com.zaxxer.hikari.util.PropertyElf
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.json4s.jackson.Serialization
import org.json4s.{Formats, _}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.io.{BufferedSource, Source}
import scala.reflect.runtime.universe._

class JDBCExternal extends Logging with External {
  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  override val tpe: String = "jdbc"

  override type T = HikariDataSource
  override type E = Connection

  private final def supportedProperties: String =
    PropertyElf.getPropertyNames(classOf[HikariConfig]).asScala.mkString("[", ", ", "]")

  private def createHikariConfig(ec: ExternalConfigModel): HikariConfig = {
    assert(ec.configs != null, "Provided config is 'null'")
    assert(
      ec.configs.nonEmpty,
      s"Provided config is empty. Supported properties:\n $supportedProperties")

    val properties = new Properties()
    val (stringOnlyConfigs, otherConfigs) = ec.configs.partition(_._2.isInstanceOf[String])
    logWarning(
      s"Only String type values supported. " +
        s"Omitting configs with keys [${otherConfigs.mkString("'", ", ", "'")}].")

    properties.putAll(stringOnlyConfigs.mapValues(_.toString).asJava)
    withTry(
      new HikariConfig(properties),
      s"Illegal Argument: Supported properties:\n $supportedProperties")
  }

  override def connect(externalConfigModel: ExternalConfigModel): T = {
    val source = new HikariDataSource(createHikariConfig(externalConfigModel))
    assert(!source.isClosed, "Connection initialised but not running")

    source
  }

  override def disconnect(): Unit = getImpl.close()

  override def executeThis[O](f: Connection => O): O = {
    val connection: Connection = getImpl.getConnection
    val result: O = f(connection)
    connection.close()

    result
  }

  def readAs[T](query: String, colToFieldMapping: Map[String, String] = Map.empty)(
      implicit mf: scala.reflect.Manifest[T]): Seq[Option[T]] =
    withTry({
      executeThis(con => {
        {
          logInfo(s"JDBCExternal [readAs]: `$query` was executed.")
          val pst: PreparedStatement = con.prepareStatement(query)
          val rs: ResultSet = pst.executeQuery()

          val rsmd: ResultSetMetaData = rs.getMetaData
          val numColumns: Int = rsmd.getColumnCount

          val serializedNames: Map[String, String] =
            if (colToFieldMapping.isEmpty) getColumnFieldMapping[T, Column]
            else colToFieldMapping

          @tailrec
          def go(rs: ResultSet, rows: Seq[JValue]): Seq[JValue] = {
            if (rs.next()) {
              import scala.collection.immutable
              val rowElements: immutable.IndexedSeq[JValue] = for (i <- 1 to numColumns) yield {
                val columnName: String = rsmd.getColumnName(i)
                encodeAsJson(
                  serializedNames.getOrElse(columnName, columnName),
                  rs.getObject(columnName),
                  formats)
              }

              go(rs, rows :+ rowElements.reduce(_ merge _))
            } else rows
          }

          val res: Seq[JValue] = go(rs, Nil)

          val r: Seq[Option[T]] = if (typeOf[T] =:= typeOf[String]) {
            res.map(
              x =>
                jsonOption(x)
                  .map(
                    _.extract[Map[String, Any]]
                      .map(kv => s"${kv._1}=${kv._2}")
                      .mkString(", ")
                      .asInstanceOf[T]))

          } else {
            res.map(x => {
              jsonOption(x).map(_.extract[T])
            })
          }

          r
        }
      })
    })

  def insertFrom[T](
      tableName: String,
      obj: T,
      fieldToColMapping: Map[String, String] = Map.empty)(
      implicit mf: scala.reflect.Manifest[T]): Unit =
    withTry({
      executeThis(con => {
        val serializedNames: Map[String, String] =
          if (fieldToColMapping.isEmpty) getFieldColumnMapping[T, Column]
          else fieldToColMapping

        val fields: List[String] = classAccessors[T]
        val (cols, values) = obj.getClass.getDeclaredFields
          .filter(f => fields.contains(f.getName))
          .map(v => {
            v.setAccessible(true)
            (serializedNames.getOrElse(v.getName, v.getName), v.get(obj))
          })
          .unzip

        val query: String = s"INSERT INTO $tableName (${cols.mkString(", ")}) " +
          s"VALUES (${Seq.fill(cols.length)("?").mkString(", ")})"

        val pst: PreparedStatement = con.prepareStatement(query)

        for (i <- 1 to values.length)
          pst.setObject(i, values(i - 1))

        logInfo(s"JDBCExternal [insertFrom]: `$query` was executed.")
        pst.executeUpdate()
      })
    })

  def insertBatchFrom[T](
      tableName: String,
      obj: Seq[T],
      setAutoCommit: Boolean = false,
      numBatches: Int = Int.MaxValue,
      fieldToColMapping: Map[String, String] = Map.empty)(
      implicit mf: scala.reflect.Manifest[T]): Unit =
    withTry({
      executeThis(con => {
        val serializedNames: Map[String, String] =
          if (fieldToColMapping.isEmpty) getFieldColumnMapping[T, Column]
          else fieldToColMapping

        require(obj.nonEmpty, "At least 1 obj is required")

        val fields: List[String] = classAccessors[T]

        con.setAutoCommit(setAutoCommit)

        val colsAndVals: Seq[(scala.Array[String], scala.Array[AnyRef])] = obj.map(
          obj =>
            obj.getClass.getDeclaredFields
              .filter(f => fields.contains(f.getName))
              .map(v => {
                v.setAccessible(true)
                (serializedNames.getOrElse(v.getName, v.getName), v.get(obj))
              })
              .unzip)

        val cols: scala.Array[String] = colsAndVals.head._1
        val query: String = s"INSERT INTO $tableName (${cols.mkString(", ")}) " +
          s"VALUES (${scala.Array.fill(cols.length)("?").mkString(", ")})"

        val pst: PreparedStatement = con.prepareStatement(query)

        assert(numBatches > 0, "Number of batches must by greater than 0")
        colsAndVals
          .grouped(numBatches)
          .foreach(g => {
            g.foreach {
              case (_, values: scala.Array[AnyRef]) =>
                for (i <- 1 to values.length)
                  pst.setObject(i, values(i - 1))

                pst.addBatch()
            }

            logInfo(s"JDBCExternal [insertBatchFrom]: `$query` was executed.")
            pst.executeBatch()
          })

        con.setAutoCommit(true)
      })
    })

  def updateFrom[T](
      tableName: String,
      obj: T,
      whereCondition: Option[String] = None,
      fieldToColMapping: Map[String, String] = Map.empty)(
      implicit mf: scala.reflect.Manifest[T]): Unit =
    withTry({
      executeThis(con => {
        val serializedNames: Map[String, String] =
          if (fieldToColMapping.isEmpty) getFieldColumnMapping[T, Column]
          else fieldToColMapping

        val fields: List[String] = classAccessors[T]
        val (cols, values) = obj.getClass.getDeclaredFields
          .filter(f => fields.contains(f.getName))
          .map(v => {
            v.setAccessible(true)
            (serializedNames.getOrElse(v.getName, v.getName), v.get(obj))
          })
          .unzip

        val condition: String =
          if (whereCondition.isDefined) s" WHERE ${whereCondition.get}" else ""
        val query =
          s"UPDATE $tableName SET ${cols.map(x => s"$x=?").mkString(", ")} $condition"

        val pst: PreparedStatement = con.prepareStatement(query)

        for (i <- 1 to values.length)
          pst.setObject(i, values(i - 1))

        logInfo(s"JDBCExternal [updateFrom]: `$query` was executed.")
        pst.executeUpdate()
      })

    })

  def updateFromQuery(queries: String*): Unit =
    withTry({
      executeThis(con => {
        if (queries.size == 1) {
          val pst: PreparedStatement = con.prepareStatement(queries.head)
          logInfo(s"JDBCExternal [updateFromQuery]: `${queries.head}` was executed.")

          pst.executeUpdate()
        } else if (queries.size > 1) {
          con.setAutoCommit(false)
          logInfo(s"JDBCExternal [updateFromQuery]: Transaction block started.")
          queries.foreach(query => {
            logInfo(s"JDBCExternal [updateFromQuery]: `$query` was executed.")

            val pst: PreparedStatement = con.prepareStatement(query)
            pst.executeUpdate()
          })

          logInfo(s"JDBCExternal [updateFromQuery]: Transaction block ended.")
          con.commit()
        }
      })
    })

  def executeFromScript(filePath: String): Unit =
    withTry({
      executeThis(con => {
        val bufferedSource: BufferedSource = Source.fromFile(filePath)
        val stmnts: scala.Array[String] = bufferedSource.mkString.split("(;(\r)?\n)|(--\n)")
        stmnts.foreach(query => {
          if (query.trim.length > 0) {
            val pst: PreparedStatement = con.prepareStatement(query)
            pst.executeUpdate()
          }
        })
      })
    })

  def executeFromQuery(queries: String*): Unit =
    withTry({
      executeThis(con => {
        if (queries.size == 1) {
          val pst: PreparedStatement = con.prepareStatement(queries.head)
          logInfo(s"JDBCExternal [executeFromQuery]: `${queries.head}` was executed.")

          pst.executeQuery()
        } else if (queries.size > 1) {
          con.setAutoCommit(false)
          logInfo(s"JDBCExternal [executeFromQuery]: Transaction block started.")
          queries.foreach(query => {
            logInfo(s"JDBCExternal [executeFromQuery]: `$query` was executed.")

            val pst: PreparedStatement = con.prepareStatement(query)
            pst.executeQuery()
          })

          logInfo(s"JDBCExternal [executeFromQuery]: Transaction block ended.")
          con.commit()
        }
      })
    })

}
