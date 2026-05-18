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
public final class CheckAuthStateUseCase_Factory implements Factory<CheckAuthStateUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public CheckAuthStateUseCase_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public CheckAuthStateUseCase get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static CheckAuthStateUseCase_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new CheckAuthStateUseCase_Factory(authRepositoryProvider);
  }

  public static CheckAuthStateUseCase newInstance(AuthRepository authRepository) {
    return new CheckAuthStateUseCase(authRepository);
  }
}
