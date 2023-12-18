package com.materialcentral.container.repository

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun String.toContainerName(): ContainerName = ContainerName(this)

/**
 * https://docs.microsoft.com/en-us/azure/container-registry/container-registry-concepts
 * https://cloud.google.com/artifact-registry/docs/docker/names
 */
class ContainerName {
    val name: String

    val registryProtocol: String?

    val registryHostname: String?

    val namespace: String?

    val repository: String

    val tag: String?

    val hasTag: Boolean
        get() = tag != null

    val digest: String?

    val hasDigest: Boolean
        get() = digest != null

    val tagOrDigest: String
        get() = tag ?: digest ?: "latest"

    val hasTagOrDigest: Boolean
        get() = hasTag || hasDigest

    val namespaceRepository: String
        get() = if (namespace != null) "$namespace/$repository" else repository

    val registryNamespaceRepository: String
        get() = if (registryHostname != null) "$registryHostname/$namespaceRepository" else namespaceRepository

    constructor(registryProtocol: String?, registryHostname: String?, namespace: String?, repository: String, tag: String?, digest: String?) {
        if (!tag.isNullOrBlank() && !digest.isNullOrBlank()) {
            throw IllegalArgumentException("A ContainerName cannot have a tag and digest.")
        }

        this.registryProtocol = registryProtocol
        this.registryHostname = registryHostname
        this.namespace = namespace
        this.repository = repository
        this.tag = tag
        this.digest = digest

        var name = registryNamespaceRepository
        if (hasTag) {
            name += ":$tag"
        } else if (hasDigest) {
            name += "@$digest"
        }
        this.name = name
    }

    constructor(registryHostname: String, repositoryName: String) {
        registryProtocol = null
        this.registryHostname = registryHostname

        var repositoryName = repositoryName
        var index = repositoryName.indexOf(":")
        if (index > 0) {
            repositoryName = repositoryName.substring(0, index)
            tag = repositoryName.substring(index + 1, repositoryName.length)
            digest = null
        } else {
            tag = null
            index = repositoryName.indexOf('@')
            if (index > 0) {
                repositoryName = repositoryName.substring(0, index)
                digest = repositoryName.substring(index + 1, repositoryName.length)
            } else {
                digest = null
            }
        }

        index = repositoryName.lastIndexOf('/')
        if (index > 0) {
            namespace = repositoryName.substring(0, index).removePrefix("/")
            repository = repositoryName.substring(index + 1, repositoryName.length)

        } else {
            namespace = null
            repository = repositoryName.removePrefix("/")
        }

        var name = registryNamespaceRepository
        if (hasTag) {
            name += ":$tag"
        } else if (hasDigest) {
            name += "@$digest"
        }
        this.name = name
    }

    constructor(repositoryName: ContainerName, tagOrDigest: String) {
        registryProtocol = repositoryName.registryProtocol
        registryHostname = repositoryName.registryHostname
        namespace = repositoryName.namespace
        repository = repositoryName.repository
        if (canBeTag(tagOrDigest)) {
            tag = tagOrDigest
            digest = null
            name = "${repositoryName.registryNamespaceRepository}:$tag"
        } else if (canBeDigest(tagOrDigest)) {
            tag = null
            digest = tagOrDigest
            name = "${repositoryName.registryNamespaceRepository}@$digest"
        } else {
            throw IllegalArgumentException("The provided tagOrDigest value cannot be a tag or digest.")
        }
    }

    constructor(name: String) {
        this.name = name
        val index = name.lastIndexOf('/')
        if (index > 0) {
            val repositoryImageCandidate = name.substring(index + 1, name.length)

            val digestIndex = repositoryImageCandidate.indexOf('@')
            if (digestIndex > 0) {
                repository = repositoryImageCandidate.substring(0, digestIndex).removePrefix("/")
                digest = repositoryImageCandidate.substring(digestIndex + 1, repositoryImageCandidate.length)
                tag = null
            } else {
                digest = null
                val tagIndex = repositoryImageCandidate.indexOf(':')
                if (tagIndex > 0) {
                    repository = repositoryImageCandidate.substring(0, tagIndex).removePrefix("/")
                    tag = repositoryImageCandidate.substring(tagIndex + 1, repositoryImageCandidate.length)
                } else {
                    repository = repositoryImageCandidate.removePrefix("/")
                    tag = null
                }
            }

            if (name.startsWith("/")) {
                namespace = name.substring(1, index)
                registryProtocol = null
                registryHostname = null
            } else {
                var registryNamespace = name.substring(0, index)
                if (registryNamespace.isBlank()) {
                    registryProtocol = null
                    registryHostname = null
                    namespace = null
                } else {
                    val protocolIndex = registryNamespace.indexOf("://")
                    if (protocolIndex > 0) {
                        registryProtocol = registryNamespace.substring(0, protocolIndex)
                        registryNamespace = registryNamespace.substring(protocolIndex + 3, registryNamespace.length)
                    } else {
                        registryProtocol = null
                    }

                    val registryNamespaceHierarchy = registryNamespace.split("/").filter { it.isNotBlank() }
                    if (registryNamespaceHierarchy.isEmpty()) {
                        registryHostname = null
                        namespace = null
                    } else if (registryNamespaceHierarchy.size == 1) {
                        val namespaceCandidate = registryNamespaceHierarchy[0]
                        if (canByNamespaceOrRepository(namespaceCandidate)) {
                            registryHostname = null
                            namespace = namespaceCandidate.removePrefix("/")
                        } else {
                            registryHostname = namespaceCandidate
                            namespace = null
                        }
                    } else {
                        registryHostname = registryNamespaceHierarchy[0]
                        namespace = registryNamespaceHierarchy.subList(1, registryNamespaceHierarchy.size).joinToString("/").removePrefix("/")
                    }
                }
            }
        } else {
            registryProtocol = null
            registryHostname = null
            namespace = null

            val digestIndex = name.indexOf('@')
            if (digestIndex > 0) {
                repository = name.substring(0, digestIndex)
                digest = name.substring(digestIndex + 1, name.length)
                tag = null
            } else {
                digest = null
                val tagIndex = name.indexOf(':')
                if (tagIndex > 0) {
                    repository = name.substring(0, tagIndex)
                    tag = name.substring(tagIndex + 1, name.length)
                } else {
                    repository = name
                    tag = null
                }
            }
        }

    }

    fun withTag(tag: String): ContainerName = ContainerName(registryProtocol, registryHostname, namespace, repository, tag, null)

    fun withDigest(digest: String): ContainerName = ContainerName(registryProtocol, registryHostname, namespace, repository, null, digest)

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContainerName

        if (registryHostname != other.registryHostname) return false
        if (namespace != other.namespace) return false
        if (repository != other.repository) return false
        if (tag != other.tag) return false
        if (digest != other.digest) return false

        return true
    }

    override fun hashCode(): Int {
        var result = registryHostname?.hashCode() ?: 0
        result = 31 * result + (namespace?.hashCode() ?: 0)
        result = 31 * result + repository.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + (digest?.hashCode() ?: 0)
        return result
    }


    companion object {
        val log: Logger = LoggerFactory.getLogger(javaClass)

        /**
         * https://docs.docker.com/docker-hub/repos/
         * The repository name needs to be unique in that namespace, can be two to 255 characters, and can only contain lowercase letters, numbers, hyphens (-), and underscores (_).
         */
        fun canByNamespaceOrRepository(repositoryOrNamespace: String, type: String = "repository"): Boolean {
            return if (repositoryOrNamespace.isEmpty()) { // Microsoft has one character repository names - planetary-computer/r
                log.info("The $type name $repositoryOrNamespace must be a minimum of two characters.")
                false
            } else if (repositoryOrNamespace.length > 255) {
                log.info("The $type name $repositoryOrNamespace cannot be longer than 255 characters.")
                false
            } else {
                var validName = true
                for (char in repositoryOrNamespace.toCharArray()) {
                    // Microsoft seems to put '.' in some of their repository names ?
                    if (!char.isLetterOrDigit() && char != '-' && char != '_' && (type != "namespace" || char != '/') && char != '.') {
                        log.info("The $type name $repositoryOrNamespace cannot contain the '$char' character ($repositoryOrNamespace).")
                        validName = false
                        break
                    }
                }
                validName
            }
        }

        /**
         * https://docs.docker.com/engine/reference/commandline/tag/
         * A tag name must be valid ASCII and may contain lowercase and uppercase letters, digits, underscores, periods and dashes. A tag name may not start with a period or a dash and may contain a maximum of 128 characters.
         */
        fun canBeTag(tag: String): Boolean {
            return if (tag.isBlank()) {
                log.info("A tag cannot be empty.")
                false
            } else if (tag.length > 128) {
                log.info("Tag $tag cannot be longer than 128 characters.")
                false
            } else if (tag.startsWith(".")) {
                log.info("Tag $tag cannot start with a '.' character")
                false
            } else if (tag.startsWith("-")) {
                log.info("Tag $tag cannot start with a '-' character")
                false
            } else {
                var validTag = true
                for (char in tag.toCharArray()) {
                    if (!char.isLetterOrDigit() && char != '_' && char != '.' && char != '-') {
                        log.info("Tag $tag cannot contain a '$char' character")
                        validTag = false
                        break
                    }
                }
                validTag
            }
        }

        /**
         * https://windsock.io/explaining-docker-image-ids/
         */
        val ValidHexValues = listOf('A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        fun canBeDigest(digest: String): Boolean {
            val algorithmIndex = digest.indexOf(':')
            if (algorithmIndex <= 0) {
                log.info("A colon character ':' must be contained in the digest $digest.")
                return false
            }

            val hexDigest = digest.substring(algorithmIndex + 1, digest.length)
            for (char in hexDigest.toCharArray()) {
                if (!ValidHexValues.contains(char)) {
                    log.info("The digest $digest cannot contain the '$char' character.")
                    return false
                }
            }

            return true
        }
    }
}