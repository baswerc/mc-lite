package com.materialcentral.container.registry

import org.geezer.HasNameDescription
import org.geezer.db.Data
import com.materialcentral.container.registry.client.authentication.AnonymousDockerHubAuthenticationProvider
import com.materialcentral.container.registry.client.authentication.BasicAuthenticationProvider
import com.materialcentral.container.registry.client.authentication.BearerTokenAuthenticationProvider
import com.materialcentral.container.registry.client.authentication.RegistryAuthenticationProvider
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.repository.container.ContainerName
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.repository.container.image.ContainerImage
import com.materialcentral.container.registry.ui.ContainerRegistryUiController
import com.materialcentral.container.repository.ContainerName
import com.materialcentral.container.repository.ContainerRepository
import com.materialcentral.secret.SecretsCache
import kotlin.reflect.KFunction

class ContainerRegistry(
    var type: ContainerRegistryType,
    var hostname: String,
    var ssl: Boolean,
    override var description: String?,
    var active: Boolean,
    var authenticationType: RegistryAuthenticationType,
    var secretOneId: Long?,
    var secretTwoId: Long?) : Data(), HasNameDescription, HasIcon, Linkable {

    override val name: String
        get() = hostname


    override val icon: FontIcon = Icon

    override val route: KFunction<*> = ContainerRegistryUiController::getRegistry

    fun createContainerName(repository: ContainerRepository): ContainerName {
        var repositoryPath = repository.name.removePrefix("/")

        if (authenticationType == RegistryAuthenticationType.DOCKERHUB_ANONYMOUS) {
            val index = repositoryPath.indexOf('/')
            if (index < 0) {
                repositoryPath = "library/$repositoryPath"
            }
        }

        return ContainerName("$hostname/$repositoryPath")
    }

    fun createContainerName(repository: ContainerRepository, image: ContainerImage): ContainerName {
        return ContainerName(createContainerName(repository), image.name)
    }

    fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        when (authenticationType) {
            RegistryAuthenticationType.USERNAME_PASSWORD -> {
                val usernameSecret = SecretsCache.find(secretOneId)
                if (usernameSecret == null) {
                    errors.add("A username secret is required.")
                } else if (!usernameSecret.hasValue) {
                    errors.add("The username secret value is not available.")
                }

                val passwordSecret = SecretsCache.find(secretTwoId)
                if (passwordSecret == null) {
                    errors.add("A password secret is required.")
                } else if (!passwordSecret.hasValue) {
                    errors.add("The password secret value is not available.")
                }
            }

            RegistryAuthenticationType.BEARER_TOKEN -> {
                val bearerTokenSecret = SecretsCache.find(secretOneId)
                if (bearerTokenSecret == null) {
                    errors.add("A bearer token secret is required.")
                } else if (!bearerTokenSecret.hasValue) {
                    errors.add("The bearer token secret value is not available.")
                }
            }

            else -> {}
        }
        return errors
    }

    fun getAuthenticationProvider(): RegistryAuthenticationProvider? {
        check(active) { "The container registry $hostname is not currently active." }

        val validationErrors = getValidationErrors()
        check(validationErrors.isEmpty()) { validationErrors.joinToString(" ")}

        return when (authenticationType) {
            RegistryAuthenticationType.USERNAME_PASSWORD -> {
                val username = SecretsCache.find(secretOneId)?.getValue()
                val password = SecretsCache.find(secretTwoId)?.getValue()
                if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                    BasicAuthenticationProvider(username, password)
                } else {
                    null
                }
            }

            RegistryAuthenticationType.BEARER_TOKEN -> {
                val bearerToken = SecretsCache.find(secretOneId)?.getValue()
                if (!bearerToken.isNullOrBlank()) {
                    BearerTokenAuthenticationProvider(bearerToken)
                } else {
                    null
                }
            }
            RegistryAuthenticationType.DOCKERHUB_ANONYMOUS -> AnonymousDockerHubAuthenticationProvider
            else -> null
        }
    }

    companion object {
        @JvmField
        val Icon = FontIcon("fa-server", "f233")
    }
}