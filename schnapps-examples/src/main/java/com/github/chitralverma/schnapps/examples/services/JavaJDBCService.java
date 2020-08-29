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


import com.github.chitralverma.schnapps.extras.ExternalManager;
import com.github.chitralverma.schnapps.extras.externals.jdbc.JDBCExternal;
import com.github.chitralverma.schnapps.internal.CustomSubject;
import com.github.chitralverma.schnapps.internal.RestService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.jboss.resteasy.spi.HttpRequest;
import scala.Unit;
import scala.runtime.AbstractFunction1;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("tables")
@RequiresAuthentication
public class JavaJDBCService extends RestService implements CustomSubject {
    @Override
    public Response get(HttpRequest request) {
        JDBCExternal jdbcLink = (JDBCExternal) ExternalManager.getExternal("hsqldb_source").get();

        ArrayList<String> arr = new ArrayList<>();


        jdbcLink.executeThis(new AbstractFunction1<Connection, Unit>() {
            @Override
            public Unit apply(Connection c) {
                ResultSet queryResult = null;
                try {
                    queryResult = c.prepareStatement(
                            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE='TABLE'"
                    ).executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                while (true) {
                    try {
                        assert queryResult != null;
                        if (!queryResult.next()) break;
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    try {
                        arr.add(queryResult.getString(1));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }

                return null;
            }
        });

        return Response.ok().entity(String.join(", ", arr)).build();
    }

    @Override
    public Subject getSubject(HttpRequest request) {
        String sessionID = request.getHttpHeaders().getRequestHeader("sessionID").get(0);
        return new Subject.Builder().sessionId(sessionID).buildSubject();
    }
}
