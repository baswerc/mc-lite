package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/types/license.go#L7
 */
class TrivyDetectedLicense(
    val severity: String?,
    val category: TrivyLicenseCategory?,
    val packageName: String?,
    val filePath: String?,
    val name: String?,
    val confidence: Double?,
    val link: String?
) {
    companion object {
        fun map(json: JsonObject): TrivyDetectedLicense {
            val severity = json.string("Severity")
            val category = TrivyLicenseCategory.map(json.string("Category"))
            val packageName = json.string("PkgName")
            val filePath = json.string("FilePath")
            val name = json.string("Name")
            val confidence = json.double("Confidence")
            val link = json.string("link")
            return TrivyDetectedLicense(severity, category, packageName, filePath, name, confidence, link)
        }
    }
}