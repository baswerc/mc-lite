package com.materialcentral.container.oci.manifest

import org.geezer.json.Jsonable

interface ManifestLayer : Jsonable {
    /**
     * The layer index.
     */
    val index: Int

    /**
     * The content type of the layer blob.
     */
    val mediaType: String?

    /**
     * The SHA256 of the layer download blob.
     */
    val digest: String

    /**
     * The download size of the layer.
     */
    val size: Long?

    companion object {
        // https://github.com/opencontainers/image-spec/blob/main/layer.md
        const val EmptyLayerSize = 1024L


        val TarMediaTypes = listOf("application/vnd.oci.image.layer.v1.tar", "application/vnd.oci.image.layer.nondistributable.v1.tar")

        val GzipMediaTypes = listOf("application/vnd.oci.image.layer.v1.tar+gzip", "application/vnd.docker.image.rootfs.diff.tar.gzip", "application/vnd.oci.image.layer.nondistributable.v1.tar+gzip")

        val ZstdMediaTypes = listOf("application/vnd.oci.image.layer.v1.tar+zstd", "application/vnd.oci.image.layer.nondistributable.v1.tar+zstd")
    }

}