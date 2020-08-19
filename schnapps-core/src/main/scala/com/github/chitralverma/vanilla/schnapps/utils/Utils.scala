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

package com.github.chitralverma.vanilla.schnapps.utils

import java.util.{Locale, Properties}

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

  // scalastyle:off classforname
  def getInstance[C](classToInstantiate: String, initializationBody: Class[_] => C): C = {
    Try(initializationBody(Class.forName(classToInstantiate))) match {
      case Success(c) => c
      case Failure(e: ClassNotFoundException) =>
        throw new ClassNotFoundException(
          s"Class with name $classToInstantiate not found in the classpath",
          e)
      case Failure(e) =>
        throw new RuntimeException(s"Generic error trying to instantiate $classToInstantiate", e)
    }
  }
  // scalastyle:on classforname

  def lower(str: String): String = str.toLowerCase(Locale.ROOT)

}
