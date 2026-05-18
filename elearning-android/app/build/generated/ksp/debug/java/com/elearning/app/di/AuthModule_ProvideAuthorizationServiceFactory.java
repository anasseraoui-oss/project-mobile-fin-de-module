package com.elearning.app.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationService;

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
public final class AuthModule_ProvideAuthorizationServiceFactory implements Factory<AuthorizationService> {
  private final Provider<Context> contextProvider;

  private final Provider<AppAuthConfiguration> configProvider;

  public AuthModule_ProvideAuthorizationServiceFactory(Provider<Context> contextProvider,
      Provider<AppAuthConfiguration> configProvider) {
    this.contextProvider = contextProvider;
    this.configProvider = configProvider;
  }

  @Override
  public AuthorizationService get() {
    return provideAuthorizationService(contextProvider.get(), configProvider.get());
  }

  public static AuthModule_ProvideAuthorizationServiceFactory create(
      Provider<Context> contextProvider, Provider<AppAuthConfiguration> configProvider) {
    return new AuthModule_ProvideAuthorizationServiceFactory(contextProvider, configProvider);
  }

  public static AuthorizationService provideAuthorizationService(Context context,
      AppAuthConfiguration config) {
    return Preconditions.checkNotNullFromProvides(AuthModule.INSTANCE.provideAuthorizationService(context, config));
  }
}
