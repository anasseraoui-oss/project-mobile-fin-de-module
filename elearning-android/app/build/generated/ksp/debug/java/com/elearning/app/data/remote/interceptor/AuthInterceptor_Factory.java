package com.elearning.app.data.remote.interceptor;

import com.elearning.app.data.local.datastore.TokenManager;
import com.elearning.app.data.remote.api.AuthApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AuthInterceptor_Factory implements Factory<AuthInterceptor> {
  private final Provider<TokenManager> tokenManagerProvider;

  private final Provider<AuthApiService> authServiceProvider;

  public AuthInterceptor_Factory(Provider<TokenManager> tokenManagerProvider,
      Provider<AuthApiService> authServiceProvider) {
    this.tokenManagerProvider = tokenManagerProvider;
    this.authServiceProvider = authServiceProvider;
  }

  @Override
  public AuthInterceptor get() {
    return newInstance(tokenManagerProvider.get(), authServiceProvider);
  }

  public static AuthInterceptor_Factory create(Provider<TokenManager> tokenManagerProvider,
      Provider<AuthApiService> authServiceProvider) {
    return new AuthInterceptor_Factory(tokenManagerProvider, authServiceProvider);
  }

  public static AuthInterceptor newInstance(TokenManager tokenManager,
      Provider<AuthApiService> authServiceProvider) {
    return new AuthInterceptor(tokenManager, authServiceProvider);
  }
}
