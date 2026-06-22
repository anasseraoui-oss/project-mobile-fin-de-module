package com.elearning.app.data.remote.api

import com.elearning.app.data.remote.dto.LoginRequestDto
import com.elearning.app.data.remote.dto.RegisterRequestDto
import com.elearning.app.data.remote.dto.TokenResponseDto
import com.elearning.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for the Authorization Server endpoints.
 */
interface AuthApiService {

    /**
     * Resource Owner Password Credentials Grant (classic login).
     */
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun loginWithPassword(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("client_id") clientId: String = "elearning-mobile-client",
        @Field("scope") scope: String = "openid profile email offline_access"
    ): Response<TokenResponseDto>

    /**
     * Authorization Code Exchange (PKCE flow callback).
     */
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun exchangeAuthorizationCode(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("client_id") clientId: String = "elearning-mobile-client"
    ): Response<TokenResponseDto>

    /**
     * Refresh Token Grant.
     */
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): Response<TokenResponseDto>

    /**
     * Token Revocation (logout).
     */
    @FormUrlEncoded
    @POST("oauth2/revoke")
    suspend fun revokeToken(
        @Field("token") token: String,
        @Field("client_id") clientId: String = "elearning-mobile-client"
    ): Response<Unit>


    /**
     * Register a new user.
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<Unit>

    /**
     * Request a password-reset email.
     */
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body email: Map<String, String>): Response<Unit>

    /**
     * Login with Google SSO token.
     */
    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body request: com.elearning.app.data.remote.dto.GoogleLoginRequestDto
    ): Response<TokenResponseDto>
}
