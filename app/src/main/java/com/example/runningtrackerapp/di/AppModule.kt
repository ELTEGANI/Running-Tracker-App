package com.example.runningtrackerapp.di

import android.content.Context
import androidx.room.Room
import com.example.runningtrackerapp.db.RunningDataBase
import com.example.runningtrackerapp.other.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideRunningDataBase(@ApplicationContext context:Context) = Room.databaseBuilder(
       context,
       RunningDataBase::class.java,
       RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun providesDao(runningDataBase: RunningDataBase) = runningDataBase.getRunDao()
}