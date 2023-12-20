package com.materialcentral.scan.analysis

import com.materialcentral.job.JobTerminatedError
import com.materialcentral.scan.Scan
import org.geezer.system.runtime.IntProperty
import org.geezer.system.runtime.RuntimeClock
import org.geezer.causeMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

abstract class AnalyzerBase : Analyzer {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    override val id: String
        get() = "$toolId-${targetType.readableId}"

    override val name: String
        get() = "$toolName ${targetType.label}"

    override val imagePath: String
        get() = "analyzers/${toolId}.png"

    fun runAnalyzerCommand(cmds: List<String>, scan: Scan, timeoutSeconds: Int? = null, fromDirectory: File? = null, expectedResponse: Int? = 0) {
        if (cmds.isEmpty()) {
            throw IllegalStateException("No commands to run for analyzer ${javaClass.kotlin.simpleName}.")
        }

        log.info("Executing trivy cmd: ${cmds.joinToString(" ")}")
        val builder = ProcessBuilder().command(cmds).inheritIO().redirectErrorStream(true)
        if (fromDirectory != null) {
            builder.directory(fromDirectory)
        }

        val process = builder.start()
        val processStartedAt = RuntimeClock.now
        val maxSeconds = timeoutSeconds ?: defaultMaxProcessSeconds()

        while (true) {
            if (process.waitFor(pollSecondsForProcess().toLong(), TimeUnit.SECONDS)) {
                val exitValue = process.exitValue()
                if (expectedResponse != null && exitValue != expectedResponse) {
                    scan.failed("Analyzer process ${cmds.firstOrNull()} returned unexpected exit value ${exitValue}.")
                    throw JobTerminatedError()
                } else {
                    return
                }
            } else {
                scan.checkpoint()
                if (RuntimeClock.secondsAgo(processStartedAt) > maxSeconds) {
                    scan.failed("Analyzer process ${cmds.firstOrNull()} timed out after $maxSeconds seconds.")
                    try {
                        process.destroyForcibly()
                    } catch (e: Exception) {
                        log.warn("Unable to destroy analyzer process: ${cmds.firstOrNull()} due to: ${e.causeMessage}", e)
                    }
                    throw JobTerminatedError()
                }
            }
        }
    }

    companion object {
        val defaultMaxProcessSeconds = IntProperty("AnalyzerProcessDefaultMaxSeconds", 60 * 60 * 1)

        val pollSecondsForProcess = IntProperty("AnalyzerProcessPollSeconds", 5)
    }
}