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

import com.github.chitralverma.schnapps.internal.Logging
import com.github.chitralverma.schnapps.utils.Utils
import org.json4s.{Extraction, Formats, JNothing, JObject, _}

object DatabaseUtils extends Logging {

  /** Return an option that translates JNothing to None */
  def jsonOption(json: JValue): Option[JValue] = {
    json match {
      case JNothing => None
      case null => None
      case value: JValue => Some(value)
    }
  }

  def encodeAsJson(str: String, src: Any, formats: Formats): JValue =
    Utils.evaluateSafely(Extraction.decompose(src)(formats), currentLogger = log) match {
      case Some(v) =>
        if (v == JNull) JNothing else JObject((str, v))
      case None => JNothing
    }

}
