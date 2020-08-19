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

import com.github.chitralverma.schnapps.config.models.ExternalConfigModel

import scala.util.{Failure, Success, Try}

abstract class External(config: ExternalConfigModel) extends Logging {

  type T
  type E

  val name: String = config.name

  final private val _instance: Singleton[T] = Try(connect()) match {
    case Success(i) => i
    case Failure(e) =>
      logger.error("Error occurred while connecting to External", e)
      throw e
  }

  protected def connect(): Singleton[T]
  def disconnect(): Unit // todo use this
  def executeThis[O](f: E => O): O
  def as[A]: A = this.asInstanceOf[A]
  def getAs[T]: T = _instance.get.asInstanceOf[T]

}
