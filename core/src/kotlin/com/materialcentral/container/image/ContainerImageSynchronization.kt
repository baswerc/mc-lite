package com.materialcentral.container.image

import com.materialcentral.io.ui.MCUI
import org.geezer.io.ui.UI
import org.geezer.io.ui.attribute
import com.materialcentral.job.Job
import com.materialcentral.job.JobInitializationParameters
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.TagConsumer
import kotlinx.html.div

class ContainerImageSynchronization(
    val containerImageId: Long,
    val parameters: JobInitializationParameters
) : Job(parameters) {

    constructor(image: ContainerImage) : this(image.id, JobInitializationParameters())

    override fun addAttributesAtStart(request: HttpServletRequest, html: TagConsumer<*>, columnSize: String, tableDetails: Boolean) {
        val coordinates = ContainerImageCoordinates.getById(containerImageId)

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

        html.div(columnSize) {
            html.attribute("Container Image") {
                if (MCUI.canRead(coordinates.repository, request)) {
                    coordinates.image.toLink(html, request)
                } else {
                    coordinates.image.toLabel(html)
                }
            }
        }
    }

}