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

import java.util.Locale

import wvlet.log._
import wvlet.log.LogFormatter._
import wvlet.log.LogTimestampFormatter.formatTimestamp

import scala.reflect.ClassTag

abstract class Logging extends LogSupport {
  Logger.setDefaultFormatter(FrameworkLogFormatter)
  Logger.scheduleLogLevelScan

  def loggerOf[C: ClassTag]: Logger = Logger.of[C]
}

private object FrameworkLogFormatter extends LogFormatter {
  override def formatLog(r: LogRecord): String = {
    val location: String = r.source.map(_.fileLoc).getOrElse("")
    val formatted: String =
      s"${formatTimestamp(r.getMillis)} - " +
        s"${r.level.name.toUpperCase(Locale.ROOT)} - " +
        s"$location || ${r.getMessage}"

    appendStackTrace(formatted, r)
  }
}
