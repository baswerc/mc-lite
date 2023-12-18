package com.materialcentral.container.image.metadata

class ContainerImageOssPackageRelease(
    val ossPackageReleaseId: Long,
    override val filePathId: Long?,
    val sizeBytes: Long?,
    val md5DigestId: Long?,
    val sha1DigestId: Long?,
    val sha256DigestId: Long?,
    var criticalFindings: Int?,
    var highFindings: Int?,
    var mediumFindings: Int?,
    var lowFindings: Int?,
    containerImageId: Long,
    inheritedMetadata: Boolean
) : ContainerImageMetadata(containerImageId, inheritedMetadata) {
}