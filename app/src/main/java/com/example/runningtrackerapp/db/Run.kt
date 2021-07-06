package com.example.runningtrackerapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run (
  var image:Bitmap? = null,
  var timeStamp:Long = 0L,
  var avgSpeedInKmh:Int = 0,
  var distanceInMeters:Int = 0,
  var timeInMilis:Long = 0L,
  var caloriesBurned : Int = 0
){
  @PrimaryKey(autoGenerate = true)
  var id : Int? = null
}