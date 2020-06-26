package com.github.chitralverma.vanilla.schnapps.config.models

import com.github.chitralverma.vanilla.schnapps.utils.Utils

// todo check this config again
case class ExternalConfig(
    name: String = "undefined name",
    private val `type`: String,
    configs: Map[String, String] = Map.empty) {

  val tpe: String = Utils.lower(`type`)
}
