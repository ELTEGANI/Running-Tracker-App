package com.example.runningtrackerapp.ui.fragments

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentTrackingBinding
import com.example.runningtrackerapp.db.Run
import com.example.runningtrackerapp.other.CancelTrackingDailog
import com.example.runningtrackerapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtrackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtrackerapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningtrackerapp.other.Constants.MAP_ZOOM
import com.example.runningtrackerapp.other.Constants.POLYLINE_COLOR
import com.example.runningtrackerapp.other.Constants.POLYLINE_WIDTH
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.service.Polyline
import com.example.runningtrackerapp.service.TrackingService
import com.example.runningtrackerapp.ui.viewmodels.MainViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.round


const val CANCEL_DIALOG_TRACKING = "CANCEL_DIALOG"

@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private lateinit var fragmentTrackingBinding: FragmentTrackingBinding
    private val mainViewModels: MainViewModels by viewModels()
    private var googleMap : GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var menu:Menu? = null
    @set:Inject
    var weight = 80f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentTrackingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracking,container,false)
        fragmentTrackingBinding.lifecycleOwner = this
        setHasOptionsMenu(true)
        return fragmentTrackingBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentTrackingBinding.mapView.onCreate(savedInstanceState)
        fragmentTrackingBinding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        if (savedInstanceState != null){
            val cancelTag = parentFragmentManager.findFragmentByTag(CANCEL_DIALOG_TRACKING)
            as CancelTrackingDailog?
            cancelTag?.setYesListener {
                stopRun()
            }
        }
        fragmentTrackingBinding.mapView.getMapAsync {
            googleMap = it
            addAllPolyLine()
        }
        fragmentTrackingBinding.btnFinishRun.setOnClickListener {
            zoomToSeeTrack()
            endRunAndSaveDb()
        }
        subscriberToObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible =true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.canceltracking->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
       CancelTrackingDailog().apply {
           setYesListener {
               stopRun()
           }
       }.show(parentFragmentManager,CANCEL_DIALOG_TRACKING)
    }

    private fun stopRun(){
        fragmentTrackingBinding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment2)
    }
    private fun subscriberToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLastPloyLine()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            fragmentTrackingBinding.tvTimer.text = formattedTime
        })
    }
    private fun toggleRun(){
      if(isTracking){
          menu?.getItem(0)?.isVisible = true
         sendCommandToService(ACTION_PAUSE_SERVICE)
      }else{
         sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
      }
    }

    private fun updateTracking(isTracking:Boolean){
        this.isTracking = isTracking
        if(!isTracking && curTimeInMillis > 0L) {
            fragmentTrackingBinding.btnToggleRun.text = getString(R.string.Start)
            fragmentTrackingBinding.btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking) {
            fragmentTrackingBinding.btnToggleRun.text = getString(R.string.Stop)
            menu?.getItem(0)?.isVisible = true
            fragmentTrackingBinding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
      if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
          googleMap?.animateCamera(
              CameraUpdateFactory.newLatLngZoom(
                  pathPoints.last().last(),
                  MAP_ZOOM
              )
          )
      }
    }

    private fun addAllPolyLine(){
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            googleMap?.addPolyline(polylineOptions)
        }
    }
    private fun addLastPloyLine(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            googleMap?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action:String) =
        Intent(requireContext(),TrackingService::class.java).also {
             it.action = action
             requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        fragmentTrackingBinding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        fragmentTrackingBinding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        fragmentTrackingBinding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        fragmentTrackingBinding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        fragmentTrackingBinding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragmentTrackingBinding.mapView.onSaveInstanceState(outState)
    }

    private fun zoomToSeeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        googleMap?.moveCamera(
          CameraUpdateFactory.newLatLngBounds(
              bounds.build(),
              fragmentTrackingBinding.mapView.width,
              fragmentTrackingBinding.mapView.height,
              (fragmentTrackingBinding.mapView.height * 0.05f).toInt()
          )
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun endRunAndSaveDb(){
        googleMap?.snapshot {bmp->
          var distanceMeter = 0
          for (polyLine in pathPoints){
             distanceMeter = TrackingUtility.calculatePolyLineLength(polyLine).toInt()
          }
            val avgSpeed = round((distanceMeter / 1000f) / (curTimeInMillis / 1000f / 60 / 60 ) * 10) / 10f
            val dateTime = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceMeter / 1000f) * weight).toInt()
            val run = Run(bmp,dateTime,avgSpeed,distanceMeter,curTimeInMillis,caloriesBurned)
            mainViewModels.insertRun(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView),
               "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }
}