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
public final class MockQuizRepositoryImpl_Factory implements Factory<MockQuizRepositoryImpl> {
  @Override
  public MockQuizRepositoryImpl get() {
    return newInstance();
  }

  public static MockQuizRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockQuizRepositoryImpl newInstance() {
    return new MockQuizRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final MockQuizRepositoryImpl_Factory INSTANCE = new MockQuizRepositoryImpl_Factory();
  }
}
