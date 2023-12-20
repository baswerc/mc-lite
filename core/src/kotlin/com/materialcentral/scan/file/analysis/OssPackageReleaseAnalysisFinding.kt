package com.materialcentral.scan.file.analysis

import com.materialcentral.oss.PackageType
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.analysis.AnalysisFinding

class OssPackageReleaseAnalysisFinding(
    val packageType: PackageType,
    val packageName: String,
    val packageVersion: String,
    val sizeBytes: Long?,
    val md5Digest: ByteArray?,
    val sha1Digest: ByteArray?,
    val sha256Digest: ByteArray?,
    filePath: String?,
    layerId: String?,
    analyzerId: String

) : FileAnalysisFinding(filePath, layerId, analyzerId, null) {

    override val findingType: FindingType = FindingType.OSS_PACKAGE_RELEASE

    override fun associatedWithSameScanFinding(analysisFinding: AnalysisFinding): Boolean {
        return super.associatedWithSameScanFinding(analysisFinding) && (analysisFinding is OssPackageReleaseAnalysisFinding) && (packageType == analysisFinding.packageType)
                && (packageName == analysisFinding.packageName) && (packageVersion == analysisFinding.packageVersion)
    }
}
