package com.example.runningtrackerapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.other.Constants
import com.example.runningtrackerapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtrackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtrackerapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningtrackerapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningtrackerapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningtrackerapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.ui.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {
    var isFirstRun = true
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timeRunInSeconds = MutableLiveData<Long>()
    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValue(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitialValue()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, {
            updateLocationTracking(it)
        })
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->{
                  if(isFirstRun){
                    startForeGroundService()
                    isFirstRun = false
                  }else{
                      Timber.d("Resuming service....")
                      startTimer()
                  }
                  Timber.d("Started or resumed service")
                }
                ACTION_PAUSE_SERVICE ->{
                    Timber.d("Pause service")
                    pauseService()
                }
                ACTION_STOP_SERVICE ->{
                    Timber.d("Stop service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecoundTimeStamp = 0L

    private fun startTimer() {
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecoundTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecoundTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService(){
        isTimerEnabled = false
        isTracking.postValue(false)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking:Boolean){
         if (isTracking){
             if (TrackingUtility.hasLocationPermission(this)){
                 val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                 }
                 fusedLocationProviderClient.requestLocationUpdates(
                     request,
                     locationCallback,
                     Looper.getMainLooper()
                 )
             }
         }else{
             fusedLocationProviderClient.removeLocationUpdates(locationCallback)
         }
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!){
                result?.locations?.let {locations->
                    for (location in locations){
                        addPathPoint(location)
                        Timber.d("New Location : ${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location:Location?){
      location?.let {
           val pos = LatLng(location.latitude,location.longitude)
           pathPoints.value?.apply {
               last().add(pos)
               pathPoints.postValue(this)
           }
      }
    }

    private fun addEmptyPolyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }?: pathPoints.postValue(mutableListOf(mutableListOf()))

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0, Intent(this, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun startForeGroundService(){
        startTimer()
        isTracking.postValue(true)
        val notificationManger = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            createNotificationChannel(notificationManger)
        }
        val notificationBuilder = NotificationCompat.Builder(this,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(Constants.NOTIFICATION_ID,notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}