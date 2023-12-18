package com.materialcentral.user.session

import com.materialcentral.user.User
import com.materialcentral.user.GroupUserRoles
import org.geezer.db.db
import org.geezer.system.runtime.RuntimeClock
import org.geezer.user.authentication.UserAuthenticationType
import org.geezer.user.session.AppUserSessionRequestManager

object UserSessionRequestManager : AppUserSessionRequestManager<UserSession>(UserSessionsTable) {
    fun createSession(user: User, groupsRoles: List<GroupUserRoles>, authenticationType: UserAuthenticationType, userAgent: String, ipAddress: String, createdAt: Long = RuntimeClock.now): UserSession {
        return db {
            UserSessionsTable.create(UserSession(user, groupsRoles, authenticationType, userAgent, ipAddress, createdAt, createdAt, true))
        }
    }
}