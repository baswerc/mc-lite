package com.materialcentral.user

import org.geezer.db.Data

class GroupUserRoles(
    var groupName: String,
    var roles: List<UserRole>,
    var active: Boolean
) : Data() {
}