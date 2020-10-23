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

package com.github.chitralverma.schnapps.config.models

import com.github.chitralverma.schnapps.utils.Utils
import org.yaml.snakeyaml.Yaml

case class ExternalConfigModel(
    name: String,
    private val `type`: String,
    configs: Map[String, Any] = Map.empty) {

  val configAsJsonStr: String = {
    import com.fasterxml.jackson.databind.ObjectMapper
    val mapper = new ObjectMapper()
    mapper.registerModule(com.fasterxml.jackson.module.scala.DefaultScalaModule)

    mapper.writeValueAsString(configs)
  }

  val configAsYamlStr: String = {
    import org.codehaus.jackson.map.ObjectMapper
    val mapper = new ObjectMapper()
    val jsonNodeTree = mapper.readTree(configAsJsonStr)
    val v = mapper.convertValue(jsonNodeTree, classOf[java.util.Map[String, Object]])

    new Yaml().dump(v)
  }

  val tpe: String = Utils.lower(`type`)
}
