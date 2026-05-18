package com.elearning.app.presentation.auth;

import com.elearning.app.domain.repository.AuthRepository;
import com.elearning.app.domain.usecase.auth.CheckAuthStateUseCase;
import com.elearning.app.domain.usecase.auth.ExchangeCodeForTokensUseCase;
import com.elearning.app.domain.usecase.auth.ForgotPasswordUseCase;
import com.elearning.app.domain.usecase.auth.GetCurrentUserUseCase;
import com.elearning.app.domain.usecase.auth.LoginClassicUseCase;
import com.elearning.app.domain.usecase.auth.LogoutUseCase;
import com.elearning.app.domain.usecase.auth.RegisterUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import net.openid.appauth.AuthorizationService;

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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<LoginClassicUseCase> loginClassicUseCaseProvider;

  private final Provider<LogoutUseCase> logoutUseCaseProvider;

  private final Provider<RegisterUseCase> registerUseCaseProvider;

  private final Provider<ForgotPasswordUseCase> forgotPasswordUseCaseProvider;

  private final Provider<GetCurrentUserUseCase> getCurrentUserUseCaseProvider;

  private final Provider<CheckAuthStateUseCase> checkAuthStateUseCaseProvider;

  private final Provider<ExchangeCodeForTokensUseCase> exchangeCodeForTokensUseCaseProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<AuthorizationService> authorizationServiceProvider;

  public AuthViewModel_Factory(Provider<LoginClassicUseCase> loginClassicUseCaseProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider,
      Provider<RegisterUseCase> registerUseCaseProvider,
      Provider<ForgotPasswordUseCase> forgotPasswordUseCaseProvider,
      Provider<GetCurrentUserUseCase> getCurrentUserUseCaseProvider,
      Provider<CheckAuthStateUseCase> checkAuthStateUseCaseProvider,
      Provider<ExchangeCodeForTokensUseCase> exchangeCodeForTokensUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<AuthorizationService> authorizationServiceProvider) {
    this.loginClassicUseCaseProvider = loginClassicUseCaseProvider;
    this.logoutUseCaseProvider = logoutUseCaseProvider;
    this.registerUseCaseProvider = registerUseCaseProvider;
    this.forgotPasswordUseCaseProvider = forgotPasswordUseCaseProvider;
    this.getCurrentUserUseCaseProvider = getCurrentUserUseCaseProvider;
    this.checkAuthStateUseCaseProvider = checkAuthStateUseCaseProvider;
    this.exchangeCodeForTokensUseCaseProvider = exchangeCodeForTokensUseCaseProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.authorizationServiceProvider = authorizationServiceProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(loginClassicUseCaseProvider.get(), logoutUseCaseProvider.get(), registerUseCaseProvider.get(), forgotPasswordUseCaseProvider.get(), getCurrentUserUseCaseProvider.get(), checkAuthStateUseCaseProvider.get(), exchangeCodeForTokensUseCaseProvider.get(), authRepositoryProvider.get(), authorizationServiceProvider.get());
  }

  public static AuthViewModel_Factory create(
      Provider<LoginClassicUseCase> loginClassicUseCaseProvider,
      Provider<LogoutUseCase> logoutUseCaseProvider,
      Provider<RegisterUseCase> registerUseCaseProvider,
      Provider<ForgotPasswordUseCase> forgotPasswordUseCaseProvider,
      Provider<GetCurrentUserUseCase> getCurrentUserUseCaseProvider,
      Provider<CheckAuthStateUseCase> checkAuthStateUseCaseProvider,
      Provider<ExchangeCodeForTokensUseCase> exchangeCodeForTokensUseCaseProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<AuthorizationService> authorizationServiceProvider) {
    return new AuthViewModel_Factory(loginClassicUseCaseProvider, logoutUseCaseProvider, registerUseCaseProvider, forgotPasswordUseCaseProvider, getCurrentUserUseCaseProvider, checkAuthStateUseCaseProvider, exchangeCodeForTokensUseCaseProvider, authRepositoryProvider, authorizationServiceProvider);
  }

  public static AuthViewModel newInstance(LoginClassicUseCase loginClassicUseCase,
      LogoutUseCase logoutUseCase, RegisterUseCase registerUseCase,
      ForgotPasswordUseCase forgotPasswordUseCase, GetCurrentUserUseCase getCurrentUserUseCase,
      CheckAuthStateUseCase checkAuthStateUseCase,
      ExchangeCodeForTokensUseCase exchangeCodeForTokensUseCase, AuthRepository authRepository,
      AuthorizationService authorizationService) {
    return new AuthViewModel(loginClassicUseCase, logoutUseCase, registerUseCase, forgotPasswordUseCase, getCurrentUserUseCase, checkAuthStateUseCase, exchangeCodeForTokensUseCase, authRepository, authorizationService);
  }
}
