package com.materialcentral.user.session

import com.materialcentral.io.ui.DropdownMenuBuilder
import com.materialcentral.io.ui.Sidebar
import com.materialcentral.user.User
import com.materialcentral.user.authorization.Group
import com.materialcentral.user.UserRole
import com.materialcentral.user.UsersTable
import com.materialcentral.user.authentication.UserAuthenticationType
import org.geezer.io.ui.dropdown.DropdownMenu
import org.geezer.system.runtime.RuntimeClock
import org.geezer.user.session.AppUserSession

class UserSession(
    val groups: List<String>,
    val roles: List<UserRole>,
    override val userAuthenticationType: UserAuthenticationType,
    identifier: String,
    userId: Long,
    userAgentHash: ByteArray,
    ipAddressHash: ByteArray,
    createdAt: Long,
    lastAccessedAt: Long,
    active: Boolean,
) : AppUserSession(identifier, userId, userAgentHash, ipAddressHash, createdAt, lastAccessedAt, active) {

    val cachedUser: User by lazy { UsersTable.getById(userId) }

    val cachedSidebar: Sidebar by lazy { Sidebar.buildFor(this) }

    val cachedProfileDropdown: DropdownMenu by lazy { DropdownMenuBuilder.buildUserDropdown(this) }

    val administrator: Boolean by lazy { hasRole(UserRole.ADMINISTRATOR) }

    val securityOfficer: Boolean by lazy { hasRole(UserRole.SECURITY_OFFICER) }

    constructor(user: User, groupRoles: List<Group>, authenticationType: UserAuthenticationType,
                userAgent: String, ipAddress: String, createdAt: Long = RuntimeClock.transactionAt, lastAccessedAt: Long = createdAt, active: Boolean = true) :
            this(groupRoles.map { it.name }, groupRoles.flatMap { it.memberRoles }.distinct(), authenticationType, createSessionId(), user.id, hash(userAgent), hash(ipAddress), createdAt, lastAccessedAt, active)


    fun hasAnyRole(role: UserRole, vararg additionalRoles: UserRole): Boolean {
        return (additionalRoles.toList() + role).any { hasRole(it) }
    }

    fun hasAllRole(role: UserRole, vararg additionalRoles: UserRole): Boolean {
        return (additionalRoles.toList() + role).all { hasRole(it) }
    }

    fun hasRole(role: UserRole): Boolean {
        return roles.any { it.equivalentRoles().contains(role) }
    }
}