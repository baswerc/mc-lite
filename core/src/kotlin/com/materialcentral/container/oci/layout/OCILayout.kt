package com.materialcentral.container.oci.layout

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import org.geezer.json.Jsonable
import org.geezer.causeMessage
import org.geezer.toJsonObjectOrBust
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OCILayout(val imageLayoutVersion: String = Version) : Jsonable {
    override fun toJson(): JsonObject = json {
        obj(
            "imageLayoutVersion" to imageLayoutVersion
        )
    }

    companion object {
        const val Version = "1.0.0"

        fun parse(json: String?): Either<String, OCILayout> {
            if (json.isNullOrBlank()) {
                return "No OCI layout content.".left()
            }

            val json = try {
                json.toJsonObjectOrBust()
            } catch (e: Exception) {
                return "Cannot parse OCI layout JSON: $json due to: ${e.causeMessage}".left()
            }

            val imageLayoutVersion = json.string("imageLayoutVersion")
            if (imageLayoutVersion.isNullOrBlank()) {
                return "OCI layout has no imageLayoutVersion property in JSON: $json".left()
            }

            return OCILayout(imageLayoutVersion).right()
        }
    }
}