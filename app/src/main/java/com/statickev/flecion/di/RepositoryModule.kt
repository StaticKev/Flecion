package com.statickev.flecion.di

import com.statickev.flecion.data.repository.TaskRepository
import com.statickev.flecion.data.dao.TaskDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideAppRepository(taskDao: TaskDAO): TaskRepository {
        return TaskRepository(taskDao)
    }

}