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

package com.github.chitralverma.schnapps.internal

import java.util.UUID

import scala.collection.concurrent.TrieMap
import scala.reflect.ClassTag

/**
 * Variables that need to be shared among workers maybe held here as local singletons.
 * Utility that helps maintain state of un serializable objects in Spark closures across tasks.
 *
 * An example of such objects can be connection pool, a client, a config etc.
 */
class Singleton[T: ClassTag](constructor: => T) extends Serializable {

  val singletonUUID: String = UUID.randomUUID().toString

  @transient private lazy val instance: T = {
    Singleton.objectPool.synchronized {
      val singletonOption: Option[Any] = Singleton.objectPool.get(singletonUUID)
      if (singletonOption.isEmpty) {
        Singleton.objectPool.put(singletonUUID, constructor)
      }
    }
    Singleton.objectPool(singletonUUID).asInstanceOf[T]
  }

  def get: T = instance

}

object Singleton {
  private val objectPool = new TrieMap[String, Any]()

  def apply[T: ClassTag](constructor: => T): Singleton[T] =
    new Singleton[T](constructor)
  def poolSize: Int = objectPool.size
  def poolClear(): Unit = objectPool.clear()
}
