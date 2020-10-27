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

package com.github.chitralverma.schnapps.examples.services;

import com.github.chitralverma.schnapps.internal.CustomSubject;
import com.github.chitralverma.schnapps.internal.RestService;
import com.github.chitralverma.schnapps.utils.Utils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.jboss.resteasy.spi.HttpRequest;
import scala.Option;
import scala.collection.Iterable;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("login")
@RequiresGuest
public class JavaLoginService extends RestService implements CustomSubject {

    final String UserHeader = "user";
    final String PasswordHeader = "pass";
    final String SessionIDHeader = "sessionID";

    @Override
    public Response get(HttpRequest request) {
        String username = request.getHttpHeaders().getRequestHeader(UserHeader).get(0);
        String password = request.getHttpHeaders().getRequestHeader(PasswordHeader).get(0);

        discardParallelSessionIfAny(username);

        AuthenticationToken token = new UsernamePasswordToken(username, password);
        Subject sub = SecurityUtils.getSubject();
        sub.login(token);
        sub.getSession().setAttribute(UserHeader, username);

        return Response.ok().entity(String.format("Logged in with session ID: %s",
                sub.getSession().getId())).build();
    }

    @Override
    public Subject getSubject(HttpRequest request) {
        String sessionID = request.getHttpHeaders().getRequestHeader(SessionIDHeader).get(0);
        return new Subject.Builder().sessionId(sessionID).buildSubject();
    }

    private void discardParallelSessionIfAny(String username) {
        Option<Iterable<Session>> sessionsOpt = Utils.getSessionsByAttribute(UserHeader, username);
        if (sessionsOpt.isEmpty()) {
            logError("Error occurred while fetching the sessions for given username");
        } else {
            Iterable<Session> sessions = sessionsOpt.get();
            if (sessions.nonEmpty()) {
                Utils.stopSessions(sessions.head(), sessions.tail().toSeq());
            }
        }

    }
}
