package com.elearning.app.di;

import com.elearning.app.data.local.db.AppDatabase;
import com.elearning.app.data.local.db.FormationDao;
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
public final class DatabaseModule_ProvideFormationDaoFactory implements Factory<FormationDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideFormationDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public FormationDao get() {
    return provideFormationDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideFormationDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideFormationDaoFactory(dbProvider);
  }

  public static FormationDao provideFormationDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFormationDao(db));
  }
}
