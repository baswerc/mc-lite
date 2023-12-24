package com.materialcentral.container.client

class TagMetadata(val value: String, val taggedAt: Long? = null) {

    override fun toString(): String {
        return value
    }

    operator fun component1(): String {
        return value
    }

    operator fun component2(): Long? {
        return taggedAt
    }
}