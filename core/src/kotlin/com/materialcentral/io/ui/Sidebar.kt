package com.materialcentral.io.ui

import com.materialcentral.MaterialGroupType
import com.materialcentral.job.Job
import com.materialcentral.job.ui.JobsUiController
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.repository.container.ui.ContainerRepositoryUiController
import com.materialcentral.user.UserSession
import com.materialcentral.user.authorization.Role
import com.materialcentral.user.session.UserSession
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.stream.createHTML
import kotlinx.html.ul
import org.geezer.io.ui.sidebar.SidebarHeader
import org.geezer.io.ui.sidebar.SidebarItem
import org.geezer.io.ui.sidebar.SidebarLink

class Sidebar(val items: List<SidebarItem>) {

    fun toHTML(request: HttpServletRequest): String {
        return createHTML(prettyPrint = true, xhtmlCompatible = true).ul("side-nav") {
            for (item in items) {
                item.addHtml(this, request)
            }
        }
    }

    companion object {
        fun buildFor(session: UserSession): Sidebar {
            val items = mutableListOf<SidebarItem>()

            val repositoryLinks = mutableListOf<SidebarItem>()

            if (session.hasRoleFor(MaterialGroupType.CONTAINER_REPOSITORY, Role.VIEWER)) {
                repositoryLinks.add(SidebarLink(ContainerRepositoryUiController::getAll, "Container Repositories", "View container repositories.", ContainerRepository.Icon))
            }

            if (session.administrator) {
                repositoryLinks.add(SidebarLink(JobsUiController::getAll, "Jobs", "View Job history.", Job.Icon))
            }

            if (repositoryLinks.isNotEmpty()) {
                items.add(SidebarHeader("Repositories", null))
                items.addAll(repositoryLinks)
            }

            return Sidebar(items)
        }
    }
}