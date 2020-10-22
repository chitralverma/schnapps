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

package com.github.chitralverma.schnapps.extras.externals.redis

import com.github.chitralverma.schnapps.TestBase
import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.fppt.jedismock.RedisServer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RedisExternalTestSuite extends TestBase {
  private var redisServer: RedisServer = _
  private var redisExternal: RedisExternal = _
  final private val RedisServerPort = 1379

  private val config: ExternalConfigModel = ExternalConfigModel(
    name = "test_conn",
    `type` = "redis",
    configs = Map(
      "codec" -> Map("class" -> "org.redisson.client.codec.StringCodec"),
      "singleServerConfig" -> Map(
        "address" -> s"redis://127.0.0.1:$RedisServerPort",
        "connectionMinimumIdleSize" -> 1)))

  override def beforeAll(): Unit = {
    redisServer = new RedisServer(RedisServerPort)
    redisServer.start()

    redisExternal = new RedisExternal()
    redisExternal.initialise(config)
  }

  override def afterAll(): Unit = {
    if (redisExternal.isConnected) redisExternal.disconnect()
    redisServer.stop()
  }

  test("isConnected()") {
    assert(redisExternal.isConnected)
  }

}
