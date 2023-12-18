package com.materialcentral.container.image

import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.os.Architecture
import com.materialcentral.os.LinuxDistribution
import com.materialcentral.os.OperatingSystemType
import com.materialcentral.container.image.ui.ContainerImageUiController
import com.materialcentral.container.repository.ContainerRepository
import org.geezer.HasName
import org.geezer.db.Data
import org.geezer.io.ui.HasNameIcon
import org.geezer.system.runtime.RuntimeClock
import kotlin.reflect.KFunction

class ContainerImage(
    val containerRepositoryId: Long,
    val digest: String,
    override var name: String,
    var createdAt: Long,
    var baseContainerImageId: Long?,
    var baseImage: Boolean,
    var os: OperatingSystemType?,
    var osVersion: String?,
    var linuxDistribution: LinuxDistribution?,
    var architecture: Architecture?,
    var bytesSize: Long?,
    var latestInRepository: Boolean,
    var deletedFromRepository: Boolean
) : Data(), Linkable, HasNameIcon {

    override val route: KFunction<*> = ContainerImageUiController::getImage

    override val icon: FontIcon = Icon

    override val linkShortName: String
        get() {
            var shortName = name
            val index = shortName.indexOf('/')
            if (index > 0) {
                shortName = shortName.substring(index + 1)
            }
            return shortName
        }

    constructor(repository: ContainerRepository, digest: String, createdAt: Long = RuntimeClock.now) :
            this(repository.id, digest, shortenDigest(digest), createdAt, null, false, null, null, null, null, null, false, false)


    companion object {
        @JvmField
        val Icon = FontIcon("fa-layer-group", "f5fd")

        fun shortenDigest(digest: String): String {
            if (digest.startsWith("sha256:")) {
                val shortened = digest.removePrefix("sha256:")
                if (shortened.length > 7) {
                    return "sha256:${shortened.substring((shortened.length - 7), shortened.length)}"
                }
            }

            return digest
        }
    }
}