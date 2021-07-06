package com.example.runningtrackerapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("Select * from running_table ORDER By timeStamp DESC")
    fun getAllRunSortedByDate():LiveData<List<Run>>

    @Query("Select * from running_table ORDER By timeInMilis DESC")
    fun getAllRunSortedByTimeInMilis():LiveData<List<Run>>

    @Query("Select * from running_table ORDER By caloriesBurned DESC")
    fun getAllRunSortedByCaloriesBurned():LiveData<List<Run>>

    @Query("Select * from running_table ORDER By avgSpeedInKmh DESC")
    fun getAllRunSortedByAvgSpeed():LiveData<List<Run>>

    @Query("Select * from running_table ORDER By distanceInMeters DESC")
    fun getAllRunSortedByDistanceInMeter():LiveData<List<Run>>

    @Query("Select SUM(timeInMilis) from running_table")
    fun getTotalTimeInMilis():LiveData<Long>

    @Query("Select SUM(caloriesBurned) from running_table")
    fun getTotalColariesBurned():LiveData<Int>

    @Query("Select SUM(distanceInMeters) from running_table")
    fun getTotalDistance():LiveData<Int>

    @Query("Select SUM(avgSpeedInKmh) from running_table")
    fun getTotalAvgSpeed():LiveData<Float>
}