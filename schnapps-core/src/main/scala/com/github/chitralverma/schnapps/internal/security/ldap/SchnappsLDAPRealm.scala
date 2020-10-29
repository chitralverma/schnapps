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

package com.github.chitralverma.schnapps.internal.security.ldap

import com.github.chitralverma.schnapps.internal.Logging
import javax.naming.directory.SearchControls
import javax.naming.ldap.LdapContext
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.{AuthenticationInfo, AuthenticationToken}
import org.apache.shiro.realm.ldap.{DefaultLdapRealm, LdapContextFactory, LdapUtils}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class SchnappsLDAPRealm extends DefaultLdapRealm {
  case object Logger extends Logging

  import Logger._

  private final val MatchAllFilterStr = "(objectClass=*)"

  private var sessionAttrs: String = ""
  private var searchControls: SearchControls = _

  def getSessionAttrs: String = sessionAttrs

  def setSessionAttrs(sessionAttrs: String): Unit = {
    this.sessionAttrs = sessionAttrs
  }

  override def queryForAuthenticationInfo(
      token: AuthenticationToken,
      ldapContextFactory: LdapContextFactory): AuthenticationInfo = {
    var principal = token.getPrincipal
    val credentials = token.getCredentials

    logDebug(s"Authenticating user '{$principal}' through LDAP")
    principal = getLdapPrincipal(token)

    val authInfo: Option[AuthenticationInfo] =
      Try(ldapContextFactory.getLdapContext(principal, credentials)) match {
        case Success(ctx) =>
          val info = Some(super.createAuthenticationInfo(token, principal, credentials, ctx))
          val session = SecurityUtils.getSubject.getSession
          getUserInfo(ctx, principal.toString).foreach(entry =>
            session.setAttribute(entry._1, entry._2))

          LdapUtils.closeContext(ctx)
          info
        case Failure(_) => None
      }

    authInfo.orNull
  }

  private def getSearchControls = {
    if (searchControls == null) {
      searchControls = {
        val attrs = getSessionAttrs.split(",").map(_.trim)
        val cons = new SearchControls()
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE)
        cons.setReturningAttributes(attrs)

        cons
      }
    }
    searchControls
  }

  private def getUserInfo(ldapCtx: LdapContext, ctxPath: String): Map[String, Any] = {
    ldapCtx
      .search(ctxPath, MatchAllFilterStr, getSearchControls)
      .asScala
      .flatMap(x => x.getAttributes.getIDs.asScala.zip(x.getAttributes.getAll.asScala))
      .map(x => (x._1, x._2.get()))
      .toMap
  }
}
