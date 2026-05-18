package com.elearning.app.presentation.formation;

import androidx.lifecycle.SavedStateHandle;
import com.elearning.app.domain.repository.FormationRepository;
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
public final class FormationDetailViewModel_Factory implements Factory<FormationDetailViewModel> {
  private final Provider<FormationRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public FormationDetailViewModel_Factory(Provider<FormationRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public FormationDetailViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static FormationDetailViewModel_Factory create(
      Provider<FormationRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new FormationDetailViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static FormationDetailViewModel newInstance(FormationRepository repository,
      SavedStateHandle savedStateHandle) {
    return new FormationDetailViewModel(repository, savedStateHandle);
  }
}
