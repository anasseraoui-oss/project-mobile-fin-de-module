package com.elearning.app.di

import android.content.Context
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AuthModule — configures AppAuth's [AuthorizationService] with a secure browser allowlist.
 *
 * The browser allowlist restricts the PKCE flow to known safe browsers (Chrome, Firefox).
 * TokenManager is provided automatically via @Singleton in its own class.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAppAuthConfiguration(): AppAuthConfiguration =
        AppAuthConfiguration.Builder()
            .setBrowserMatcher(
                BrowserAllowList(
                    VersionedBrowserMatcher.CHROME_BROWSER,
                    VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                    VersionedBrowserMatcher.FIREFOX_BROWSER,
                    VersionedBrowserMatcher.FIREFOX_CUSTOM_TAB,
                    VersionedBrowserMatcher.SAMSUNG_BROWSER,
                    VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
                )
            )
            .build()

    @Provides
    @Singleton
    fun provideAuthorizationService(
        @ApplicationContext context: Context,
        config: AppAuthConfiguration
    ): AuthorizationService = AuthorizationService(context, config)
}
