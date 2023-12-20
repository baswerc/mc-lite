package com.materialcentral.container.repository

import org.geezer.io.ui.FontIcon
import com.materialcentral.container.registry.ContainerRegistry
import com.materialcentral.container.repository.ui.ContainerRepositoryUiController
import com.materialcentral.scan.ScanTargetSource
import org.geezer.db.Data
import org.geezer.io.ui.HasNameDescriptionIcon
import org.geezer.io.ui.Linkable
import org.geezer.system.runtime.RuntimeClock
import kotlin.reflect.KFunction

class ContainerRepository(
    var containerRegistryId: Long,
    override var name: String,
    override var description: String?,
    var baseContainerRepositoryId: Long?,
    var baseContainerRepository: Boolean,
    var imagesLastSynchronizedAt: Long?,
    var existsInRegistry: Boolean?,
    var latestImageUploadedAt: Long?,
    var active: Boolean,
    var addedAt: Long,
) : Data(), HasNameDescriptionIcon, ScanTargetSource {

    override val icon: FontIcon = Icon

    override val route: KFunction<*> = ContainerRepositoryUiController::getRepository

    constructor(registry: ContainerRegistry,  name: String = "", description: String? = null, existsInRegistry: Boolean? = false, latestImageUploadedAt: Long? = null, active: Boolean = true)
            : this(registry.id, name, description,  null, false, null, existsInRegistry, null,active, RuntimeClock.transactionAt )

    companion object {
        @JvmField
        val Icon = FontIcon("fa-docker", "f395", true)
    }
}