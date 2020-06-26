package com.github.chitralverma.vanilla.schnapps.utils

import java.util.Locale

import scala.util.{Failure, Success, Try}

object Utils {

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
