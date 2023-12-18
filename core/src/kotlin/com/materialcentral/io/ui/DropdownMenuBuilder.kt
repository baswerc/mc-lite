package com.materialcentral.io.ui

import com.materialcentral.user.authentication.UserAuthenticationProvider
import com.materialcentral.user.session.UserSession
import org.geezer.io.ui.dropdown.DropdownMenu

object DropdownMenuBuilder {
    fun buildUserDropdown(session: UserSession): DropdownMenu {
        return DropdownMenu(UserAuthenticationProvider.getUserDropdownItems())
    }
}