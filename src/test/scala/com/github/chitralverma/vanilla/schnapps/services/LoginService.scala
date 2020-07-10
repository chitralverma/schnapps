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

package com.github.chitralverma.vanilla.schnapps.services

import com.github.chitralverma.vanilla.schnapps.internal.{CustomSubject, RestService}
import javax.ws.rs._
import javax.ws.rs.core.Response
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.jboss.resteasy.spi.HttpRequest

@Path("login")
@RequiresGuest
class LoginService extends RestService with CustomSubject {

  override def get(request: HttpRequest): Response = {
    val username: String = request.getHttpHeaders.getRequestHeader("user").get(0)
    val password: String = request.getHttpHeaders.getRequestHeader("pass").get(0)

    val token = new UsernamePasswordToken(username, password)
    val sub: Subject = SecurityUtils.getSubject
    sub.login(token)

    Response.ok.entity(s"Logged in with session ID: ${sub.getSession.getId}").build()
  }

  override def post(request: HttpRequest): Response = DefaultResponse

  override def getSubject(request: HttpRequest): Subject = {
    val sessionID: String = request.getHttpHeaders.getRequestHeader("sessionID").get(0)
    new Subject.Builder().sessionId(sessionID).buildSubject()
  }
}
