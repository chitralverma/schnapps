package com.github.chitralverma.vanilla.schnapps.config.models

case class ServiceRegistryConfigModel(
    address: String,
    username: Option[String] = None,
    password: Option[String] = None,
    group: Option[String] = None,
    client: Option[String] = None,
    timeoutMs: Option[Int] = None,
    workingDir: Option[String],
    useAsConfigCenter: Boolean = true,
    useAsMetadataCenter: Boolean = true,
    extraParams: Map[String, String] = Map.empty) {}
