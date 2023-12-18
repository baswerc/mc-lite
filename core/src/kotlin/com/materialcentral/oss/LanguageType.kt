package com.materialcentral.oss

import org.geezer.db.DataEnumType
import org.geezer.db.ReadableDataEnum
import org.geezer.io.ui.FontIcon

enum class LanguageType(override val id: Int, override val readableId: String, override val label: String, val packageTypes: Set<PackageType>, val icon: FontIcon) : ReadableDataEnum {
    JVM(0, "jvm", "JVM Languages", setOf(PackageType.MAVEN_MODULE), PackageType.MAVEN_MODULE.icon),
    JAVASCRIPT(1, "javascript", "Javascript", setOf(PackageType.NPM_MODULE), PackageType.NPM_MODULE.icon),
    PYTHON(2, "pyhton", "Python", setOf(PackageType.PYPI), PackageType.PYPI.icon),
    RUBY(3, "ruby", "Javascript", setOf(PackageType.NPM_MODULE), PackageType.NPM_MODULE.icon),
    DOT_NET(4, "dot-net", ".NET Languages", setOf(PackageType.NUGET), PackageType.NUGET.icon),
    GO(5, "go", "Go", setOf(PackageType.GOLANG), PackageType.GOLANG.icon),
    RUST(6, "rust", "Rust", setOf(PackageType.CARGO), PackageType.CARGO.icon),
    IOS(7, "ios", "iOS Languages", setOf(PackageType.COCOA_PODS, PackageType.SWIFT), PackageType.COCOA_PODS.icon),
    CPP(8, "c", "C/C++", setOf(PackageType.CONAN), PackageType.CONAN.icon);

    companion object : DataEnumType<LanguageType> {
        override val dataEnumValues: Array<LanguageType> = enumValues()

    }
}