package com.elearning.app.di

import com.elearning.app.data.repository.AuthRepositoryImpl
import com.elearning.app.data.repository.CertificateRepositoryImpl
import com.elearning.app.data.repository.FavoritesRepositoryImpl
import com.elearning.app.data.repository.FormationRepositoryImpl
import com.elearning.app.data.repository.NotificationRepositoryImpl
import com.elearning.app.data.repository.QuizRepositoryImpl
import com.elearning.app.data.repository.SeanceRepositoryImpl
import com.elearning.app.domain.repository.AuthRepository
import com.elearning.app.domain.repository.CertificateRepository
import com.elearning.app.domain.repository.FavoritesRepository
import com.elearning.app.domain.repository.FormationRepository
import com.elearning.app.domain.repository.NotificationRepository
import com.elearning.app.domain.repository.QuizRepository
import com.elearning.app.domain.repository.SeanceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule — binds repository interfaces to their implementations.
 * Using @Binds is more efficient than @Provides for this pattern.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFormationRepository(impl: FormationRepositoryImpl): FormationRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindCertificateRepository(impl: CertificateRepositoryImpl): CertificateRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository

    @Binds
    @Singleton
    abstract fun bindSeanceRepository(impl: SeanceRepositoryImpl): SeanceRepository
}
