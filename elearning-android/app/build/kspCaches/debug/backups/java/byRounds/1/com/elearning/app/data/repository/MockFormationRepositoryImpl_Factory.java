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
public final class MockFormationRepositoryImpl_Factory implements Factory<MockFormationRepositoryImpl> {
  @Override
  public MockFormationRepositoryImpl get() {
    return newInstance();
  }

  public static MockFormationRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockFormationRepositoryImpl newInstance() {
    return new MockFormationRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final MockFormationRepositoryImpl_Factory INSTANCE = new MockFormationRepositoryImpl_Factory();
  }
}
