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

package com.github.chitralverma.schnapps.internal

import com.github.chitralverma.schnapps.config.ConfigParser
import com.github.chitralverma.schnapps.enums.HTTPMethodsEnums
import javax.ws.rs._
import javax.ws.rs.core.{Context, Response}
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.jboss.resteasy.spi.HttpRequest

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

trait Service {
  @GET @POST @PUT @DELETE @HEAD @OPTIONS
  def onRequest(@Context request: HttpRequest): Response
}

abstract class RestService extends Logging with Service {

  protected val DefaultResponse: Response =
    Response.status(Response.Status.NOT_IMPLEMENTED).entity("NOT IMPLEMENTED").build()

  override def onRequest(request: HttpRequest): Response = {
    val reqMethod: String = request.getHttpMethod

    if (ConfigParser.getConfiguration.serverConfig.logAccess) {
      logInfo(s"New Request with method '$reqMethod' received at path ${request.getUri.getPath}")
    }

    Try { HTTPMethodsEnums.withName(reqMethod) } match {
      case Success(HTTPMethodsEnums.GET) => get(request)
      case Success(HTTPMethodsEnums.POST) => post(request)
      case Success(HTTPMethodsEnums.OPTIONS) => options(request)
      case Success(HTTPMethodsEnums.DELETE) => delete(request)
      case Success(HTTPMethodsEnums.PUT) => put(request)
      case Success(_) => unknown(request)
      case Failure(ex) =>
        logError(ex.getMessage, ex)
        unknown(request)
    }
  }

  private def unknown(request: HttpRequest): Response = {
    val errorMsg = s"Request with invalid method '${request.getHttpMethod}' received"
    logError(errorMsg, new IllegalStateException())

    Response.status(Response.Status.BAD_REQUEST.getStatusCode).entity(errorMsg).build()
  }

  def get(request: HttpRequest): Response = DefaultResponse

  def post(request: HttpRequest): Response = DefaultResponse

  def options(request: HttpRequest): Response = DefaultResponse

  def delete(request: HttpRequest): Response = DefaultResponse

  def put(request: HttpRequest): Response = DefaultResponse

  def getRequestBody(request: HttpRequest): Try[String] = {
    Try {
      val source: BufferedSource = Source.fromInputStream(request.getInputStream)
      val requestBody: String = source.mkString
      source.close()

      requestBody
    }
  }

}

trait CustomSubject {
  def getSubject(request: HttpRequest): Subject = SecurityUtils.getSubject
}
