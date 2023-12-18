package com.materialcentral.oss.host.github

enum class GithubProjectVisibility(val id: String) {
    PUBLIC("public"),
    PRIVATE("private"),
    INTERNAL("internal");

    companion object {
        fun map(id: String): GithubProjectVisibility = values().firstOrNull { it.id == id } ?: throw IllegalArgumentException("Invalid GitHub project visibility id $id")
    }

}