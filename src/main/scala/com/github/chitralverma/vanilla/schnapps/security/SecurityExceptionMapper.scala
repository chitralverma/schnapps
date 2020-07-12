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

package com.github.chitralverma.vanilla.schnapps.security

import com.github.chitralverma.vanilla.schnapps.internal.Logging
import javax.ws.rs.core.{Context, Response}
import javax.ws.rs.core.Response.Status
import javax.ws.rs.ext.ExceptionMapper
import org.apache.shiro.ShiroException
import org.apache.shiro.authz._
import org.jboss.resteasy.spi.HttpRequest

/**
 * Borrowed from Shiro JAX-RS module.
 */
class SecurityExceptionMapper extends Logging with ExceptionMapper[ShiroException] {

  @Context var request: HttpRequest = _

  override def toResponse(exception: ShiroException): Response = {
    import org.apache.shiro.session.SessionException
    val status: Status = exception match {
      case _: HostUnauthorizedException => Status.UNAUTHORIZED
      case _: UnauthorizedException => Status.UNAUTHORIZED
      case _: UnauthenticatedException => Status.UNAUTHORIZED
      case _: SessionException => Status.UNAUTHORIZED
      case _ => Status.FORBIDDEN
    }

    logger.error(
      s"Encountered exception of type '${exception.getClass.getCanonicalName}' " +
        s"at path '${request.getUri.getPath}' with message '${exception.getMessage}'",
      exception)

    Response.status(status).build
  }
}
