/*
 *    Copyright 2020 Chitral Verma
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.chitralverma.schnapps.extras

import java.util.ServiceLoader

import com.github.chitralverma.schnapps.config.Configuration
import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.chitralverma.schnapps.extras.externals.External
import com.github.chitralverma.schnapps.internal.Logging
import com.github.chitralverma.schnapps.utils.Utils

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}

object ExternalManager extends Logging {

  private var _managerInstance: TrieMap[String, External] = _

  def loadExternals(config: Configuration): TrieMap[String, External] = {
    if (_managerInstance == null) {
      _managerInstance = {
        assertUniqueExternals(config)

        val classes: Map[String, String] = scanExternals()
        val externalConfigs: Seq[(ExternalConfigModel, Option[String])] =
          config.externalConfigs.map(ec => (ec, classes.get(ec.tpe)))

        val (availExtSeq, nAvailExtSeq) = externalConfigs.partition(_._2.nonEmpty)
        if (nAvailExtSeq.nonEmpty) {
          logError(
            s"No implementation was found for provided External(s) with name(s) in" +
              s" ${nAvailExtSeq.map(_._1.name).mkString("['", "', '", "']")}")

          throw new IllegalArgumentException(
            "Class not found for one or more external defined in provided config.")
        }

        availExtSeq.foreach(x => x)

        val externals: Seq[(String, External)] = availExtSeq
          .map(x => (x._1, Utils.classForName(x._2.get)))
          .map(ec =>
            (Utils.lower(ec._1.name), {
              val external: External = ec._2.newInstance().asInstanceOf[External]
              external.initialise(ec._1)
              logInfo(
                s"Configured an external with class name '${ec._2.getCanonicalName}', " +
                  s"type '${ec._1.tpe}' and given name '${ec._1.name}'")

              external
            }))

        TrieMap.empty[String, External] ++ externals
      }

      logInfo("Externals loaded successfully.")
    } else logWarning("Externals already loaded.")

    _managerInstance
  }

  private def assertUniqueExternals(config: Configuration): Unit = {
    val duplicates: Iterable[String] =
      config.externalConfigs
        .map(_.name)
        .groupBy(x => x)
        .mapValues(_.size)
        .filter(_._2 > 1)
        .keys

    if (duplicates.nonEmpty) {
      throw new IllegalArgumentException(
        s"Names of externals must be unique. External(s)" +
          s" with name(s) in ${duplicates.mkString("['", "', '", "']")} had duplicates.")
    }

  }

  private def scanExternals(): Map[String, String] = {
    val loader: ClassLoader = Utils.getContextOrMainClassLoader
    val serviceLoader: ServiceLoader[External] = ServiceLoader.load(classOf[External], loader)

    val classMap: Map[String, String] = serviceLoader.asScala
      .map(x => x.tpe -> x.getClass.getCanonicalName)
      .toSeq
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .map({
        case (tpe, impls) if impls.size > 1 =>
          val sourceNames: Seq[String] = impls.map(_.getClass.getName)
          val internalSources: Seq[String] =
            impls.filter(_.getClass.getName.startsWith("com.github.chitralverma.schnapps"))
          if (internalSources.size == 1) {
            logWarning(
              s"Multiple External implementations in classes [${sourceNames.mkString(", ")}] " +
                s"found for given type '$tpe'. Defaulting to internal implementation " +
                s"in class '${internalSources.head.getClass.getName}'.")

            (tpe, internalSources.head.getClass.getCanonicalName)
          } else {
            throw new IllegalStateException(
              s"Multiple External implementations in classes [${sourceNames.mkString(", ")}] " +
                s"found for given type '$tpe'. Please specify the fully qualified class name.")
          }

        case (tpe, impl :: Nil) => (tpe, impl)
      })
      .map(x => Utils.lower(x._1) -> x._2)

    classMap
      .map(c => Utils.classForName(c._2))
      .foreach(cls =>
        assert(
          classOf[External].isAssignableFrom(cls),
          s"Class '${cls.getCanonicalName}' does not extend ${classOf[External].getCanonicalName}"))
    classMap
  }

  def getExternal[T](name: String): Option[T] = {
    Try(getExternals.get(Utils.lower(name)).map(_.as[T])) match {
      case Failure(exception) =>
        logError(
          s"Exception occurred while getting external instance with name '$name'",
          exception)
        None
      case Success(value) => value
    }
  }

  def getExternalsByType[T](tpe: String): TrieMap[String, External] = {
    getExternals.filter(_._2.tpe.equalsIgnoreCase(tpe))
  }

  def getExternals: TrieMap[String, External] = {
    val instance: Option[TrieMap[String, External]] = Option(_managerInstance)
    assert(instance.isDefined, "Externals have not been loaded yet.")

    _managerInstance
  }

}
