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

import java.lang.annotation.Annotation

import com.github.chitralverma.vanilla.schnapps.internal.{CustomSubject, Logging}
import com.github.chitralverma.vanilla.schnapps.security.handlers._
import javax.ws.rs.container._
import javax.ws.rs.core.Context
import org.apache.shiro.authz.annotation._
import org.apache.shiro.authz.aop._
import org.apache.shiro.subject.Subject
import org.apache.shiro.SecurityUtils
import org.jboss.resteasy.spi.HttpRequest

import scala.util.{Failure, Success, Try}

/**
 * Borrowed from Shiro JAX-RS module.
 */
class AnnotationFilter(val authzSpecs: Seq[Annotation], resourceInfo: ResourceInfo)
    extends Logging
    with ContainerRequestFilter {

  @Context var request: HttpRequest = _
  @Context var resourceContext: ResourceContext = _

  private def createHandler(
      annotation: Annotation,
      subject: Subject): AuthorizingAnnotationHandler = {

    annotation match {
      case _: RequiresPermissions => new PermissionAnnotationHandler
      case _: RequiresRoles => new RoleAnnotationHandler
      case _: RequiresUser => new UserAnnotationHandler
      case _: RequiresGuest => new RequiresGuestHandler(subject)
      case _: RequiresAuthentication => new RequiresAuthenticationHandler(subject)
      case _ =>
        throw new IllegalArgumentException(
          s"Cannot create a handler for the unknown annotation " +
            s"'${annotation.annotationType().getCanonicalName}")
    }
  }

  override def filter(requestContext: ContainerRequestContext): Unit = {
    val resource: Any = resourceContext.getResource(resourceInfo.getResourceClass)

    val authzChecks: Map[AuthorizingAnnotationHandler, Annotation] =
      authzSpecs.map(authSpec => (createHandler(authSpec, getSubject(resource)), authSpec)).toMap

    authzChecks.foreach(authzCheck => {
      val handler: AuthorizingAnnotationHandler = authzCheck._1
      val authzSpec: Annotation = authzCheck._2

      handler.assertAuthorized(authzSpec)
    })
  }

  def getSubject(resource: Any): Subject = {
    val subject: Subject = Try(resource.asInstanceOf[CustomSubject].getSubject(request)) match {
      case Success(sub) => sub
      case Failure(exception: ClassCastException) =>
        logger.error(
          s"Resource Class '${resourceInfo.getResourceClass}' " +
            s"does not implement ${classOf[CustomSubject].getCanonicalName}",
          exception)
        SecurityUtils.getSubject
      case Failure(_) =>
        logger.warn("Error encountered while building Subject")
        SecurityUtils.getSubject
    }

    subject
  }
}
