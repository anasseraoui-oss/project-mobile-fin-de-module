package com.elearning.app.presentation.quiz;

import androidx.lifecycle.SavedStateHandle;
import com.elearning.app.domain.repository.QuizRepository;
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
public final class QuizViewModel_Factory implements Factory<QuizViewModel> {
  private final Provider<QuizRepository> repositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public QuizViewModel_Factory(Provider<QuizRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public QuizViewModel get() {
    return newInstance(repositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static QuizViewModel_Factory create(Provider<QuizRepository> repositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new QuizViewModel_Factory(repositoryProvider, savedStateHandleProvider);
  }

  public static QuizViewModel newInstance(QuizRepository repository,
      SavedStateHandle savedStateHandle) {
    return new QuizViewModel(repository, savedStateHandle);
  }
}
