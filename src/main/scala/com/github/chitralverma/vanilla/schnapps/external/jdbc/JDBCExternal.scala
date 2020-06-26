package com.github.chitralverma.vanilla.schnapps.external.jdbc

import java.sql.Connection
import java.util.Properties

import scala.collection.JavaConverters._

import com.github.chitralverma.vanilla.schnapps.config.models.ExternalConfig
import com.github.chitralverma.vanilla.schnapps.internal.{External, ExternalMarker}
import com.github.chitralverma.vanilla.schnapps.utils.Utils
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import com.zaxxer.hikari.util.PropertyElf

@ExternalMarker(tpe = JDBCExternal.Jdbc)
case class JDBCExternal(ec: ExternalConfig) extends External(ec) {

  import com.github.chitralverma.vanilla.schnapps.internal.Singleton

  override type T = HikariDataSource
  override type E = Connection

  private final def supportedProperties: String =
    PropertyElf.getPropertyNames(classOf[HikariConfig]).asScala.mkString("[", ", ", "]")

  private def createHikariConfig(): HikariConfig = {
    assert(ec.configs != null, "Provided config is 'null'")
    assert(
      ec.configs.nonEmpty,
      s"Provided config is empty. Supported properties:\n $supportedProperties")

    val properties = new Properties()
    properties.putAll(ec.configs.asJava)

    Utils.withTry(
      new HikariConfig(properties),
      s"Illegal Argument: Supported properties:\n $supportedProperties")
  }

  override def connect(): Singleton[HikariDataSource] = {
    val source = new HikariDataSource(createHikariConfig())
    assert(source.isRunning, "Connection initialised but not running")

    Singleton(source)
  }

  override def disconnect(): Unit = getAs[T].close()

  override def executeThis[O](f: Connection => O): O = {
    val connection = getAs[T].getConnection
    val result = f(connection)
    connection.close()

    result
  }
}

object JDBCExternal {

  final val Jdbc = "jdbc"
}
