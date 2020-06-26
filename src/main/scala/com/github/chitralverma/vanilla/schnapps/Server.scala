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

package com.github.chitralverma.vanilla.schnapps

import java.io.File
import java.nio.file.Paths

import com.github.chitralverma.vanilla.schnapps.config.Configuration
import com.github.chitralverma.vanilla.schnapps.internal.{Constants => Cnsnts, _}
import com.github.chitralverma.vanilla.schnapps.utils.Utils
import org.apache.dubbo.config._
import org.apache.dubbo.config.bootstrap.DubboBootstrap

import scala.collection.JavaConverters._
// import org.apache.shiro.env.BasicIniEnvironment
// import org.apache.shiro.mgt.SecurityManager
// import org.apache.shiro.subject.Subject

object Server extends Logging {

  private var _serverInstance: DubboBootstrap = _
//  private var _securityManagerInstance: Option[SecurityManager] = _

//  def getSecurityManager: Option[SecurityManager] = _securityManagerInstance

  def bootUp(configuration: Configuration): Unit = {
    if (_serverInstance == null) {
      _serverInstance = {
        val appConfig: ApplicationConfig = createAppConfig(configuration)
        val registryConfig: RegistryConfig = createServiceRegistryConfig(configuration)
        val protocolConfigs: Map[String, ProtocolConfig] = createProtocols(configuration)
        val serviceConfigs: Seq[ServiceConfig[_]] = createServices(configuration, protocolConfigs)

        DubboBootstrap
          .getInstance()
          .application(appConfig)
          .registry(registryConfig)
          .services(serviceConfigs.asJava)
      }

//      _securityManagerInstance = serverConfigs.shiroINIPath.map(path => {
//        import org.apache.shiro.SecurityUtils
//        logger.info(s"Configuring Shiro Security using config at path '$path'")
//        val securityManager = new BasicIniEnvironment(path).getSecurityManager
//
//        SecurityUtils.setSecurityManager(securityManager)
//        securityManager
//      })

      _serverInstance.start()

      if (_serverInstance.isReady && _serverInstance.isStarted) {
        logger.info("Server has started successfully and is ready for use. Use `Server.await()`.")
      } else {
        logger.error("Unable to start the server", throw new IllegalStateException())
      }
    } else logger.warn("Server is already running")
  }

  def await(): Unit = {
    val instance: Option[DubboBootstrap] = Option(_serverInstance)
    assert(instance.isDefined, "Server has not been booted up yet. Use `Server.bootUp(...)`.")

    _serverInstance.await()
  }

  private def createAppConfig(configuration: Configuration): ApplicationConfig = {
    import com.github.chitralverma.vanilla.schnapps.config.models.AppInfoModel
    val appInfo: AppInfoModel = configuration.appInfo

    val appConfig = new ApplicationConfig()
    appConfig.setName(appInfo.name)
    appConfig.setQosEnable(configuration.serverConfig.enableQOS)

    if (appInfo.organization.isDefined) {
      appConfig.setOrganization(appInfo.organization.get)
    }

    if (appInfo.owner.isDefined) {
      appConfig.setOwner(appInfo.owner.get)
    }

    if (appInfo.version.isDefined) {
      appConfig.setVersion(appInfo.version.get)
    }

    appConfig
  }

  private def createServiceRegistryConfig(configuration: Configuration): RegistryConfig = {
    import com.github.chitralverma.vanilla.schnapps.config.models.ServiceRegistryConfigModel
    val serviceRegistryConfigOpt: Option[ServiceRegistryConfigModel] =
      configuration.serverConfig.serviceRegistryConfig
    val registryConfig = new RegistryConfig()

    serviceRegistryConfigOpt match {
      case Some(srcm) =>
        registryConfig.setAddress(srcm.address)
        registryConfig.setUseAsConfigCenter(srcm.useAsConfigCenter)
        registryConfig.setUseAsMetadataCenter(srcm.useAsMetadataCenter)

        if (srcm.username.isDefined) {
          registryConfig.setUsername(srcm.username.get)
        }

        if (srcm.password.isDefined) {
          registryConfig.setPassword(srcm.password.get)
        }

        if (srcm.client.isDefined) {
          registryConfig.setClient(srcm.client.get)
        }

        if (srcm.timeoutMs.isDefined) {
          registryConfig.setTimeout(srcm.timeoutMs.get)
        }

        if (srcm.workingDir.isDefined) {
          assert(
            new File(srcm.workingDir.get).isDirectory,
            s"Provided value '${srcm.workingDir}' for is not a directory.")
          val registryCacheFile: String =
            Paths
              .get(srcm.workingDir.get, Cnsnts.RegistryCacheDir, Cnsnts.RegistryCacheFile)
              .toString

          registryConfig.setFile(registryCacheFile)
        }

      case None =>
        registryConfig.setAddress(RegistryConfig.NO_AVAILABLE)
        registryConfig.setUseAsConfigCenter(true)
        registryConfig.setUseAsMetadataCenter(true)
    }

    registryConfig.setGroup(
      serviceRegistryConfigOpt.flatMap(_.group).getOrElse(configuration.appInfo.name))

    logger.info(
      s"Connected to a '${registryConfig.getProtocol}' service registry at " +
        s"address '${registryConfig.getAddress}'")

    registryConfig
  }

  private def createProtocols(configuration: Configuration): Map[String, ProtocolConfig] = {
    configuration.serverConfig.protocolConfigs
      .map(p => {
        val protocolConfig = new ProtocolConfig()
        protocolConfig.setHost(configuration.serverConfig.host)
        protocolConfig.setPort(p.port)
        protocolConfig.setName(p.protocol.toString)
        protocolConfig.setContextpath(p.contextPath)
        protocolConfig.setAccesslog(configuration.serverConfig.logAccess.toString)

        if (p.server.isDefined) {
          protocolConfig.setServer(p.server.get)
        }

        if (configuration.serverConfig.maxConnections.isDefined) {
          protocolConfig.setAccepts(configuration.serverConfig.maxConnections.get)
        }

        if (configuration.serverConfig.maxPayloadBytes.isDefined) {
          protocolConfig.setPayload(configuration.serverConfig.maxPayloadBytes.get)
        }

        if (configuration.serverConfig.ioThreads.isDefined) {
          protocolConfig.setIothreads(configuration.serverConfig.ioThreads.get)
        }

        if (configuration.serverConfig.threads.isDefined) {
          protocolConfig.setThreads(configuration.serverConfig.threads.get)
        }

        if (p.serialization.isDefined) {
          protocolConfig.setSerialization(p.serialization.get.toString)
        }

        logger.info(
          s"Created a '${p.protocol}' protocol with name '${p.name}' " +
            s"on host '${configuration.serverConfig.host}' port '${p.port}' and " +
            s"contextPath '${p.contextPath}'")
        p.name -> protocolConfig
      })
      .toMap
  }

  private def createServices(
      configuration: Configuration,
      protocolConfigs: Map[String, ProtocolConfig]): Seq[ServiceConfig[_]] = {

    val serviceConfigs: Seq[ServiceConfig[Any]] = configuration.services.map(definition => {
      protocolConfigs.get(definition.protocolName) match {
        case Some(protoConf) =>
          val serviceConfig = new ServiceConfig[Any]()
          serviceConfig.setProtocol(protoConf)
          serviceConfig.setRef(
            Utils
              .getInstance(definition.className, c => c.getDeclaredConstructor().newInstance()))

          serviceConfig.setInterface(
            definition.interfaceName.getOrElse(classOf[RestService].getCanonicalName))
          serviceConfig.setId(
            s"${definition.protocolName}:${definition.className}:${definition.version}")

          if (definition.version.isDefined) {
            serviceConfig.setVersion(definition.version.get)
          }

          logger.info(
            s"Created service with protocol name '${definition.protocolName}' " +
              s"class name '${definition.className}' and version '${definition.version.getOrElse(
                Cnsnts.EmptyString)}'")

          serviceConfig
        case None =>
          val exception = new IllegalArgumentException
          logger.error(
            s"No protocol was defined with name '${definition.protocolName}' " +
              s"for service with class name '${definition.protocolName}'",
            exception)
          throw exception
      }

    })

    serviceConfigs
  }

}
