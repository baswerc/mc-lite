package com.materialcentral.container.oci.configuration

import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import com.materialcentral.container.oci.manifest.v2.Descriptor

/**
 * https://github.com/opencontainers/image-spec/blob/main/config.md
 */
class Config : Jsonable {

    val json: JsonObject

    /**
     * The username or UID which is a platform-specific structure that allows specific control over which user the process run as. This acts as a default value to use when the value is not specified when creating a container. For Linux
     * based systems, all of the following are valid: user, uid, user:group, uid:gid, uid:group, user:gid. If group/gid is not specified, the default group and supplementary groups of the given user/uid in /etc/passwd from the container
     * are applied.
     */
    val user: String?

    /**
     * A set of ports to expose from a container running this image. Its keys can be in the format of: port/tcp, port/udp, port with the default protocol being tcp if not specified. These values act as defaults and are merged with any
     * specified when creating a container. NOTE: This JSON structure value is unusual because it is a direct JSON serialization of the Go type map[string]struct{} and is represented in JSON as an object mapping its keys to an empty object.
     */
    val exposedPorts: Map<String, JsonObject>?

    /**
     * Entries are in the format of VARNAME=VARVALUE. These values act as defaults and are merged with any specified when creating a container.
     */
    val environment: List<Pair<String, String>>?

    /**
     * A list of arguments to use as the command to execute when the container starts. These values act as defaults and may be replaced by an entrypoint specified when creating a container.
     */
    val entryPoints: List<String>?

    /**
     * Default arguments to the entrypoint of the container. These values act as defaults and may be replaced by any specified when creating a container. If an Entrypoint value is not specified, then the first entry of the Cmd array
     * SHOULD be interpreted as the executable to run.
     */
    val cmd: List<String>?

    /**
     * A set of directories describing where the process is likely to write data specific to a container instance. NOTE: This JSON structure value is unusual because it is a direct JSON serialization of the Go type map[string]struct{}
     * and is represented in JSON as an object mapping its keys to an empty object.
     */
    val volumes: Map<String, JsonObject>?

    /**
     * Sets the current working directory of the entrypoint process in the container. This value acts as a default and may be replaced by a working directory specified when creating a container.
     */
    val workingDir: String?

    /**
     * The field contains arbitrary metadata for the container. This property MUST use the annotation rules - https://github.com/opencontainers/image-spec/blob/main/annotations.md#rules.
     */
    val labels: List<Pair<String, String>>?

    /**
     * The field contains the system call signal that will be sent to the container to exit. The signal can be a signal name in the format SIGNAME, for instance SIGKILL or SIGRTMIN+3.
     */
    val stopSignal: String?

    val memory: Int?

    val memorySwap: Int?

    val cpuShares: Int?

    val healthCheck: JsonObject?

    constructor(json: JsonObject, user: String?, exposedPorts: Map<String, JsonObject>?, environment: List<Pair<String, String>>?, entryPoints: List<String>?, cmd: List<String>?, volumes: Map<String, JsonObject>?, workingDir: String?, labels: List<Pair<String, String>>?, stopSignal: String?, memory: Int?, memorySwap: Int?, cpuShares: Int?, healthCheck: JsonObject?) {
        this.json = json
        this.user = user
        this.exposedPorts = exposedPorts
        this.environment = environment
        this.entryPoints = entryPoints
        this.cmd = cmd
        this.volumes = volumes
        this.workingDir = workingDir
        this.labels = labels
        this.stopSignal = stopSignal
        this.memory = memory
        this.memorySwap = memorySwap
        this.cpuShares = cpuShares
        this.healthCheck = healthCheck
    }

    override fun toJson(): JsonObject = json

    companion object {
        fun parse(imageConfigurationJson: JsonObject): Config? {
            val configJson = imageConfigurationJson.obj("config") ?: return null

            val user = configJson.string("User")

            val exposedPorts = configJson.obj("ExposedPorts")?.let { exposedPortJson ->
                val exposedPorts = mutableMapOf<String, JsonObject>()
                for (property in exposedPortJson.keys) {
                    exposedPortJson.obj(property)?.let { value ->
                        exposedPorts[property] = value
                    }
                }
                exposedPorts
            }

            val environment = configJson.array<String>("Env")?.map {
                val nameValues = it.split("=")
                Pair(nameValues.first(), nameValues.getOrNull(1) ?: "")
            }

            val entrypoint = configJson?.array<String>("Entrypoint")
            val cmd = configJson?.array<String>("Cmd")

            val volumes = configJson?.obj("Volumes")?.let { volumeJson ->
                val volumes = mutableMapOf<String, JsonObject>()
                for (property in volumeJson.keys) {
                    volumeJson.obj(property)?.let { value ->
                        volumes[property] = value
                    }
                }
                volumes
            }

            val workingDir = configJson?.string("WorkingDir")
            val labels = Descriptor.mapAnnotations(configJson?.obj("Labels"))
            val stopSignal = configJson?.string("StopSignal")
            val memory = configJson?.int("Memory")
            val memorySwap = configJson?.int("MemorySwap")
            val cpuShares = configJson?.int("CpuShares")
            val healthCheck = configJson?.obj("Healthcheck")

            return Config(configJson, user,  exposedPorts, environment, entrypoint, cmd, volumes, workingDir, labels, stopSignal, memory, memorySwap, cpuShares, healthCheck)
        }
    }
}