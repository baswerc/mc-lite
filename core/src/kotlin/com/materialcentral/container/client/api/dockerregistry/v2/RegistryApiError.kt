package com.materialcentral.container.client.api.dockerregistry.v2

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import org.geezer.toJsonObjectOrBust
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://docs.docker.com/registry/spec/api/#errors
 */
class RegistryApiError {
    val json: JsonObject

    val code: RegistryApiErrorCode

    val message: String

    val details: JsonArray<String>?

    constructor(json: JsonObject, code: RegistryApiErrorCode, message: String, details: JsonArray<String>?) {
        this.json = json
        this.code = code
        this.message = message
        this.details = details
    }

    override fun toString(): String {
        return json.toJsonString()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun parse(jsonString: String?): List<RegistryApiError> {
            if (jsonString.isNullOrBlank()) {
                return listOf()
            }

            val json = try {
                jsonString.toJsonObjectOrBust()
            } catch (e: Exception) {
                return listOf()
            }

            val errorsJson = json.array<JsonObject>("errors")
            val errors = mutableListOf<RegistryApiError>()
            if (errorsJson != null) {
                for (errorJson in errorsJson) {
                    val code = errorJson.string("code")
                    val registryCode = if (code.isNullOrBlank()) {
                        log.warn("Registry returned no error code property in errors property.")
                        RegistryApiErrorCode.UNKNOWN
                    } else {
                        val registryErrorCode = RegistryApiErrorCode.fromCode(code)
                        if (registryErrorCode == RegistryApiErrorCode.UNKNOWN) {
                            log.warn("Registry returned unknown error code $code.")
                        }
                        registryErrorCode
                    }

                    val message = errorJson.string("message") ?: ""
                    val detail = errorJson.array<String>("detail")
                    errors.add(RegistryApiError(errorJson, registryCode, message, detail))
                }
            }
            return errors
        }
    }
}