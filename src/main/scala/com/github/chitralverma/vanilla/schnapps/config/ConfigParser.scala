package com.github.chitralverma.vanilla.schnapps.config

import java.io.File

import com.github.chitralverma.vanilla.schnapps.enums._
import com.github.chitralverma.vanilla.schnapps.internal.Logging
import com.github.chitralverma.vanilla.schnapps.utils.Utils.withTry
import org.clapper.classutil.ClassFinder
import org.json4s.{file2JsonInput, DefaultFormats, Formats}
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.JsonMethods.{parse => jsonParse}

object ConfigParser extends Logging {

  implicit var jsonFormat: Formats = DefaultFormats +
    new EnumNameSerializer(ProtocolEnums)
  new EnumNameSerializer(HTTPMethodsEnums)
  new EnumNameSerializer(SerializationEnums)

  private var _instance: Configuration = _
  final val classFinder: ClassFinder = ClassFinder()

  def parse(args: Array[String]): Configuration = {
    if (_instance == null) {
      _instance = {

        val jobConfig = if (args.headOption.isDefined) {
          val configPath = args(0)
          logger.info(s"Attempting to load configuration from path: '$configPath'")
          withTry(
            jsonParse(file2JsonInput(new File(configPath))).extract[Configuration],
            s"Fatal Error: Unable to initialize configuration from config at path: '$configPath'")
        } else {
          throw new IllegalStateException(
            "Path to json configuration was not provided in arguments")
        }

        jobConfig
      }
    } else {
      logger.warn("Configuration is already initialised.")
    }

    _instance
  }

  def getConfiguration: Configuration = {
    val instance = Option(_instance)
    assert(
      instance.isDefined,
      "Unable to get an instance of Configuration as it hasn't been initialised yet")

    instance.get
  }

}
