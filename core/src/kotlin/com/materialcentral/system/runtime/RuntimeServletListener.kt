package com.materialcentral.system.runtime

import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener

class RuntimeServletListener : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent) {
        MaterialRuntime.start()
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        MaterialRuntime.stop()
    }
}