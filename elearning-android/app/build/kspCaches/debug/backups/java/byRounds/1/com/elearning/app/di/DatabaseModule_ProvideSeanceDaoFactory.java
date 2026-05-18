package com.elearning.app.di;

import com.elearning.app.data.local.db.AppDatabase;
import com.elearning.app.data.local.db.SeanceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DatabaseModule_ProvideSeanceDaoFactory implements Factory<SeanceDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSeanceDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SeanceDao get() {
    return provideSeanceDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSeanceDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSeanceDaoFactory(dbProvider);
  }

  public static SeanceDao provideSeanceDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSeanceDao(db));
  }
}
