package com.elearning.app.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import net.openid.appauth.AppAuthConfiguration;

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
public final class AuthModule_ProvideAppAuthConfigurationFactory implements Factory<AppAuthConfiguration> {
  @Override
  public AppAuthConfiguration get() {
    return provideAppAuthConfiguration();
  }

  public static AuthModule_ProvideAppAuthConfigurationFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppAuthConfiguration provideAppAuthConfiguration() {
    return Preconditions.checkNotNullFromProvides(AuthModule.INSTANCE.provideAppAuthConfiguration());
  }

  private static final class InstanceHolder {
    private static final AuthModule_ProvideAppAuthConfigurationFactory INSTANCE = new AuthModule_ProvideAppAuthConfigurationFactory();
  }
}
