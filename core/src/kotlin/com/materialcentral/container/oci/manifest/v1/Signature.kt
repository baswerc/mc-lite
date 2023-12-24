package com.materialcentral.container.oci.manifest.v1

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import org.geezer.json.Jsonable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://docs.docker.com/registry/spec/manifest-v2-1/#signed-manifests
 */
class Signature : Jsonable {
    /**
     * A JOSE header.
     * https://datatracker.ietf.org/doc/html/rfc7515#section-4
     */
    val header: JsonObject

    /**
     * A signature for the image manifest, signed by a libtrust private key.
     */
    val signature: String

    /**
     * The signed protected header.
     */
    val protected: String

    constructor(header: JsonObject, signature: String, protected: String) {
        this.header = header
        this.signature = signature
        this.protected = protected
    }

    override fun toJson(): JsonObject = json {
        obj(
            "header" to header,
            "signature" to signature,
            "protected" to protected,
        )
    }

    companion object {
        fun parseSignatures(manifestJson: JsonObject): Either<String, List<Signature>> {
            val signatures = mutableListOf<Signature>()

            for (signatureJson in manifestJson.array<JsonObject>("signatures") ?: listOf()) {
                val header = manifestJson.obj("header")
                if (header == null) {
                    return "Manifest signature had no header object.".left()
                }

                val signature = manifestJson.string("signature")
                if (signature.isNullOrBlank()) {
                    return "Manifest signature object had no signature property.".left()
                }

                val protected = manifestJson.string("protected")
                if (protected.isNullOrBlank()) {
                    return "Manifest signature object had no protected property.".left()
                }

                signatures.add(Signature(header, signature, protected))
            }

            return signatures.right()
        }
    }
}