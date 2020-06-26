package com.github.chitralverma.vanilla.schnapps.internal

import scala.util.{Failure, Success, Try}

import com.github.chitralverma.vanilla.schnapps.config.models.ExternalConfig

abstract class External(config: ExternalConfig) extends Logging {

  type T
  type E

  val name: String = config.name

  final private val _instance: Singleton[T] = Try(connect()) match {
    case Success(i) => i
    case Failure(e) =>
      logger.error("Error occured while connecting to External", e)
      throw e
  }

  protected def connect(): Singleton[T]
  def disconnect(): Unit // todo use this
  def executeThis[O](f: E => O): O
  def as[A]: A = this.asInstanceOf[A]
  def getAs[T]: T = _instance.get.asInstanceOf[T]

}
