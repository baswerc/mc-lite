package com.materialcentral.container.oci.layout

import com.beust.klaxon.JsonObject
import com.materialcentral.container.oci.manifest.Platform
import com.materialcentral.container.oci.manifest.v2.Descriptor
import com.materialcentral.container.oci.manifest.v2.DescriptorParameters

class IndexManifest : Descriptor {

    val platform: Platform?

    constructor(parameters: DescriptorParameters, platform: Platform?) : super(parameters) {
        this.platform = platform
    }

    override fun addTo(json: JsonObject) {
        json["platform"] = platform?.toJson()
    }

    companion object {
        fun map(json: JsonObject?): IndexManifest? {
            if (json == null) {
                return null
            }

            val parameters = mapDescriptor(json, "manifest") ?: return null
            val platform = Platform.map(json.obj("platform"))

            return IndexManifest(parameters, platform)
        }
    }

}