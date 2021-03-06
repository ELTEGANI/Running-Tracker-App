package com.example.runningtrackerapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningtrackerapp.db.RunningDataBase
import com.example.runningtrackerapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningtrackerapp.other.Constants.KEY_NAME
import com.example.runningtrackerapp.other.Constants.KEY_WEIGHT
import com.example.runningtrackerapp.other.Constants.RUNNING_DATABASE_NAME
import com.example.runningtrackerapp.other.Constants.SHARED_PREFERENCES_NAME
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

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context) =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME,"")?: ""


    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT,80f)


    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE,true)
}