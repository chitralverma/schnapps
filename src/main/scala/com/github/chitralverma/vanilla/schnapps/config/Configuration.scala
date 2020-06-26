package com.github.chitralverma.vanilla.schnapps.config

import com.github.chitralverma.vanilla.schnapps.config.models._

case class Configuration(
    appInfo: AppInfoModel,
    serverConfig: ServerConfigModel,
    services: Seq[ServiceDefinitionModel],
    externalConfigs: Seq[ExternalConfig])
