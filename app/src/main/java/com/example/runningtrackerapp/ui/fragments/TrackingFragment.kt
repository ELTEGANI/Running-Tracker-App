package com.example.runningtrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentTrackingBinding
import com.example.runningtrackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtrackerapp.service.TrackingService
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private lateinit var fragmentTrackingBinding: FragmentTrackingBinding
    private var googleMap : GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentTrackingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracking,container,false)
        fragmentTrackingBinding.lifecycleOwner = this
        return fragmentTrackingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentTrackingBinding.mapView.onCreate(savedInstanceState)
        fragmentTrackingBinding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
        fragmentTrackingBinding.mapView.getMapAsync {
            googleMap = it
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