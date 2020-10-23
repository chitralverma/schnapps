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

import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.chitralverma.schnapps.extras.externals.External
import com.github.chitralverma.schnapps.internal.Logging
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.api.redisnode.RedisNodes
import org.redisson.config.Config

/**
 * Redis External creates a pool of connections to a Redis instance.
 * Responsible for initiating clients to either Redis `StandAlone` or `Cluster`
 * */
class RedisExternal extends Logging with External {

  override type T = RedissonClient
  override type E = RedissonClient

  override val tpe: String = "redis"

  override protected def connect(externalConfigModel: ExternalConfigModel): T = {
    Redisson.create(Config.fromYAML(externalConfigModel.configAsYamlStr))
  }

  override def executeThis[O](f: E => O): O = f(getImpl)

  override def disconnect(): Unit = getImpl.shutdown()

  /**
   * Checks if a successful connection to Redis exists.
   */
  def isConnected: Boolean = {
    val conn = getImpl
    val config = conn.getConfig

    if (config.isClusterConfig) {
      conn.getRedisNodes(RedisNodes.CLUSTER).pingAll()
    } else if (config.isSentinelConfig) {
      conn.getRedisNodes(RedisNodes.SENTINEL_MASTER_SLAVE).pingAll()
    } else {
      conn.getRedisNodes(RedisNodes.SINGLE).pingAll()
    }
  }
}
