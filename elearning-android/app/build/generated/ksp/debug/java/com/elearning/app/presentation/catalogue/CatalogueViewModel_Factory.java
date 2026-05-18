package com.elearning.app.presentation.catalogue;

import com.elearning.app.data.remote.api.ResourceApiService;
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
public final class CatalogueViewModel_Factory implements Factory<CatalogueViewModel> {
  private final Provider<ResourceApiService> apiProvider;

  public CatalogueViewModel_Factory(Provider<ResourceApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public CatalogueViewModel get() {
    return newInstance(apiProvider.get());
  }

  public static CatalogueViewModel_Factory create(Provider<ResourceApiService> apiProvider) {
    return new CatalogueViewModel_Factory(apiProvider);
  }

  public static CatalogueViewModel newInstance(ResourceApiService api) {
    return new CatalogueViewModel(api);
  }
}
