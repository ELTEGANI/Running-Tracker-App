package com.example.runningtrackerapp.repository

import com.example.runningtrackerapp.db.Run
import com.example.runningtrackerapp.db.RunDao
import javax.inject.Inject


class MainRepository @Inject constructor(
    val runDao: RunDao
){
   suspend fun insertRun(run: Run) = runDao.insertRun(run)

   suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

   fun getAllRunSortedByDate() = runDao.getAllRunSortedByDate()

   fun getAllRunSortedByDistance() = runDao.getAllRunSortedByDistanceInMeter()

   fun getAllRunSortedByTimeInMilis() = runDao.getAllRunSortedByTimeInMilis()

   fun getAllRunSortedByAvgSpeed() = runDao.getAllRunSortedByAvgSpeed()

   fun getAllRunSortedByCaloriesBurned() = runDao.getAllRunSortedByCaloriesBurned()

   fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalColariesBurned() = runDao.getTotalColariesBurned()

    fun getTotalTimeInMilis() = runDao.getTotalTimeInMilis()

}