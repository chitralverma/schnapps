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

import com.github.chitralverma.schnapps.TestBase
import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.chitralverma.schnapps.extras.orm.Column
import com.github.chitralverma.schnapps.utils.Utils._
import org.json4s._
import org.json4s.jackson.Serialization
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JDBCExternalTestSuite extends TestBase {

  private var jDBCExternal: JDBCExternal = _

  private val config: ExternalConfigModel = ExternalConfigModel(
    name = "test_conn",
    `type` = "jdbc",
    configs = Map(
      "jdbcUrl"
        -> s"jdbc:hsqldb:file:target/testdb;shutdown=true;hsqldb.lock_file=false",
      "username" -> "SA",
      "password" -> "",
      "driverClassName" -> "org.hsqldb.jdbc.JDBCDriver",
      "connectionTestQuery" -> "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"))

  private def clearDBs(): Unit = {
    jDBCExternal.updateFromQuery("TRUNCATE SCHEMA PUBLIC AND COMMIT")
    jDBCExternal.updateFromQuery("drop table test_table1 if exists")
  }

  override def beforeAll(): Unit = {
    jDBCExternal = new JDBCExternal()
    jDBCExternal.initialise(config)
    clearDBs()
    jDBCExternal.updateFromQuery("create table test_table1(name varchar(10), age int)")
  }

  override def afterAll(): Unit = {
    clearDBs()
    if (jDBCExternal.getImpl.isRunning) jDBCExternal.disconnect()
  }

  ignore("executeThis()") {
    // todo
  }

  ignore("disconnect()") {
    // todo
  }

  ignore("connect()") {
    // todo
  }

  ignore("createHikariConfig()") {
    // todo
  }

  test("insertFrom()") {
    val row: TestTable1 = TestTable1("x", 5)
    jDBCExternal
      .insertFrom[TestTable1]("test_table1", row, getColumnFieldMapping[TestTable1, Column])

    val res1: Seq[Option[TestTable1]] =
      jDBCExternal.readAs[TestTable1](
        "select * from test_table1",
        getColumnFieldMapping[TestTable1, Column])

    assert(res1.size == 1)
    assert(res1.head.isDefined)
    assert(res1.head.get.age === row.age)
    assert(res1.head.get.name === row.name)

    jDBCExternal.insertFrom[TestTable1]("test_table1", TestTable1("y", 16))
    val res2: Seq[Option[TestTable1]] =
      jDBCExternal.readAs[TestTable1](
        "select * from test_table1",
        getColumnFieldMapping[TestTable1, Column])

    assert(res2.size == 2)
  }

  test("updateFromQuery()") {
    jDBCExternal.updateFromQuery(
      "DELETE FROM test_table1 WHERE name = 'y'",
      "DELETE FROM test_table1 WHERE age = 16")
  }

  test("readAs()") {
    val q: Seq[Option[String]] = jDBCExternal.readAs[String](
      "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
        "WHERE TABLE_NAME='ADMINISTRABLE_ROLE_AUTHORIZATIONS'")

    assert(q.size === 1)
    assert(q.head.get === "TABLE_NAME=ADMINISTRABLE_ROLE_AUTHORIZATIONS")
  }

  test("executeFromQuery()") {
    jDBCExternal.executeFromQuery("SELECT COUNT(1) from INFORMATION_SCHEMA.SYSTEM_SCHEMAS")
    jDBCExternal.executeFromQuery(
      "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_SCHEMAS",
      "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES")
  }

  test("updateFrom()") {
    val row: TestTable1 = TestTable1("x", 10)
    jDBCExternal.updateFrom[TestTable1](
      "test_table1",
      row,
      fieldToColMapping = getFieldColumnMapping[TestTable1, Column])

    val res: Seq[Option[TestTable1]] =
      jDBCExternal.readAs[TestTable1]("select * from test_table1")

    assert(res.size == 1)
    assert(res.head.isDefined)
    assert(res.head.get.age === row.age)
    assert(res.head.get.name === row.name)

    jDBCExternal.updateFrom[TestTable1]("test_table1", row.copy(age = 20), Some("name='x'"))

    val res2: Seq[Option[TestTable1]] =
      jDBCExternal.readAs[TestTable1]("select * from test_table1")
    assert(res2.head.get.age === 20)
  }

  test("insertBatchFrom()") {
    jDBCExternal.updateFromQuery("TRUNCATE TABLE test_table1")

    val c: String = jDBCExternal
      .readAs[String]("select count(1) as count from test_table1")
      .head
      .get
      .split("=")(1)
    assert(c.toInt === 0)

    val b: Seq[TestTable1] =
      Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 0).map(x => TestTable1(generateRandomId(3), x))

    jDBCExternal.insertBatchFrom[TestTable1]("test_table1", b)

    val c2: String = jDBCExternal
      .readAs[String]("select count(1) as count from test_table1")
      .head
      .get
      .split("=")(1)
    assert(c2.toInt === b.size)

    jDBCExternal.updateFromQuery("TRUNCATE TABLE test_table1")

    jDBCExternal.insertBatchFrom[TestTable1](
      "test_table1",
      b,
      fieldToColMapping = getFieldColumnMapping[TestTable1, Column])
    val c3: String = jDBCExternal
      .readAs[String]("select count(1) as count from test_table1")
      .head
      .get
      .split("=")(1)
    assert(c3.toInt === b.size)

  }

  test("encodeAsJson()") {
    implicit val formats: Formats = Serialization.formats(NoTypeHints)

    val jv1: Any = DatabaseUtils.encodeAsJson("name", "x", formats)
    assert(jv1 === JObject(("name", JString("x"))))

    val jv2: Any = DatabaseUtils.encodeAsJson("name", null, formats)
    assert(jv2 === JNothing)
  }

}

case class TestTable1(@Column(name = "NAME") name: String, @Column(name = "AGE") age: Int)
