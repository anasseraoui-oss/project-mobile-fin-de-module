package com.elearning.app.data.repository;

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
public final class MockSeanceRepositoryImpl_Factory implements Factory<MockSeanceRepositoryImpl> {
  @Override
  public MockSeanceRepositoryImpl get() {
    return newInstance();
  }

  public static MockSeanceRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockSeanceRepositoryImpl newInstance() {
    return new MockSeanceRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final MockSeanceRepositoryImpl_Factory INSTANCE = new MockSeanceRepositoryImpl_Factory();
  }
}
