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

package com.github.chitralverma.schnapps.internal.filters.security

import java.lang.annotation.Annotation

import javax.ws.rs.container.{DynamicFeature, ResourceInfo}
import javax.ws.rs.core.FeatureContext
import javax.ws.rs.Priorities
import org.apache.shiro.authz.annotation._

import scala.collection.mutable.ArrayBuffer

/**
 * Wraps a [[AnnotationFilter]] around JAX-RS resources that are annotated
 * with Shiro annotations.
 *
 * Borrowed from Shiro JAX-RS module.
 */
object AnnotationFilterFeature {

  private val shiroAnnotations = Seq(
    classOf[RequiresPermissions],
    classOf[RequiresRoles],
    classOf[RequiresAuthentication],
    classOf[RequiresUser],
    classOf[RequiresGuest])
}

class AnnotationFilterFeature extends DynamicFeature {

  // XXX What is the performance of getAnnotation vs getAnnotations?
  override def configure(resourceInfo: ResourceInfo, context: FeatureContext): Unit = {
    val authzSpecs: ArrayBuffer[Annotation] = ArrayBuffer.empty[Annotation]
    for (annotationClass <- AnnotationFilterFeature.shiroAnnotations) {
      val classAuthzSpec: Annotation =
        resourceInfo.getResourceClass.getAnnotation(annotationClass)
      val methodAuthzSpec: Annotation =
        resourceInfo.getResourceMethod.getAnnotation(annotationClass)

      if (classAuthzSpec != null) authzSpecs.+=:(classAuthzSpec)
      if (methodAuthzSpec != null) authzSpecs.+=:(methodAuthzSpec)
    }

    if (authzSpecs.nonEmpty) {
      context.register(new AnnotationFilter(authzSpecs, resourceInfo), Priorities.AUTHORIZATION)
    }
  }
}
