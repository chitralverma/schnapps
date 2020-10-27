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

package com.github.chitralverma.schnapps.utils

import java.lang.reflect.InvocationTargetException
import java.util
import java.util.{Locale, Properties}

import com.github.chitralverma.schnapps.config.models.ExternalConfigModel
import com.github.chitralverma.schnapps.internal.Logging
import javax.ws.rs.core.Response
import org.apache.shiro.SecurityUtils
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.session.Session
import org.apache.shiro.session.mgt.DefaultSessionManager
import org.slf4j.Logger

import scala.collection.JavaConverters._
import scala.io.Source
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

object Utils extends Logging {

  def generateRandomId(len: Int = 8): String = {
    if (len < 1) ""
    else scala.util.Random.alphanumeric.take(len).mkString
  }

  // scalastyle:off println
  def printLogo(): Unit = {
    import scala.io.BufferedSource
    val textArt: BufferedSource =
      Source.fromInputStream(ClassLoader.getSystemResourceAsStream("text-art.txt"))
    val p = new Properties()
    p.load(ClassLoader.getSystemResourceAsStream("maven.properties"))
    println(
      textArt
        .getLines()
        .mkString("\n")
        .replace("project.version", p.getProperty("project.version")))

    textArt.close()
  }
  // scalastyle:on println

  /**
   * Evaluates a functional composition with safety.
   *
   * @param f        functional block to evaluate
   * @param errorMsg in case the default error message needs to be overridden
   * @return [[T]] in case of `Success` result of type `T` is returned
   *        else a fatal exception is thrown.
   */
  def withTry[T](f: => T, errorMsg: String = "Fatal Error Occurred:"): T =
    Try(f) match {
      case Success(result) => result
      case Failure(ex) => throw new IllegalStateException(errorMsg, ex)
    }

  /**
   * Takes a functional block and errorMsg along with a boolean `showError`
   * to determine whether to show the message or not for that particular
   * function. By default the showError and execute it in the [[Try]]
   * block and prints the exception if encountered else returns
   * the result wrapped in Success class
   *
   * @param f         a functional block
   * @param errorMsg  a String having default value error occurred if not specified.
   * @param showError a boolean to specify whether to show the error message or not
   * @return [[Option]] returns the result wrapped in [[Success]] or [[Failure]]
   */
  def evaluateSafely[T](
      f: => T,
      errorMsg: String = "Error Occurred:",
      showError: Boolean = true,
      currentLogger: Logger = log): Option[T] = Try(f) match {
    case Success(s) => Option(s)
    case Failure(ex) =>
      if (showError) {
        if (currentLogger == null) {
          logWarning("Provided Logger is null, use default.")
          logError(errorMsg, ex)
        } else currentLogger.error(errorMsg, ex)
      }

      None
  }

  def getInstance[C](classToInstantiate: String, initializationBody: Class[_] => C): C = {
    Try(initializationBody(classForName(classToInstantiate))) match {
      case Success(c) => c
      case Failure(e: ClassNotFoundException) =>
        throw new ClassNotFoundException(
          s"Class with name $classToInstantiate not found in the classpath",
          e)
      case Failure(e) =>
        throw new RuntimeException(s"Generic error trying to instantiate $classToInstantiate", e)
    }
  }

  def lower(str: String): String = str.toLowerCase(Locale.ROOT)

  // scalastyle:off classforname
  /** Preferred alternative to Class.forName(className) */
  def classForName(className: String): Class[_] = {
    Class.forName(className, true, getContextOrMainClassLoader)
  }
  // scalastyle:on classforname

  /**
   * Get the main ClassLoader.
   */
  def getMainClassLoader: ClassLoader = getClass.getClassLoader

  /**
   * Get the Context ClassLoader on this thread or, if not present, the main ClassLoader.
   *
   * This should be used whenever passing a ClassLoader to Class.ForName or finding the currently
   * active loader when setting up ClassLoader delegation chains.
   *
   * Borrowed from Apache Spark
   */
  def getContextOrMainClassLoader: ClassLoader =
    Option(Thread.currentThread().getContextClassLoader).getOrElse(getMainClassLoader)

  def createResponse(status: Response.StatusType, entity: Any): Response =
    createResponse(status.getStatusCode, entity)

  def createResponse(status: Int, entity: Any): Response =
    Response.status(status).entity(entity).build

  def classAccessors[T: TypeTag]: List[String] =
    typeOf[T].members
      .collect {
        case m: MethodSymbol if m.isCaseAccessor => m
      }
      .toList
      .map(_.name.decodedName.toString)

  def getColumnFieldMapping[T: TypeTag, A: TypeTag]: Map[String, String] =
    symbolOf[T].asClass.primaryConstructor.typeSignature.paramLists.flatten
      .filter(_.annotations.nonEmpty)
      .map(
        x =>
          x.annotations
            .find(_.tree.tpe =:= typeOf[A])
            .map(_.tree.children.tail.map {
              case Literal(Constant(id: String)) => id
              case AssignOrNamedArg(_, Literal(Constant(id: String))) => id
            })
            .getOrElse(Seq(x.name.decodedName.toString))
            .head -> x.name.decodedName.toString)
      .toMap

  def getFieldColumnMapping[T: TypeTag, A: TypeTag]: Map[String, String] =
    getColumnFieldMapping[T, A].map(_.swap)

  def getGenericName[T: TypeTag](fullName: Boolean = false): String =
    if (fullName) typeOf[T].typeSymbol.fullName
    else typeOf[T].typeSymbol.name.toString

  def getExternalConfigByName(
      name: String,
      externalConfigs: Seq[ExternalConfigModel]): Option[ExternalConfigModel] = {
    externalConfigs.find(_.name.matches(name))
  }

  @throws(classOf[NoSuchMethodException])
  @throws(classOf[IllegalAccessException])
  @throws(classOf[InvocationTargetException])
  def getActiveSessions: Map[String, Session] = {
    val securityManager = SecurityUtils.getSecurityManager.asInstanceOf[DefaultSecurityManager]

    val getActiveSessionsMethod =
      classOf[DefaultSessionManager].getDeclaredMethod("getActiveSessions")
    getActiveSessionsMethod.setAccessible(true)

    getActiveSessionsMethod
      .invoke(securityManager.getSessionManager)
      .asInstanceOf[util.Collection[Session]]
      .asScala
      .map(s => s.getId.toString -> s)
      .toMap
  }

  def getSessionsByAttribute(attrKey: Object, attrValue: Object): Option[Iterable[Session]] = {
    Try(getActiveSessions.filter(_._2.getAttribute(attrKey) == attrValue).values).toOption
  }

  def stopSessions(session: Session, sessions: Session*): Unit = {
    val securityManager = SecurityUtils.getSecurityManager.asInstanceOf[DefaultSecurityManager]

    val deleteMethod =
      classOf[DefaultSessionManager].getDeclaredMethod("delete", classOf[Session])
    deleteMethod.setAccessible(true)

    logInfo(s"Discarding Session with ID '${session.getId}'")

    deleteMethod.invoke(securityManager.getSessionManager, session)
    sessions.foreach(s => {
      logInfo(s"Discarding Session with ID '${s.getId}'")
      deleteMethod.invoke(securityManager.getSessionManager, s)
    })
  }

}
