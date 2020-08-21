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

package com.github.chitralverma.schnapps.internal.filters.security.handlers

import java.lang.annotation.Annotation

import org.apache.shiro.authz.UnauthenticatedException
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler
import org.apache.shiro.subject.Subject

/**
 * Handles [[RequiresAuthentication]] annotation and ensures the calling subject is
 * authenticated before allowing access.
 *
 * Borrowed from Shiro JAX-RS module.
 */
class RequiresAuthenticationHandler(subject: Subject)
    extends AuthorizingAnnotationHandler(classOf[RequiresAuthentication]) {

  /**
   * Ensures that the calling <code>Subject</code> is authenticated, and if not,
   * throws an [[UnauthenticatedException]] indicating the method is not allowed to be executed.
   *
   * @param a the annotation to inspect
   * @throws UnauthenticatedException if the calling <code>Subject</code> is not authenticated.
   */
  @throws[UnauthenticatedException]
  override def assertAuthorized(a: Annotation): Unit = {
    a match {
      case _: RequiresAuthentication =>
        if (subject == null || !subject.isAuthenticated) {
          throw new UnauthenticatedException(
            "The current Subject is not authenticated. Access denied.")
        }
      case _ =>
    }
  }
}
