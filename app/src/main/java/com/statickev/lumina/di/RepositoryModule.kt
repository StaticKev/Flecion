package com.statickev.lumina.di

import com.statickev.lumina.data.AppRepository
import com.statickev.lumina.data.dao.TaskDAO
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
    fun provideAppRepository(taskDao: TaskDAO): AppRepository {
        return AppRepository(taskDao)
    }

}