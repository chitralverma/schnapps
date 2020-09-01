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

import java.util.{Locale, Properties}

import javax.ws.rs.core.Response

import scala.io.Source
import scala.util.{Failure, Success, Try}

object Utils {
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

  def withTry[T](f: => T, errorMsg: String = "Fatal Error Occurred:"): T =
    Try(f) match {
      case Success(result) => result
      case Failure(ex) => throw new IllegalStateException(errorMsg, ex)
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
}
