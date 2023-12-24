package com.materialcentral.container.client.api.dockerregistry.v2

/**
 * https://docs.docker.com/registry/spec/api/#errors-2
 */
enum class RegistryApiErrorCode(val message: String, val description: String) {
    BLOB_UNKNOWN("blob unknown to registry", "This error may be returned when a blob is unknown to the registry in a specified repository. This can be returned with a standard get or if a manifest references an unknown layer during upload."),
    BLOB_UPLOAD_INVALID("blob upload invalid", "The blob upload encountered an error and can no longer proceed."),
    BLOB_UPLOAD_UNKNOWN("blob upload unknown to registry", "If a blob upload has been cancelled or was never started, this error code may be returned."),
    DIGEST_INVALID("provided digest did not match uploaded content", "When a blob is uploaded, the registry will check that the content matches the digest provided by the client. The error may include a detail structure with the key “digest”, including the invalid digest string. This error may also be returned when a manifest includes an invalid layer digest."),
    MANIFEST_BLOB_UNKNOWN("blob unknown", "to registry	This error may be returned when a manifest blob is unknown to the registry."),
    MANIFEST_INVALID("manifest invalid", "During upload, manifests undergo several checks ensuring validity. If those checks fail, this error may be returned, unless a more specific error is included. The detail will contain information the failed validation."),
    MANIFEST_UNKNOWN("manifest unknown", "This error is returned when the manifest, identified by name and tag is unknown to the repository."),
    MANIFEST_UNVERIFIED("manifest failed signature verification", "During manifest upload, if the manifest fails signature verification, this error will be returned."),
    NAME_INVALID("invalid repository name", "Invalid repository name encountered either during manifest validation or any API operation."),
    NAME_UNKNOWN("repository name not known to registry", "This is returned if the name used during an operation is unknown to the registry."),
    SIZE_INVALID("provided length did not match content length", "When a layer is uploaded, the provided size will be checked against the uploaded content. If they do not match, this error will be returned."),
    TAG_INVALID("manifest tag did not match", "URI	During a manifest upload, if the tag in the manifest does not match the uri tag, this error will be returned."),
    UNAUTHORIZED("authentication required", "The access controller was unable to authenticate the client. Often this will be accompanied by a Www-Authenticate HTTP response header indicating how to authenticate."),
    DENIED("requested access to the resource is denied", "The access controller denied access for the operation on a resource."),
    UNSUPPORTED("The operation is unsupported.", "The operation was unsupported due to a missing implementation or invalid set of parameters."),
    TOO_MANY_REQUESTS("too many requests", "Returned when a client attempts to contact a service too many times."),
    UNKNOWN("An unknown error code was returned.", "The registry returned an error code not currently mapped.");

    companion object {
        fun fromCode(code: String): RegistryApiErrorCode = values().firstOrNull { it.toString() == code } ?: UNKNOWN
    }
}