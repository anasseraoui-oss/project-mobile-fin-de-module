package com.elearning.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.elearning.app.data.local.datastore.TokenManager;
import com.elearning.app.data.notification.ElearningFirebaseMessagingService;
import com.elearning.app.data.remote.api.AuthApiService;
import com.elearning.app.data.remote.api.ResourceApiService;
import com.elearning.app.data.remote.interceptor.AuthInterceptor;
import com.elearning.app.data.repository.AuthRepositoryImpl;
import com.elearning.app.data.repository.MockFormationRepositoryImpl;
import com.elearning.app.data.repository.MockQuizRepositoryImpl;
import com.elearning.app.data.repository.MockSeanceRepositoryImpl;
import com.elearning.app.di.AuthModule_ProvideAppAuthConfigurationFactory;
import com.elearning.app.di.AuthModule_ProvideAuthorizationServiceFactory;
import com.elearning.app.di.NetworkModule_ProvideAuthApiServiceFactory;
import com.elearning.app.di.NetworkModule_ProvideAuthOkHttpClientFactory;
import com.elearning.app.di.NetworkModule_ProvideAuthRetrofitFactory;
import com.elearning.app.di.NetworkModule_ProvideLoggingInterceptorFactory;
import com.elearning.app.di.NetworkModule_ProvideResourceApiServiceFactory;
import com.elearning.app.di.NetworkModule_ProvideResourceOkHttpClientFactory;
import com.elearning.app.di.NetworkModule_ProvideResourceRetrofitFactory;
import com.elearning.app.domain.repository.FormationRepository;
import com.elearning.app.domain.repository.QuizRepository;
import com.elearning.app.domain.repository.SeanceRepository;
import com.elearning.app.domain.usecase.auth.CheckAuthStateUseCase;
import com.elearning.app.domain.usecase.auth.ExchangeCodeForTokensUseCase;
import com.elearning.app.domain.usecase.auth.ForgotPasswordUseCase;
import com.elearning.app.domain.usecase.auth.GetCurrentUserUseCase;
import com.elearning.app.domain.usecase.auth.LoginClassicUseCase;
import com.elearning.app.domain.usecase.auth.LogoutUseCase;
import com.elearning.app.domain.usecase.auth.RegisterUseCase;
import com.elearning.app.presentation.MainActivity;
import com.elearning.app.presentation.auth.AuthViewModel;
import com.elearning.app.presentation.auth.AuthViewModel_HiltModules;
import com.elearning.app.presentation.catalogue.CatalogueViewModel;
import com.elearning.app.presentation.catalogue.CatalogueViewModel_HiltModules;
import com.elearning.app.presentation.formation.FormationDetailViewModel;
import com.elearning.app.presentation.formation.FormationDetailViewModel_HiltModules;
import com.elearning.app.presentation.player.SeancePlayerViewModel;
import com.elearning.app.presentation.player.SeancePlayerViewModel_HiltModules;
import com.elearning.app.presentation.prof.CreateFormationViewModel;
import com.elearning.app.presentation.prof.CreateFormationViewModel_HiltModules;
import com.elearning.app.presentation.quiz.QuizViewModel;
import com.elearning.app.presentation.quiz.QuizViewModel_HiltModules;
import com.elearning.app.presentation.scanner.ScannerViewModel;
import com.elearning.app.presentation.scanner.ScannerViewModel_HiltModules;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

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
public final class DaggerELearningApp_HiltComponents_SingletonC {
  private DaggerELearningApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public ELearningApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements ELearningApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public ELearningApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements ELearningApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public ELearningApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements ELearningApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public ELearningApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements ELearningApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ELearningApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements ELearningApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ELearningApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements ELearningApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public ELearningApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements ELearningApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public ELearningApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends ELearningApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends ELearningApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends ELearningApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends ELearningApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(7).put(LazyClassKeyProvider.com_elearning_app_presentation_auth_AuthViewModel, AuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_elearning_app_presentation_catalogue_CatalogueViewModel, CatalogueViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_elearning_app_presentation_prof_CreateFormationViewModel, CreateFormationViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_elearning_app_presentation_formation_FormationDetailViewModel, FormationDetailViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_elearning_app_presentation_quiz_QuizViewModel, QuizViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_elearning_app_presentation_scanner_ScannerViewModel, ScannerViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_elearning_app_presentation_player_SeancePlayerViewModel, SeancePlayerViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_elearning_app_presentation_formation_FormationDetailViewModel = "com.elearning.app.presentation.formation.FormationDetailViewModel";

      static String com_elearning_app_presentation_auth_AuthViewModel = "com.elearning.app.presentation.auth.AuthViewModel";

      static String com_elearning_app_presentation_prof_CreateFormationViewModel = "com.elearning.app.presentation.prof.CreateFormationViewModel";

      static String com_elearning_app_presentation_quiz_QuizViewModel = "com.elearning.app.presentation.quiz.QuizViewModel";

      static String com_elearning_app_presentation_scanner_ScannerViewModel = "com.elearning.app.presentation.scanner.ScannerViewModel";

      static String com_elearning_app_presentation_catalogue_CatalogueViewModel = "com.elearning.app.presentation.catalogue.CatalogueViewModel";

      static String com_elearning_app_presentation_player_SeancePlayerViewModel = "com.elearning.app.presentation.player.SeancePlayerViewModel";

      @KeepFieldType
      FormationDetailViewModel com_elearning_app_presentation_formation_FormationDetailViewModel2;

      @KeepFieldType
      AuthViewModel com_elearning_app_presentation_auth_AuthViewModel2;

      @KeepFieldType
      CreateFormationViewModel com_elearning_app_presentation_prof_CreateFormationViewModel2;

      @KeepFieldType
      QuizViewModel com_elearning_app_presentation_quiz_QuizViewModel2;

      @KeepFieldType
      ScannerViewModel com_elearning_app_presentation_scanner_ScannerViewModel2;

      @KeepFieldType
      CatalogueViewModel com_elearning_app_presentation_catalogue_CatalogueViewModel2;

      @KeepFieldType
      SeancePlayerViewModel com_elearning_app_presentation_player_SeancePlayerViewModel2;
    }
  }

  private static final class ViewModelCImpl extends ELearningApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<CatalogueViewModel> catalogueViewModelProvider;

    private Provider<CreateFormationViewModel> createFormationViewModelProvider;

    private Provider<FormationDetailViewModel> formationDetailViewModelProvider;

    private Provider<QuizViewModel> quizViewModelProvider;

    private Provider<ScannerViewModel> scannerViewModelProvider;

    private Provider<SeancePlayerViewModel> seancePlayerViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private LoginClassicUseCase loginClassicUseCase() {
      return new LoginClassicUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private LogoutUseCase logoutUseCase() {
      return new LogoutUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private RegisterUseCase registerUseCase() {
      return new RegisterUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private ForgotPasswordUseCase forgotPasswordUseCase() {
      return new ForgotPasswordUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private GetCurrentUserUseCase getCurrentUserUseCase() {
      return new GetCurrentUserUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private CheckAuthStateUseCase checkAuthStateUseCase() {
      return new CheckAuthStateUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    private ExchangeCodeForTokensUseCase exchangeCodeForTokensUseCase() {
      return new ExchangeCodeForTokensUseCase(singletonCImpl.authRepositoryImplProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.catalogueViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.createFormationViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.formationDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.quizViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.scannerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.seancePlayerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(7).put(LazyClassKeyProvider.com_elearning_app_presentation_auth_AuthViewModel, ((Provider) authViewModelProvider)).put(LazyClassKeyProvider.com_elearning_app_presentation_catalogue_CatalogueViewModel, ((Provider) catalogueViewModelProvider)).put(LazyClassKeyProvider.com_elearning_app_presentation_prof_CreateFormationViewModel, ((Provider) createFormationViewModelProvider)).put(LazyClassKeyProvider.com_elearning_app_presentation_formation_FormationDetailViewModel, ((Provider) formationDetailViewModelProvider)).put(LazyClassKeyProvider.com_elearning_app_presentation_quiz_QuizViewModel, ((Provider) quizViewModelProvider)).put(LazyClassKeyProvider.com_elearning_app_presentation_scanner_ScannerViewModel, ((Provider) scannerViewModelProvider)).put(LazyClassKeyProvider.com_elearning_app_presentation_player_SeancePlayerViewModel, ((Provider) seancePlayerViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_elearning_app_presentation_catalogue_CatalogueViewModel = "com.elearning.app.presentation.catalogue.CatalogueViewModel";

      static String com_elearning_app_presentation_scanner_ScannerViewModel = "com.elearning.app.presentation.scanner.ScannerViewModel";

      static String com_elearning_app_presentation_prof_CreateFormationViewModel = "com.elearning.app.presentation.prof.CreateFormationViewModel";

      static String com_elearning_app_presentation_auth_AuthViewModel = "com.elearning.app.presentation.auth.AuthViewModel";

      static String com_elearning_app_presentation_player_SeancePlayerViewModel = "com.elearning.app.presentation.player.SeancePlayerViewModel";

      static String com_elearning_app_presentation_formation_FormationDetailViewModel = "com.elearning.app.presentation.formation.FormationDetailViewModel";

      static String com_elearning_app_presentation_quiz_QuizViewModel = "com.elearning.app.presentation.quiz.QuizViewModel";

      @KeepFieldType
      CatalogueViewModel com_elearning_app_presentation_catalogue_CatalogueViewModel2;

      @KeepFieldType
      ScannerViewModel com_elearning_app_presentation_scanner_ScannerViewModel2;

      @KeepFieldType
      CreateFormationViewModel com_elearning_app_presentation_prof_CreateFormationViewModel2;

      @KeepFieldType
      AuthViewModel com_elearning_app_presentation_auth_AuthViewModel2;

      @KeepFieldType
      SeancePlayerViewModel com_elearning_app_presentation_player_SeancePlayerViewModel2;

      @KeepFieldType
      FormationDetailViewModel com_elearning_app_presentation_formation_FormationDetailViewModel2;

      @KeepFieldType
      QuizViewModel com_elearning_app_presentation_quiz_QuizViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.elearning.app.presentation.auth.AuthViewModel 
          return (T) new AuthViewModel(viewModelCImpl.loginClassicUseCase(), viewModelCImpl.logoutUseCase(), viewModelCImpl.registerUseCase(), viewModelCImpl.forgotPasswordUseCase(), viewModelCImpl.getCurrentUserUseCase(), viewModelCImpl.checkAuthStateUseCase(), viewModelCImpl.exchangeCodeForTokensUseCase(), singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.provideAuthorizationServiceProvider.get());

          case 1: // com.elearning.app.presentation.catalogue.CatalogueViewModel 
          return (T) new CatalogueViewModel(singletonCImpl.provideResourceApiServiceProvider.get());

          case 2: // com.elearning.app.presentation.prof.CreateFormationViewModel 
          return (T) new CreateFormationViewModel();

          case 3: // com.elearning.app.presentation.formation.FormationDetailViewModel 
          return (T) new FormationDetailViewModel(singletonCImpl.bindFormationRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 4: // com.elearning.app.presentation.quiz.QuizViewModel 
          return (T) new QuizViewModel(singletonCImpl.bindQuizRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 5: // com.elearning.app.presentation.scanner.ScannerViewModel 
          return (T) new ScannerViewModel();

          case 6: // com.elearning.app.presentation.player.SeancePlayerViewModel 
          return (T) new SeancePlayerViewModel(singletonCImpl.bindSeanceRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends ELearningApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends ELearningApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectElearningFirebaseMessagingService(
        ElearningFirebaseMessagingService elearningFirebaseMessagingService) {
    }

    @Override
    public void injectElearningFirebaseMessagingService(
        com.elearning.app.service.ElearningFirebaseMessagingService elearningFirebaseMessagingService) {
    }
  }

  private static final class SingletonCImpl extends ELearningApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<HttpLoggingInterceptor> provideLoggingInterceptorProvider;

    private Provider<OkHttpClient> provideAuthOkHttpClientProvider;

    private Provider<Retrofit> provideAuthRetrofitProvider;

    private Provider<AuthApiService> provideAuthApiServiceProvider;

    private Provider<TokenManager> tokenManagerProvider;

    private Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    private Provider<AppAuthConfiguration> provideAppAuthConfigurationProvider;

    private Provider<AuthorizationService> provideAuthorizationServiceProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<OkHttpClient> provideResourceOkHttpClientProvider;

    private Provider<Retrofit> provideResourceRetrofitProvider;

    private Provider<ResourceApiService> provideResourceApiServiceProvider;

    private Provider<MockFormationRepositoryImpl> mockFormationRepositoryImplProvider;

    private Provider<FormationRepository> bindFormationRepositoryProvider;

    private Provider<MockQuizRepositoryImpl> mockQuizRepositoryImplProvider;

    private Provider<QuizRepository> bindQuizRepositoryProvider;

    private Provider<MockSeanceRepositoryImpl> mockSeanceRepositoryImplProvider;

    private Provider<SeanceRepository> bindSeanceRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideLoggingInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HttpLoggingInterceptor>(singletonCImpl, 4));
      this.provideAuthOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 3));
      this.provideAuthRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 2));
      this.provideAuthApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<AuthApiService>(singletonCImpl, 1));
      this.tokenManagerProvider = DoubleCheck.provider(new SwitchingProvider<TokenManager>(singletonCImpl, 5));
      this.authRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 0));
      this.provideAppAuthConfigurationProvider = DoubleCheck.provider(new SwitchingProvider<AppAuthConfiguration>(singletonCImpl, 7));
      this.provideAuthorizationServiceProvider = DoubleCheck.provider(new SwitchingProvider<AuthorizationService>(singletonCImpl, 6));
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 11));
      this.provideResourceOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 10));
      this.provideResourceRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 9));
      this.provideResourceApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<ResourceApiService>(singletonCImpl, 8));
      this.mockFormationRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 12);
      this.bindFormationRepositoryProvider = DoubleCheck.provider((Provider) mockFormationRepositoryImplProvider);
      this.mockQuizRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 13);
      this.bindQuizRepositoryProvider = DoubleCheck.provider((Provider) mockQuizRepositoryImplProvider);
      this.mockSeanceRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 14);
      this.bindSeanceRepositoryProvider = DoubleCheck.provider((Provider) mockSeanceRepositoryImplProvider);
    }

    @Override
    public void injectELearningApp(ELearningApp eLearningApp) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.elearning.app.data.repository.AuthRepositoryImpl 
          return (T) new AuthRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideAuthApiServiceProvider.get(), singletonCImpl.tokenManagerProvider.get());

          case 1: // com.elearning.app.data.remote.api.AuthApiService 
          return (T) NetworkModule_ProvideAuthApiServiceFactory.provideAuthApiService(singletonCImpl.provideAuthRetrofitProvider.get());

          case 2: // @javax.inject.Named("auth") retrofit2.Retrofit 
          return (T) NetworkModule_ProvideAuthRetrofitFactory.provideAuthRetrofit(singletonCImpl.provideAuthOkHttpClientProvider.get());

          case 3: // @javax.inject.Named("auth") okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideAuthOkHttpClientFactory.provideAuthOkHttpClient(singletonCImpl.provideLoggingInterceptorProvider.get());

          case 4: // okhttp3.logging.HttpLoggingInterceptor 
          return (T) NetworkModule_ProvideLoggingInterceptorFactory.provideLoggingInterceptor();

          case 5: // com.elearning.app.data.local.datastore.TokenManager 
          return (T) new TokenManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // net.openid.appauth.AuthorizationService 
          return (T) AuthModule_ProvideAuthorizationServiceFactory.provideAuthorizationService(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideAppAuthConfigurationProvider.get());

          case 7: // net.openid.appauth.AppAuthConfiguration 
          return (T) AuthModule_ProvideAppAuthConfigurationFactory.provideAppAuthConfiguration();

          case 8: // com.elearning.app.data.remote.api.ResourceApiService 
          return (T) NetworkModule_ProvideResourceApiServiceFactory.provideResourceApiService(singletonCImpl.provideResourceRetrofitProvider.get());

          case 9: // @javax.inject.Named("resource") retrofit2.Retrofit 
          return (T) NetworkModule_ProvideResourceRetrofitFactory.provideResourceRetrofit(singletonCImpl.provideResourceOkHttpClientProvider.get());

          case 10: // @javax.inject.Named("resource") okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideResourceOkHttpClientFactory.provideResourceOkHttpClient(singletonCImpl.provideLoggingInterceptorProvider.get(), singletonCImpl.authInterceptorProvider.get());

          case 11: // com.elearning.app.data.remote.interceptor.AuthInterceptor 
          return (T) new AuthInterceptor(singletonCImpl.tokenManagerProvider.get(), singletonCImpl.provideAuthApiServiceProvider);

          case 12: // com.elearning.app.data.repository.MockFormationRepositoryImpl 
          return (T) new MockFormationRepositoryImpl();

          case 13: // com.elearning.app.data.repository.MockQuizRepositoryImpl 
          return (T) new MockQuizRepositoryImpl();

          case 14: // com.elearning.app.data.repository.MockSeanceRepositoryImpl 
          return (T) new MockSeanceRepositoryImpl();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
