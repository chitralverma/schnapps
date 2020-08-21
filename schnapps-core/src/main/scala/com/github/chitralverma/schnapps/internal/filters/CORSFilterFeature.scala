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

package com.github.chitralverma.schnapps.internal.filters

import com.github.chitralverma.schnapps.config.ConfigParser
import com.github.chitralverma.schnapps.config.models.ExtensionsModel
import com.github.chitralverma.schnapps.internal.Logging
import javax.ws.rs.container.{DynamicFeature, ResourceInfo}
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.Priorities
import org.jboss.resteasy.plugins.interceptors.CorsFilter

import scala.collection.JavaConverters._

/**
 * Handles CORS requests both preflight and simple CORS requests.
 */
object CORSFilterFeature {
  final val AllowedOrigins: String = "allowedOrigins"
  final val AllowCredentials: String = "allowCredentials"
  final val AllowedHeaders: String = "allowedHeaders"
  final val AllowedMethods: String = "allowedMethods"
  final val CorsMaxAge: String = "corsMaxAge"
  final val ExposedHeaders: String = "exposedHeaders"
}

class CORSFilterFeature extends Logging with DynamicFeature {
  import com.github.chitralverma.schnapps.internal.filters.CORSFilterFeature._

  override def configure(resourceInfo: ResourceInfo, context: FeatureContext): Unit = {
    val corsFilter = new CorsFilter()

    ConfigParser.getConfiguration.serverConfig.extensions match {
      case Some(em: ExtensionsModel) =>
        debug("Found 'corsOptions' in extensions config.")

        if (em.corsOptions.contains(AllowedOrigins)) {
          corsFilter.getAllowedOrigins
            .addAll(em.corsOptions(AllowedOrigins).asInstanceOf[Seq[String]].asJavaCollection)
        }

        if (em.corsOptions.contains(AllowCredentials)) {
          corsFilter.setAllowCredentials(Boolean.unbox(em.corsOptions(AllowCredentials)))
        }

        if (em.corsOptions.contains(AllowCredentials)) {
          corsFilter.setAllowCredentials(Boolean.unbox(em.corsOptions(AllowCredentials)))
        }

        if (em.corsOptions.contains(AllowedHeaders)) {
          corsFilter.setAllowedHeaders(String.valueOf(em.corsOptions(AllowedHeaders)))
        }

        if (em.corsOptions.contains(AllowedMethods)) {
          corsFilter.setAllowedMethods(String.valueOf(em.corsOptions(AllowedMethods)))
        }

        if (em.corsOptions.contains(CorsMaxAge)) {
          corsFilter.setCorsMaxAge(Int.unbox(em.corsOptions(CorsMaxAge)))
        }

        if (em.corsOptions.contains(ExposedHeaders)) {
          corsFilter.setExposedHeaders(String.valueOf(em.corsOptions(ExposedHeaders)))
        }

      case None =>
        debug("No 'corsOptions' in extensions config.")
    }

    context.register(corsFilter, Priorities.HEADER_DECORATOR)
  }

}
