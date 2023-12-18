package com.materialcentral.user.session

import com.materialcentral.user.User
import com.materialcentral.user.authorization.Group
import com.materialcentral.user.authentication.UserAuthenticationType
import org.geezer.db.db
import org.geezer.system.runtime.RuntimeClock
import org.geezer.user.session.AppUserSessionRequestManager

object UserSessionRequestManager : AppUserSessionRequestManager<UserSession>(UserSessionsTable) {
    fun createSession(user: User, groupsRoles: List<Group>, authenticationType: UserAuthenticationType, userAgent: String, ipAddress: String, createdAt: Long = RuntimeClock.now): UserSession {
        return db {
            UserSessionsTable.create(UserSession(user, groupsRoles, authenticationType, userAgent, ipAddress, createdAt, createdAt, true))
        }
    }
}