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

package com.github.chitralverma.schnapps.security.handlers

import java.lang.annotation.Annotation

import org.apache.shiro.authz.{AuthorizationException, UnauthorizedException}
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.authz.aop.AuthorizingAnnotationHandler
import org.apache.shiro.subject.Subject

/**
 * Handles [[RequiresGuest]] annotation and ensures the calling subject is
 * authenticated before allowing access.
 *
 * Borrowed from Shiro JAX-RS module.
 */
class RequiresGuestHandler(subject: Subject)
    extends AuthorizingAnnotationHandler(classOf[RequiresGuest]) {

  /**
   * Ensures that the calling <code>Subject</code> is NOT a <em>user</em>, that is, they do not
   * have an  before continuing.  If they are a user ( != null), an
   * <code>AuthorizingException</code> will be thrown indicating
   * that execution is not allowed to continue.
   *
   * @param a the annotation to check for one or more roles
   * @throws org.apache.shiro.authz.AuthorizationException
   * if the calling <code>Subject</code> is not a &quot;guest&quot;.
   */
  @throws[AuthorizationException]
  override def assertAuthorized(a: Annotation): Unit = {
    if (a.isInstanceOf[RequiresGuest] && subject.getPrincipal != null) {

      throw new UnauthorizedException(
        "Attempting to perform a guest-only operation. " +
          "The current Subject is not a guest (they have been authenticated or" +
          " remembered from a previous login). Access " + "denied.")
    }
  }
}
