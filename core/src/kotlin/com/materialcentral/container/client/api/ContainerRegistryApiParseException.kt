package com.materialcentral.container.client.api

import java.io.IOException

/**
 * Thrown when the RegistryClient is not able to parse a successful response from the registry.
 */
class ContainerRegistryApiParseException(message: String) : IOException(message) {
}