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

package com.github.chitralverma.schnapps.examples.utils

import java.sql.{Connection, DriverManager, Statement}

import com.github.chitralverma.schnapps.internal.Logging
import org.hsqldb.persist.HsqlProperties
import org.hsqldb.server.Server

import scala.util._

object EmbeddedDB extends Logging {

  private var _instance: Server = _

  def start(config: Map[String, String]): Unit = {
    if (_instance == null) {
      _instance = {
        val props: HsqlProperties = new HsqlProperties()
        props.setProperty("server.database.0", config("jdbcUrl"))

        val sonicServer: Server = new Server()
        sonicServer.setProperties(props)

        sonicServer
      }

      logInfo("Starting Embedded DB.")
      _instance.start()
      createTestTable(config("jdbcUrl"))
    } else {
      logWarning("Embedded DB already running.")
    }
  }

  def createTestTable(jdbcUrl: String): Unit = {
    var con: Connection = null
    var stmt: Statement = null
    var result: Int = 0

    Try {
      // scalastyle:off
      Class.forName("org.hsqldb.jdbc.JDBCDriver")
      // scalastyle:on
      con = DriverManager.getConnection(jdbcUrl, "SA", "")
      stmt = con.createStatement()

      result = stmt.executeUpdate("""CREATE TABLE IF NOT EXISTS test_tbl (
          |id INT NOT NULL,
          |title VARCHAR(50) NOT NULL,
          |author VARCHAR(20) NOT NULL,
          |submission_date DATE,
          |PRIMARY KEY (id))""".stripMargin)

    } match {
      case Success(_) => logInfo("Test table created successfully.");
      case Failure(ex) => logError("Failure while creating test table", ex)
    }

  }

  def stop(): Unit = {
    if (!_instance.isNotRunning) {
      logInfo("Stopping Embedded DB.")
      _instance.stop()
    }
  }

}
