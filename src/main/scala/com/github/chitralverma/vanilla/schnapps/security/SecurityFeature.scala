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

import com.github.chitralverma.vanilla.schnapps.internal.ExceptionMapper
import javax.ws.rs.core.{Feature, FeatureContext}

import scala.util.Try

/**
 * Borrowed from Shiro JAX-RS module.
 */
class SecurityFeature extends Feature {
  override def configure(context: FeatureContext): Boolean = {
    Try {
      context.register(classOf[ExceptionMapper])
      context.register(classOf[AnnotationFilterFeature])
    }.toOption.isDefined
  }
}
