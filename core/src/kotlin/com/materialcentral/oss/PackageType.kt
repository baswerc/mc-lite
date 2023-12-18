package com.materialcentral.oss

import com.github.packageurl.MalformedPackageURLException
import com.github.packageurl.PackageURL
import org.geezer.HasReadableId
import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType
import org.geezer.io.ui.FontIcon
import com.materialcentral.os.OperatingSystemType

/**
 * https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst
 */
enum class PackageType(override val id: Int,
                       override val label: String,
                       val purlType: String,
                       val icon: FontIcon,
                       val namespaceDelimitter: String = "@",
                       override val readableId: String = purlType) : DataEnum, HasReadableId {
    GENERIC(0, "Generic", "generic", FontIcon("fa-box", "f466")),

    RPM(1, "RPM", "rpm", OperatingSystemType.LINUX.icon),

    ALPINE(2, "Alpine Linux Package", "alpine", FontIcon("", "")),

    APK(3, "APK", "apk", OperatingSystemType.LINUX.icon),

    ARCH(4, "Arch Linux Package", "arch", OperatingSystemType.LINUX.icon),

    DEB(5, "Debian", "deb", FontIcon("fa-debian", "e60b", true)),

    RUBY_GEM(6, "Rubygem", "gem", FontIcon("fa-gem", "f3a5")),

    GOLANG(7, "Go Package", "golang", FontIcon("fa-golang", "e40f", true)),

    NPM_MODULE(8, "NPM Module", "npm", FontIcon("fa-npm", "f3d4", true)),

    MAVEN_MODULE(9, "Maven Module", "maven", FontIcon("fa-java", "f4e4", true), namespaceDelimitter = ":"),

    PYPI(10, "Pypi Package","pypi", FontIcon("fa-python", "f3e2", true)),

    NUGET(11, "Nuget .NET Package", "nuget", FontIcon("fa-windows", "f17a", true)),

    COMPOSER(12, "Composer PHP Package", "composer", FontIcon("fa-php", "f457", true)),

    CARGO(13, "Cargo", "cargo", FontIcon("fa-rust", "e07a", true)),

    COCOA_PODS(14, "CocoaPods", "cocoapods", FontIcon("fa-apple", "f179", true)),

    SWIFT(15, "Swift", "swift", FontIcon("fa-apple", "f179", true)),

    CONAN(16, "Conan", "conan", FontIcon("fa-c", "43"));

    fun splitFullName(fullName: String): Pair<String?, String> {
        val fullName = fullName.trim()
        val index = fullName.indexOf(namespaceDelimitter)
        return if (index > 0 && index < fullName.length - 1) {
            fullName.substring(0, index) to fullName.substring(index + 1, fullName.length)
        } else {
            null to fullName
        }
    }

    fun createFullName(namespace: String?, name: String): String {
        return if (namespace.isNullOrBlank()) {
            name
        } else {
            "${namespace.trim()}$namespaceDelimitter${name.trim()}"
        }
    }

    companion object : DataEnumType<PackageType> {
        override val dataEnumValues: Array<PackageType> = values()

        fun fromPurl(purl: String?): PackageType? {
            return try {
                fromPurlType(PackageURL(purl).type)
            } catch (e: MalformedPackageURLException) {
                null
            }
        }

        fun fromPurlType(type: String?): PackageType? = values().firstOrNull { it.purlType.equals(type, true) }
    }
}