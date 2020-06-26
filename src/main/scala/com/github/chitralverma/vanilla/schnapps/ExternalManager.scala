/*
 * Copyright 2020 Chitral Verma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        val classes: Map[String, String] = scanExternals(ConfigParser.classFinder)
        val externalConfigs: Seq[(ExternalConfig, Option[String])] =
          ConfigParser.getConfiguration.externalConfigs.map(ec => (ec, classes.get(ec.tpe)))

        val (availExtSeq, nAvailExtSeq) = externalConfigs.partition(_._2.nonEmpty)
        if (nAvailExtSeq.nonEmpty) {
          logger.error(
            s"No associated classes were found for defined externals with names" +
              s"'${nAvailExtSeq.map(_._1.name).mkString("[", ",", "]")}'")

          throw new IllegalArgumentException(
            "Class not found for one or more external defined in provided config.")
        }

        val externals: Seq[External] = availExtSeq
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
    val instance: Option[Seq[External]] = Option(_managerInstance)
    assert(instance.isDefined, "Externals have not been loaded yet")

    _managerInstance
  }

  def getExternalConfig(tpe: String): Option[ExternalConfig] =
    ConfigParser.getConfiguration.externalConfigs.find(_.tpe.matches(tpe))

}
