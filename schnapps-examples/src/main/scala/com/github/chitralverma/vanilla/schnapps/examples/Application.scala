/*
 * Copyright 2020 Chitral Verma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.chitralverma.vanilla.schnapps.examples

import com.github.chitralverma.vanilla.schnapps.{ExternalManager, Server}
import com.github.chitralverma.vanilla.schnapps.config.{ConfigParser, Configuration}
import com.github.chitralverma.vanilla.schnapps.examples.utils.EmbeddedDB

object Application {

  def main(args: Array[String]): Unit = {
    val configuration: Configuration = ConfigParser.parse(args)

    configuration.externalConfigs.find(_.name.matches("hsqldb_source")) match {
      case Some(ecm) =>
        EmbeddedDB.start(ecm.configs)
        ExternalManager.loadExternals(configuration)
      case None =>
    }

    Server.bootUp(configuration)
    Server.await()
  }

}
