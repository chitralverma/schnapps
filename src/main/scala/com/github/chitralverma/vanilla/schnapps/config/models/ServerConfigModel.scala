package com.github.chitralverma.vanilla.schnapps.config.models

import com.github.chitralverma.vanilla.schnapps.internal.Constants

case class ServerConfigModel(
    host: String,
    logAccess: Boolean = Constants.False,
    enableQOS: Boolean = Constants.False,
    threads: Option[Int] = None,
    maxPayloadBytes: Option[Int] = None,
    ioThreads: Option[Int] = None,
    maxConnections: Option[Int] = None,
    shiroIniPath: Option[String] = None,
    serviceRegistryConfig: Option[ServiceRegistryConfigModel] = None,
    protocolConfigs: Seq[ProtocolConfigModel]) {}
