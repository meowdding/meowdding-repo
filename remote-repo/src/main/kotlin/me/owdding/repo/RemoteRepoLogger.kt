package me.owdding.repo

import org.slf4j.Logger
import org.slf4j.LoggerFactory

data object RemoteRepoLogger {
    private var logger: Logger = LoggerFactory.getLogger("Meowdding Remote Repo")

    fun withLogger(logger: Logger) {
        this.logger = logger
    }

    fun info(message: String) {
        logger.info("[MeowddingRemoteRepo] $message")
    }

    fun debug(message: String) {
        logger.debug("[MeowddingRemoteRepo] $message")
    }

    fun trace(message: String) {
        logger.trace("[MeowddingRemoteRepo] $message")
    }

    fun warn(message: String) {
        logger.warn("[MeowddingRemoteRepo] $message")
    }

    fun error(message: String) {
        logger.error("[MeowddingRemoteRepo] $message")
    }

    fun error(message: String, throwable: Throwable) {
        logger.error("[MeowddingRemoteRepo] $message", throwable)
    }
}
