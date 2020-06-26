package com.github.chitralverma.vanilla.schnapps.config.models

case class AppInfoModel(
    name: String,
    version: Option[String] = None,
    owner: Option[String] = None,
    organization: Option[String] = None) {}
