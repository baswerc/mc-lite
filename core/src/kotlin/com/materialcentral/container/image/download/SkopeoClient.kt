package com.materialcentral.container.image.download

import com.materialcentral.container.image.ContainerImageCoordinates
import com.materialcentral.repository.container.registry.RegistryAuthenticationType
import com.materialcentral.repository.container.registry.client.authentication.AnonymousDockerHubAuthenticationProvider
import com.materialcentral.secret.SecretsCache
import org.geezer.system.runtime.OptionalStringProperty
import org.geezer.system.runtime.StringProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

object SkopeoClient {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    val cmd = StringProperty("SkopeoCmd", "skopeo")

    val os = OptionalStringProperty("SkopeoOverrideOs")

    fun copy(imageCoordinates: ContainerImageCoordinates, destinationProtocol: String, toDir: File) {

        val (registry, repository, image) = imageCoordinates

        val imagePath = imageCoordinates.imagePath
        val cmds = mutableListOf(cmd(), "copy")

        when (registry.authenticationType) {
            RegistryAuthenticationType.USERNAME_PASSWORD -> {
                val usernameSecret = registry.secretOneId?.let { SecretsCache[it] }
                val passwordSecret = registry.secretTwoId?.let { SecretsCache[it] }

                if (usernameSecret != null && passwordSecret != null) {
                    val username = usernameSecret.getValue()
                    if (!username.isNullOrBlank()) {
                        val password = passwordSecret.getValue()
                        if (!password.isNullOrBlank()) {
                            cmds.add("--src-creds")
                            cmds.add("$username:$password")
                        } else {
                            log.warn("Unable to retrieve password secret for container registry: ${registry}. Will attempt to download image: $imagePath with no credentials.")
                        }
                    } else {
                        log.warn("Unable to retrieve username secret for container registry: ${registry}. Will attempt to download image: $imagePath with no credentials.")
                    }
                } else {
                    log.warn("Container registry: ${registry.hostname} has username and password authentication defined but does not have a username and password secret. Will attempt to download image: $imagePath with no credentials.")
                }
            }

            RegistryAuthenticationType.BEARER_TOKEN -> {
                val bearerTokenSecret = registry.secretOneId?.let { SecretsCache[it] }
                if (bearerTokenSecret != null) {
                    val bearerToken = bearerTokenSecret.getValue()
                    if (!bearerToken.isNullOrBlank()) {
                        cmds.add("--src-registry-token")
                        cmds.add(bearerToken)
                    } else {
                        log.warn("Unable to retrieve bearer token secret for container registry: ${registry}. Will attempt to download image: $imagePath with no credentials.")
                    }
                } else {
                    log.warn("Container registry: ${registry.hostname} has bearer token authentication defined but does not have a bearer token secret. Will attempt to download image: $imagePath with no credentials.")
                }
            }

            RegistryAuthenticationType.DOCKERHUB_ANONYMOUS -> {
                val bearerToken = AnonymousDockerHubAuthenticationProvider.getBearerToken(imagePath)
                cmds.add("--src-registry-token")
                cmds.add(bearerToken)
            }

            RegistryAuthenticationType.ANONYMOUS -> {}
        }

        cmds.add("docker://${registry.hostname}/${repository.name}@${image.digest}")
        cmds.add("$destinationProtocol:${toDir.absolutePath}")

        val copyProcess = ProcessBuilder().command(cmds).inheritIO().redirectErrorStream(true).start()
        val copyProcessStatus = copyProcess.waitFor()
        if (copyProcessStatus != 0) {
            throw IOException("Skopeo copy image $imagePath failed with status $copyProcessStatus")
        }
    }
}