package com.elearning.app.presentation.prof;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class CreateFormationViewModel_Factory implements Factory<CreateFormationViewModel> {
  @Override
  public CreateFormationViewModel get() {
    return newInstance();
  }

  public static CreateFormationViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CreateFormationViewModel newInstance() {
    return new CreateFormationViewModel();
  }

  private static final class InstanceHolder {
    private static final CreateFormationViewModel_Factory INSTANCE = new CreateFormationViewModel_Factory();
  }
}
