package com.materialcentral.user.authentication

import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.dropdown.DropdownItem
import org.geezer.io.ui.sidebar.SidebarItem
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.routes.RequestParameters
import org.geezer.routes.RoutingTable

interface UserAuthenticationProvider {
    fun registerUiRoutes(routingTable: RoutingTable)

    fun createUserSessionFromRequest(request: HttpServletRequest, response: HttpServletResponse): UserSession?

    fun onUnauthorized(request: HttpServletRequest, response: HttpServletResponse)

    fun login(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): UserSession?

    fun getAdministrationSidebarItems(): List<SidebarItem>

    fun getUserDropdownItems(): List<DropdownItem>

    companion object : UserAuthenticationProvider {

        @JvmStatic
        val LoginIcon = FontIcon("fa-arrow-right-to-bracket", "f090")

        private lateinit var userAuthenticationProvider: UserAuthenticationProvider

        fun initializeProvider(userAuthenticationProvider: UserAuthenticationProvider) {
            this.userAuthenticationProvider = userAuthenticationProvider
        }

        override fun registerUiRoutes(routingTable: RoutingTable) {
            userAuthenticationProvider.registerUiRoutes(routingTable)
        }

        override fun createUserSessionFromRequest(request: HttpServletRequest, response: HttpServletResponse): UserSession? {
            return userAuthenticationProvider.createUserSessionFromRequest(request, response)
        }

        override fun onUnauthorized(request: HttpServletRequest, response: HttpServletResponse) {
            userAuthenticationProvider.onUnauthorized(request, response)
        }

        override fun login(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): UserSession? {
            return userAuthenticationProvider.login(request, parameters, response)
        }

        override fun getAdministrationSidebarItems(): List<SidebarItem> {
            return userAuthenticationProvider.getAdministrationSidebarItems()
        }

        override fun getUserDropdownItems(): List<DropdownItem> {
            return userAuthenticationProvider.getUserDropdownItems()
        }
    }
}