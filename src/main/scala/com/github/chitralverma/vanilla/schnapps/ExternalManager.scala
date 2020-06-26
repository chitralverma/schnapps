package com.github.chitralverma.vanilla.schnapps

import com.github.chitralverma.vanilla.schnapps.config.ConfigParser
import com.github.chitralverma.vanilla.schnapps.config.models.ExternalConfig
import com.github.chitralverma.vanilla.schnapps.internal.{External, Logging}
import com.github.chitralverma.vanilla.schnapps.utils.Utils
import org.clapper.classutil.ClassFinder

object ExternalManager extends Logging {

  private var _managerInstance: Seq[External] = _

  def loadExternals(): Seq[External] = {
    if (_managerInstance == null) {
      _managerInstance = {
        val classes = scanExternals(ConfigParser.classFinder)
        val externalConfigs =
          ConfigParser.getConfiguration.externalConfigs.map(ec => (ec, classes.get(ec.tpe)))

        val (availExtSeq, nAvailExtSeq) = externalConfigs.partition(_._2.nonEmpty)
        if (nAvailExtSeq.nonEmpty) {
          logger.error(
            s"No associated classes were found for defined externals with names" +
              s"'${nAvailExtSeq.map(_._1.name).mkString("[", ",", "]")}'")

          throw new IllegalArgumentException(
            "Class not found for one or more external defined in provided config.")
        }

        val externals = availExtSeq
          .map(
            ec =>
              Utils.getInstance[External](
                ec._2.get,
                c =>
                  c.getDeclaredConstructor(classOf[ExternalConfig])
                    .newInstance(ec._1)
                    .asInstanceOf[External]))

        availExtSeq.foreach(
          ec =>
            logger.info(s"Configured an external with class name '${ec._2.get}', " +
              s"type '${ec._1.tpe}' and given name '${ec._1.name}'"))

        externals
      }

      logger.info("Externals loaded successfully")
    } else logger.warn("Externals already loaded")

    _managerInstance
  }

  private def scanExternals(finder: ClassFinder): Map[String, String] = {
    import com.github.chitralverma.vanilla.schnapps.internal.Constants
    ClassFinder
      .concreteSubclasses(classOf[External].getCanonicalName, finder.getClasses())
      .flatMap(x =>
        x.annotations.flatMap(_.params).toMap.get(Constants.Type) match {
          case Some(tpe) => Some(Utils.lower(tpe.toString), x.name)
          case None => None
        })
      .toMap
  }

  def getExternal[T](name: String): Option[T] = {
    getExternals.find(_.name.equalsIgnoreCase(name)).map(_.as[T])
  }

  def getExternals: Seq[External] = {
    val instance = Option(_managerInstance)
    assert(instance.isDefined, "Externals have not been loaded yet")

    _managerInstance
  }

  def getExternalConfig(tpe: String): Option[ExternalConfig] =
    ConfigParser.getConfiguration.externalConfigs.find(_.tpe.matches(tpe))

}
