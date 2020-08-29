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

package com.github.chitralverma.schnapps.extras.externals

import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.chitralverma.schnapps.internal.Logging

import scala.util.{Failure, Success, Try}

trait External extends Logging {

  type T
  type E

  private var _isInit: Boolean = false
  private var _config: ExternalConfigModel = _
  private var _name: String = _
  val tpe: String

  final private var _instance: T = _

  def executeThis[O](f: E => O): O

  def initialise(externalConfigModel: ExternalConfigModel): Unit = {
    if (!_isInit) {
      Try {
        val instance: T = connect(externalConfigModel)

        setName(externalConfigModel.name)
        setConfig(externalConfigModel)
        setImpl(instance)
      } match {
        case Failure(exception) =>
          logError(
            s"Exception occurred while initialising external" +
              s" with name '${externalConfigModel.name}'.")
          throw new IllegalStateException(exception)
        case Success(_) => setInit()
      }
    }
  }

  protected def connect(externalConfigModel: ExternalConfigModel): T
  def disconnect(): Unit // todo use this

  // Setters

  final private def setInit(): Unit = {
    if (!_isInit) {
      _isInit = true
    } else {
      logWarning("External already initialised.")
    }
  }

  final private def setName(name: String): Unit = {
    if (_name == null) {
      _name = name
    } else {
      logWarning("Name is already set for External.")
    }
  }

  final private def setConfig(config: ExternalConfigModel): Unit = {
    if (_config == null) {
      _config = config
    } else {
      logWarning("Config is already set for external.")
    }
  }

  final private def setImpl(instance: T): Unit = {
    if (_instance == null) {
      _instance = instance
    } else {
      logWarning("Instance is already initialised and set for External.")
    }
  }

  // Getters

  final def getName: String = {
    val name: Option[String] = Option(_name)
    assert(
      name.isDefined,
      "Unable to get the name of External as it hasn't been initialised and set yet.")

    name.get
  }

  final def getConfig: ExternalConfigModel = {
    val config: Option[ExternalConfigModel] = Option(_config)
    assert(
      config.isDefined,
      "Unable to get config of External as it hasn't been initialised and set yet.")

    config.get
  }

  final def getImpl: T = {
    val instance: Option[T] = Option(_instance)
    assert(
      instance.isDefined,
      "Unable to get impl instance of External as it hasn't been initialised and set yet.")

    instance.get
  }

  def as[A]: A = this.asInstanceOf[A]

}
