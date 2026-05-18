package com.elearning.app.core.config

import com.elearning.app.BuildConfig

/**
 * NetworkConfig — single source of truth for backend URLs.
 *
 * URLs are injected at compile-time via BuildConfig, which reads from:
 *   - debug: local.properties (never committed to git)
 *   - release: hardcoded production URLs in build.gradle.kts
 *
 * ⚠️ Never hardcode any IP address here. Use local.properties instead.
 */
object NetworkConfig {
    val AUTH_SERVER_URL: String get() = BuildConfig.AUTH_SERVER_URL
    val RESOURCE_SERVER_URL: String get() = BuildConfig.RESOURCE_SERVER_URL
}
