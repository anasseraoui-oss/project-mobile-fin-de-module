package com.elearning.app.data.repository;

import android.content.Context;
import com.elearning.app.data.local.datastore.TokenManager;
import com.elearning.app.data.remote.api.AuthApiService;
import com.elearning.app.data.remote.api.ResourceApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<AuthApiService> authApiServiceProvider;

  private final Provider<ResourceApiService> resourceApiServiceProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  public AuthRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<AuthApiService> authApiServiceProvider,
      Provider<ResourceApiService> resourceApiServiceProvider,
      Provider<TokenManager> tokenManagerProvider) {
    this.contextProvider = contextProvider;
    this.authApiServiceProvider = authApiServiceProvider;
    this.resourceApiServiceProvider = resourceApiServiceProvider;
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(contextProvider.get(), authApiServiceProvider.get(), resourceApiServiceProvider.get(), tokenManagerProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<AuthApiService> authApiServiceProvider,
      Provider<ResourceApiService> resourceApiServiceProvider,
      Provider<TokenManager> tokenManagerProvider) {
    return new AuthRepositoryImpl_Factory(contextProvider, authApiServiceProvider, resourceApiServiceProvider, tokenManagerProvider);
  }

  public static AuthRepositoryImpl newInstance(Context context, AuthApiService authApiService,
      ResourceApiService resourceApiService, TokenManager tokenManager) {
    return new AuthRepositoryImpl(context, authApiService, resourceApiService, tokenManager);
  }
}
