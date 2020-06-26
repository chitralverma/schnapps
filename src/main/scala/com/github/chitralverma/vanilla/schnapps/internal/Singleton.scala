package com.github.chitralverma.vanilla.schnapps.internal

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
      val singletonOption = Singleton.objectPool.get(singletonUUID)
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
