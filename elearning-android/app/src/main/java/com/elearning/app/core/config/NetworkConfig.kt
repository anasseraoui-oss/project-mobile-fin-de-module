package com.elearning.app.core.config

object NetworkConfig {
    const val AUTH_SERVER_PORT = 9000
    const val RESOURCE_SERVER_PORT = 8081
    const val BASE_HOST = "192.168.1.101"
    const val AUTH_SERVER_URL = "http://$BASE_HOST:$AUTH_SERVER_PORT/"
    const val RESOURCE_SERVER_URL = "http://$BASE_HOST:$RESOURCE_SERVER_PORT/"
}
