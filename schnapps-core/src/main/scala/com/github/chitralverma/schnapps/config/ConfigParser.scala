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

package com.github.chitralverma.schnapps.config

import java.io.File

import com.github.chitralverma.schnapps.enums._
import com.github.chitralverma.schnapps.internal.Logging
import com.github.chitralverma.schnapps.utils.Utils.withTry
import org.json4s.{file2JsonInput, DefaultFormats, Formats}
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.JsonMethods.{parse => jsonParse}

object ConfigParser extends Logging {

  import com.github.chitralverma.schnapps.enums.HTTPMethodsEnums

  implicit var jsonFormat: Formats = DefaultFormats +
    new EnumNameSerializer(ProtocolEnums) +
    new EnumNameSerializer(HTTPMethodsEnums) +
    new EnumNameSerializer(SerializationEnums)

  private var _instance: Configuration = _

  def parse(args: Array[String]): Configuration = {
//    Utils.printLogo()
    if (_instance == null) {
      _instance = {

        val jobConfig: Configuration = if (args.headOption.isDefined) {
          val configPath: String = args(0)
          logInfo(s"Attempting to load configuration from path: '$configPath'")
          withTry(
            jsonParse(file2JsonInput(new File(configPath))).extract[Configuration],
            s"Fatal Error: Unable to initialize configuration from config at path: '$configPath'")
        } else {
          throw new IllegalStateException(
            "Path to json configuration was not provided in arguments")
        }

        jobConfig
      }
    } else {
      logWarning("Configuration is already initialised.")
    }

    _instance
  }

  def getConfiguration: Configuration = {
    val instance: Option[Configuration] = Option(_instance)
    assert(
      instance.isDefined,
      "Unable to get an instance of Configuration as it hasn't been initialised yet.")

    instance.get
  }

}
