package com.materialcentral.user.session

import com.materialcentral.io.ui.DropdownMenuBuilder
import com.materialcentral.io.ui.Sidebar
import com.materialcentral.user.User
import com.materialcentral.user.GroupUserRoles
import com.materialcentral.user.UserRole
import com.materialcentral.user.UsersTable
import org.geezer.io.ui.dropdown.DropdownMenu
import org.geezer.system.runtime.RuntimeClock
import org.geezer.user.authentication.UserAuthenticationType
import org.geezer.user.session.AppUserSession

class UserSession(
    val groups: List<String>,
    val roles: List<UserRole>,
    identifier: String,
    userId: Long,
    authenticationType: UserAuthenticationType,
    userAgentHash: ByteArray,
    ipAddressHash: ByteArray,
    createdAt: Long,
    lastAccessedAt: Long,
    active: Boolean,
) : AppUserSession(identifier, userId, authenticationType, userAgentHash, ipAddressHash, createdAt, lastAccessedAt, active) {

    val cachedUser: User by lazy { UsersTable.getById(userId) }

    val cachedSidebar: Sidebar by lazy { Sidebar.buildFor(this) }

    val cachedProfileDropdown: DropdownMenu by lazy { DropdownMenuBuilder.buildUserDropdown(this) }

    val administrator: Boolean by lazy { hasRole(UserRole.ADMINISTRATOR) }

    val securityOfficer: Boolean by lazy { hasRole(UserRole.SECURITY_OFFICER) }

    constructor(user: User, groupRoles: List<GroupUserRoles>, authenticationType: UserAuthenticationType,
                userAgent: String, ipAddress: String, createdAt: Long = RuntimeClock.transactionAt, lastAccessedAt: Long = createdAt, active: Boolean = true) :
            this(groupRoles.map { it.groupName }, groupRoles.map { it.role }.distinct(), AppUserSession.createSessionId(), user.id, authenticationType, hash(userAgent), hash(ipAddress), createdAt, lastAccessedAt, active)


    fun hasRole(role: UserRole): Boolean {
        return roles.any { it.equivalentRoles().contains(role) }
    }
}