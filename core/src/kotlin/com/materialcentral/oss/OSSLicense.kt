package com.materialcentral.oss

import org.geezer.HasDescription
import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class OSSLicense(override val id: Int, val primaryIdentifier: String, override val label: String, val primaryUrl: String, val identifiers: List<String>, val urls: List<String>, override val description: String) : DataEnum, HasDescription {
    GPL2(0, "GPL-2.0", "The 2-Clause BSD License", "https://opensource.org/licenses/GPL-2.0", listOf("GNU", "GPL", "General Public", "GPLv2", "GPLv2+"), listOf("https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"), ""),
    GPL3(1, "GPL-3.0", "The 3-Clause BSD License", "https://opensource.org/licenses/GPL-3.0", listOf("GNU", "GPL", "General Public", "GPLv3", "GPLv3+"), listOf("https://www.gnu.org/licenses/gpl-3.0.en.html"), ""),
    LGPL2(2, "LGPL-2.0", "GNU Library General Public License version 2", "https://opensource.org/licenses/LGPL-2.0", listOf("GNU", "LGPL", "Library General", "LGPL version 2"), listOf("https://www.gnu.org/licenses/old-licenses/lgpl-2.0.en.html"), ""),
    LGPL21(3, "LGPL-2.1", "GNU Lesser General Public License version 2.1", "https://opensource.org/licenses/LGPL-2.1", listOf("GNU", "LGPL", "Library General", "LGPL version 2.1"), listOf("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html"), ""),
    LGPL3(4, "LGPL-3.0", "GNU Lesser General Public License version 3", "https://opensource.org/licenses/LGPL-3.0", listOf("GNU", "LGPL", "Library General", "LGPL version 3"), listOf("https://www.gnu.org/licenses/lgpl-3.0.en.html"), ""),
    Apache2(5, "Apache-2.0", "Apache License, Version 2.0", "https://opensource.org/licenses/Apache-2.0", listOf("Apache"), listOf("https://www.apache.org/licenses/LICENSE-2.0", "http://www.apache.org/licenses/LICENSE-2.0.txt", "https://www.apache.org/licenses/LICENSE-2.0.txt"), ""),
    BSD2(6, "BSD-2-Clause", "The 2-Clause BSD License", "https://opensource.org/licenses/BSD-2-Clause", listOf("BSD", "Simplified BSD", "FreeBSD"), listOf(), ""),
    BSD3(7, "BSD-3-Clause", "The 3-Clause BSD License", "https://opensource.org/licenses/BSD-3-Clause", listOf("BSD", "New BSD", "Modified BSD"), listOf(), ""),
    MIT(8, "MIT", "The MIT License", "https://opensource.org/licenses/MIT", listOf("MIT"), listOf("https://www.mit.edu/~amini/LICENSE.md"), ""),
    CDDL(9, "CDDL-1.0", "Common Development and Distribution License 1.0", "https://opensource.org/licenses/CDDL-1.0", listOf("CDDL", "Common Development and Distribution"), listOf(), ""),
    MPL2(10, "MPL-2.0", "Mozilla Public License 2.0", "https://opensource.org/licenses/MPL-2.0", listOf("MPL", "Mozilla"), listOf("https://www.mozilla.org/en-US/MPL/2.0/"), ""),
    ECLIPSE1(11, "EPL-1.0", "Eclipse Public License version 1.0", "https://opensource.org/licenses/EPL-2.0", listOf("EPL-1.0", "Eclipse Public License version 1.0"), listOf("https://www.eclipse.org/legal/epl-v10.html"), ""),
    ECLIPSE2(12, "EPL-2.0", "Eclipse Public License version 2.0", "https://opensource.org/licenses/EPL-2.0", listOf("EPL-2.0", "Eclipse Public License version 2.0", "Eclipse"), listOf("https://www.eclipse.org/legal/epl-2.0/"), ""),
    MSPL(13, "MS-PL", "Microsoft Public License", "https://opensource.org/licenses/MS-PL", listOf("Microsoft"), listOf(), ""),
    PUBLIC_DOMAIN(14, "Public Domain", "Public Domain", "https://wiki.creativecommons.org/wiki/public_domain", listOf("Public Domain"), listOf(), ""),
    ;

    companion object : DataEnumType<OSSLicense> {
        override val enumValues: Array<OSSLicense> = values()
    }
}