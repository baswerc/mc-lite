package com.materialcentral.container.repository

import com.materialcentral.io.ui.MCUI
import org.geezer.io.ui.attribute
import com.materialcentral.job.Job
import com.materialcentral.job.JobInitializationParameters
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.TagConsumer
import kotlinx.html.div

class ContainerRepositorySynchronization(
    val containerRepositoryId: Long,
    val reevaluateLayers: Boolean,
    val synchronizeUntaggedImages: Boolean,
    val parameters: JobInitializationParameters
) : Job(parameters) {

    constructor(repository: ContainerRepository, reevaluateLayers: Boolean = false, synchronizeUntaggedImages: Boolean = false) :
            this(repository.id, reevaluateLayers, synchronizeUntaggedImages, JobInitializationParameters())

    override fun addAttributesAtStart(request: HttpServletRequest, html: TagConsumer<*>, columnSize: String, tableDetails: Boolean) {
        val coordinates = ContainerRepositoryCoordinates.getById(containerRepositoryId)

        html.div(columnSize) {
            html.attribute("Container Registry") {
                coordinates.registry.toLink(html, request)
            }
        }

        html.div(columnSize) {
            html.attribute("Container Repository") {
                if (MCUI.canRead(coordinates.repository, request)) {
                    coordinates.repository.toLink(html, request)
                } else {
                    coordinates.repository.toLabel(html)
                }
            }
        }
    }
}