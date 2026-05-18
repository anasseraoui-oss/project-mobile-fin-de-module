package com.elearning.app.domain.usecase.auth;

import com.elearning.app.domain.repository.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class LoginClassicUseCase_Factory implements Factory<LoginClassicUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public LoginClassicUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public LoginClassicUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static LoginClassicUseCase_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new LoginClassicUseCase_Factory(authRepositoryProvider);
  }

  public static LoginClassicUseCase newInstance(AuthRepository authRepository) {
    return new LoginClassicUseCase(authRepository);
  }
}
