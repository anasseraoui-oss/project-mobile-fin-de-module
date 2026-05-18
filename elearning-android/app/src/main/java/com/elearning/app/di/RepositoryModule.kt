package com.elearning.app.di

import com.elearning.app.data.repository.AuthRepositoryImpl
import com.elearning.app.data.repository.MockFormationRepositoryImpl
import com.elearning.app.data.repository.MockQuizRepositoryImpl
import com.elearning.app.data.repository.MockSeanceRepositoryImpl
import com.elearning.app.domain.repository.AuthRepository
import com.elearning.app.domain.repository.FormationRepository
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
    abstract fun bindFormationRepository(impl: MockFormationRepositoryImpl): FormationRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(impl: MockQuizRepositoryImpl): QuizRepository

    @Binds
    @Singleton
    abstract fun bindSeanceRepository(impl: MockSeanceRepositoryImpl): SeanceRepository
}
