package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/types/artifact.go#L66
 */
class TrivyPackage(
    val id: String?,
    val name: String,
    val version: String?,
    val release: String?,
    val epoch: Int?,
    val architecture: String?,

    val srcName: String?,
    val srcVersion: String?,
    val srcRelease: String?,
    val srcEpoch: Int?,

    // Only for RedHat
    val modularityLabel: String?,
    val buildInfo: TrivyBuildInfo?,

    val ref: String?,
    val indirect: Boolean?,
    val dependsOn: List<String>,

    val license: String?,
    val layer: TrivyLayer?,

    // Each package metadata have the file path, while the package from lock files does not have.
    val filePath: String?
) {

    lateinit var result: TrivyResult

    val vulnerabilities: List<TrivyDetectedVulnerability> by lazy {
        result.vulnerabilities.filter { vulnerability ->
            vulnerability.packageName == name && (vulnerability.installedVersion.isNullOrBlank() || version.isNullOrBlank() || vulnerability.installedVersion.startsWith(version))
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(javaClass)

        fun map(json: JsonObject?): TrivyPackage? {

            if (json == null) {
                return null
            }

            var id  = json.string("id")
            var name = json.string("Name")
            var version = json.string("Version")
            var release = json.string("Release")
            val epoch = json.int("Epoch")
            val architecture = json.string("Arch")
            val srcName = json.string("SrcName")
            val srcVersion = json.string("SrcVersion")
            val srcRelease = json.string("SrcRelease")
            val srcEpoch = json.int("SrcEpoch")

            val modularityLabel = json.string("Modularitylabel")
            val buildInfo = TrivyBuildInfo.map(json.obj("BuildInfo"))

            val ref = json.string("Ref")
            val indirect = json.boolean("Indirect")
            val dependsOn = json.array<String>("DependsOn") ?:  listOf()

            val license = json.string("License")
            val layer = TrivyLayer.map(json.obj("Layer"))
            val filePath = json.string("FilePath")

            if (name.isNullOrEmpty()) {
                name = srcName
            }

            if (name.isNullOrBlank()) {
                log.warn("Trivy package without name ${json.toJsonString()}")
                return null
            }

            if (version.isNullOrBlank()) {
                version = srcVersion
            }

            if (release.isNullOrBlank()) {
                release = srcRelease
            }

            return TrivyPackage(id, name, version, release, epoch, architecture, srcName, srcVersion, srcRelease, srcEpoch, modularityLabel, buildInfo, ref, indirect, dependsOn, license, layer, filePath)
        }
    }
}
    