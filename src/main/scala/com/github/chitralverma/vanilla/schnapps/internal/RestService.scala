package com.github.chitralverma.vanilla.schnapps.internal

import com.github.chitralverma.vanilla.schnapps.enums.HTTPMethodsEnums
import javax.ws.rs._
import javax.ws.rs.core.{Context, Response}
import org.jboss.resteasy.spi.{HttpRequest, HttpResponse}

import scala.io.Source
import scala.util.{Failure, Success, Try}
// import org.apache.shiro.subject.Subject

trait RestService extends Logging {

  @GET @POST @PUT @DELETE @HEAD @OPTIONS
  protected def onRequest(
      @Context request: HttpRequest,
      @Context response: HttpResponse): Unit = {
    val reqMethod = request.getHttpMethod

    logger.debug(
      s"New Request with method '$reqMethod' received at path ${request.getUri.getPath}")
    Try { HTTPMethodsEnums.withName(reqMethod) } match {
      case Success(HTTPMethodsEnums.GET) => get(request, response)
      case Success(HTTPMethodsEnums.POST) => post(request, response)
      case Failure(ex) =>
        logger.error(ex.getMessage, ex)
        unknown(request, response)
    }
  }

  private def unknown(request: HttpRequest, response: HttpResponse): Unit = {
    val errorMsg = s"Request with invalid method '${request.getHttpMethod}' received"
    logger.error(errorMsg, new IllegalStateException())

    response.sendError(Response.Status.BAD_REQUEST.getStatusCode, errorMsg)
  }

  def get(request: HttpRequest, response: HttpResponse): Unit = {}
  def post(request: HttpRequest, response: HttpResponse): Unit = {}

  def getRequestBody(request: HttpRequest): Option[String] = {
    Try {
      val source = Source.fromInputStream(request.getInputStream)
      val requestBody = source.mkString
      source.close()

      requestBody
    } match {
      case Success(str) => Option(str)
      case Failure(ex) =>
        ex.printStackTrace()
        None
    }
  }

}
