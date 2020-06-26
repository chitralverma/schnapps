package com.github.chitralverma.vanilla.schnapps.config.models

case class ServiceDefinitionModel(
    version: Option[String],
    className: String,
    interfaceName: Option[String] = None,
    protocolName: String,
    path: Option[String] = None) {}
