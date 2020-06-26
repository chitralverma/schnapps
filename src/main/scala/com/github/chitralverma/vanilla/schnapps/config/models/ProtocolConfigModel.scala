package com.github.chitralverma.vanilla.schnapps.config.models

import com.github.chitralverma.vanilla.schnapps.enums.{ProtocolEnums, SerializationEnums}
import com.github.chitralverma.vanilla.schnapps.internal.Constants

case class ProtocolConfigModel(
    name: String,
    protocol: ProtocolEnums.ProtocolEnum,
    port: Int,
    server: Option[String], // todo change this to enum
    serialization: Option[SerializationEnums.SerializationEnum] = None,
    contextPath: String = Constants.EmptyString)
