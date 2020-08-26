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

import java.sql.Connection
import java.util.Properties

import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.chitralverma.schnapps.extras.externals.External
import com.github.chitralverma.schnapps.utils.Utils
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import com.zaxxer.hikari.util.PropertyElf

import scala.collection.JavaConverters._

class JDBCExternal extends External {

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
    properties.putAll(ec.configs.asJava)

    Utils.withTry(
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

}
