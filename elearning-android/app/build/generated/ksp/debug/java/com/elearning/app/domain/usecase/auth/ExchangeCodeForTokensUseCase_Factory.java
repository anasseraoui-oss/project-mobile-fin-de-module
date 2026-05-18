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
public final class ExchangeCodeForTokensUseCase_Factory implements Factory<ExchangeCodeForTokensUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public ExchangeCodeForTokensUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public ExchangeCodeForTokensUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static ExchangeCodeForTokensUseCase_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new ExchangeCodeForTokensUseCase_Factory(authRepositoryProvider);
  }

  public static ExchangeCodeForTokensUseCase newInstance(AuthRepository authRepository) {
    return new ExchangeCodeForTokensUseCase(authRepository);
  }
}
