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

import com.github.chitralverma.schnapps.internal.Logging
import com.github.fppt.jedismock.RedisServer
import org.redisson.Redisson
import org.redisson.client.codec.StringCodec
import org.redisson.config.Config

object EmbeddedRedisServer extends Logging {
  private var _instance: RedisServer = _

  def start(): Unit = {
    if (_instance == null) {
      logInfo("Starting Embedded Redis Server.")
      _instance = RedisServer.newRedisServer(1579)
      _instance.start()

      putDataInServer()
    } else {
      logWarning("Embedded Redis Server is already running.")
    }
  }

  def putDataInServer(): Unit = {
    val config: Config = new Config()
    config.setCodec(new StringCodec())
    config
      .useSingleServer()
      .setConnectionMinimumIdleSize(1)
      .setTimeout(1000)
      .setAddress("redis://127.0.0.1:1579")

    val redisClient = Redisson.create(config)
    redisClient.getBucket("abc").set("1")
    redisClient.getBucket("xyz").set("2")

    redisClient.shutdown()
  }

}
