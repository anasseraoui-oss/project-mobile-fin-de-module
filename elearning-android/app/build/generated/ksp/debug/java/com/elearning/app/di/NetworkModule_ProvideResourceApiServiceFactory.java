package com.elearning.app.di;

import com.elearning.app.data.remote.api.ResourceApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_ProvideResourceApiServiceFactory implements Factory<ResourceApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideResourceApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ResourceApiService get() {
    return provideResourceApiService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideResourceApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideResourceApiServiceFactory(retrofitProvider);
  }

  public static ResourceApiService provideResourceApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideResourceApiService(retrofit));
  }
}
