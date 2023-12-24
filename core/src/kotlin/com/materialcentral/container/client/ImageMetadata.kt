package com.materialcentral.container.client

import com.materialcentral.container.oci.configuration.ImageConfiguration
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.Platform

/**
 * An image tagged in its container repository. Tags are the only (standard) mechanism for discovering images published to a repository.
 */
class ImageMetadata(
    val manifest: Manifest,
    val tags: List<TagMetadata>,
    val architecture: String?,
    val os: String?,
    val osVersion: String?,
    val includedInMultiArchitectureManifest: Boolean,
    val configuration: ImageConfiguration?

) {

    class Builder(val manifest: Manifest) {

        private var includedInMultiArchitectureManifest: Boolean = false

        private var architecture: String? = null

        private var os: String? = null

        private var osVersion: String? = null

        private var configuration: ImageConfiguration? = null

        private val tags = mutableListOf<TagMetadata>()

        fun withPlatform(platform: Platform): Builder {
            addArchitecture(platform.architecture).addOs(platform.os).addOsVersion(platform.osVersion)
            includedInMultiArchitectureManifest = true
            return this
        }

        fun addTag(tag: TagMetadata): Builder {
            if (tags.none { it.value == tag.value }) {
                tags.add(tag)
            }
            return this
        }

        fun addArchitecture(architecture: String?): Builder {
            if (!architecture.isNullOrBlank()) {
                this.architecture = architecture
            }
            return this
        }

        fun addOs(os: String?): Builder {
            if (!os.isNullOrBlank()) {
                this.os = os
            }
            return this
        }

        fun addOsVersion(osVersion: String?): Builder {
            if (!osVersion.isNullOrBlank()) {
                this.osVersion = osVersion
            }
            return this
        }

        fun addConfiguration(configuration: ImageConfiguration?): Builder {
            if (configuration != null) {
                this.configuration = configuration
                addArchitecture(configuration.architecture).addOs(configuration.os).addOsVersion(configuration.osVersion)
            }
            return this
        }

        fun build(): ImageMetadata {
            val tagsList = tags.sortedBy { it.value }
            return ImageMetadata(manifest, tagsList, architecture, os, osVersion, includedInMultiArchitectureManifest, configuration)
        }
    }
}