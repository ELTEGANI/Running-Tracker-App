package com.example.runningtrackerapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
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
import com.example.runningtrackerapp.other.Constants.NOTIFICATION_ID
import com.example.runningtrackerapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.ui.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    var isFirstRun = true
    var serviceKilled = false
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var notificationBuilder:NotificationCompat.Builder
    lateinit var currentNotifcationBuilder:NotificationCompat.Builder
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
        currentNotifcationBuilder = notificationBuilder
        postInitialValue()
        isTracking.observe(this, {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService(){
       serviceKilled = true
       isFirstRun = true
       pauseService()
       postInitialValue()
        stopForeground(true)
        stopSelf()
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
                    killService()
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

    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking){
            val pauseIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent,FLAG_UPDATE_CURRENT)
        }else{
            val resumeIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent,FLAG_UPDATE_CURRENT)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        currentNotifcationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotifcationBuilder,ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            currentNotifcationBuilder = notificationBuilder
                .addAction(R.drawable.ic_paused,notificationText,pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,currentNotifcationBuilder.build())
        }
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

    private fun startForeGroundService(){
        startTimer()
        isTracking.postValue(true)
        val notificationManger = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            createNotificationChannel(notificationManger)
        }
        startForeground(Constants.NOTIFICATION_ID,notificationBuilder.build())
        timeRunInSeconds.observe(this, {
            if(!serviceKilled){
                val notification = currentNotifcationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManger.notify(NOTIFICATION_ID,notification.build())
            }
        })
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