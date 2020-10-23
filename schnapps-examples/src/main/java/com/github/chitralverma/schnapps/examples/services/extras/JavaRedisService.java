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

package com.github.chitralverma.schnapps.examples.services.extras;

import com.github.chitralverma.schnapps.extras.ExternalManager;
import com.github.chitralverma.schnapps.extras.externals.redis.RedisExternal;
import com.github.chitralverma.schnapps.internal.RestService;
import org.jboss.resteasy.spi.HttpRequest;
import org.redisson.api.RedissonClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static scala.compat.java8.JFunction.func;

@Path("/redis")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class JavaRedisService extends RestService {
    @Override
    public Response get(HttpRequest request) {
        RedisExternal redisExt = (RedisExternal) ExternalManager.getExternal("redis_source").get();

        if (redisExt.isConnected()) {
            String entity = redisExt.executeThis(
                    func((RedissonClient rc) ->
                            String.format("{\"keys\": \"%s\"}", String.join(",", rc.getKeys().getKeys()))));

            return Response.ok(entity).build();
        } else {
            return Response.serverError().build();
        }
    }
}
