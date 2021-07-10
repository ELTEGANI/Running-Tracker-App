package com.example.runningtrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentTrackingBinding
import com.example.runningtrackerapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtrackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtrackerapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningtrackerapp.other.Constants.MAP_ZOOM
import com.example.runningtrackerapp.other.Constants.POLYLINE_COLOR
import com.example.runningtrackerapp.other.Constants.POLYLINE_WIDTH
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.service.Polyline
import com.example.runningtrackerapp.service.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private lateinit var fragmentTrackingBinding: FragmentTrackingBinding
    private var googleMap : GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var menu:Menu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentTrackingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracking,container,false)
        fragmentTrackingBinding.lifecycleOwner = this
        setHasOptionsMenu(true)
        return fragmentTrackingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentTrackingBinding.mapView.onCreate(savedInstanceState)
        fragmentTrackingBinding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        fragmentTrackingBinding.mapView.getMapAsync {
            googleMap = it
            addAllPolyLine()
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
        val dialog = MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogTheme)
            .setTitle("Cancel the Run")
            .setMessage("Are You Sure To Cancel The Current Run")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){_,_->
              stopRun()
            }
            .setNegativeButton("No"){dialogInterface,_,->
              dialogInterface.cancel()
            }.create()
            dialog.show()
    }

    private fun stopRun(){
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
        if(!isTracking) {
            fragmentTrackingBinding.btnToggleRun.text = "Start"
            fragmentTrackingBinding.btnFinishRun.visibility = View.VISIBLE
        } else {
            fragmentTrackingBinding.btnToggleRun.text = "Stop"
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
}