package com.elearning.app.presentation.player;

import androidx.lifecycle.SavedStateHandle;
import com.elearning.app.domain.repository.SeanceRepository;
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
public final class SeancePlayerViewModel_Factory implements Factory<SeancePlayerViewModel> {
  private final Provider<SeanceRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public SeancePlayerViewModel_Factory(Provider<SeanceRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public SeancePlayerViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static SeancePlayerViewModel_Factory create(Provider<SeanceRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new SeancePlayerViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static SeancePlayerViewModel newInstance(SeanceRepository repository,
      SavedStateHandle savedStateHandle) {
    return new SeancePlayerViewModel(repository, savedStateHandle);
  }
}
