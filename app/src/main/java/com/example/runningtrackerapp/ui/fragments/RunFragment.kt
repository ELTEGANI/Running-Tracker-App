package com.example.runningtrackerapp.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.adapter.RunAdapter
import com.example.runningtrackerapp.databinding.FragmentRunBinding
import com.example.runningtrackerapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runningtrackerapp.other.SortType
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.ui.viewmodels.MainViewModels
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


@AndroidEntryPoint
class RunFragment : Fragment(),EasyPermissions.PermissionCallbacks {

    private val mainViewModels: MainViewModels by viewModels()
    private lateinit var fragmentRunBinding: FragmentRunBinding
    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentRunBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_run,container,false)
        fragmentRunBinding.lifecycleOwner = this
        requestPermissions()
        setUpRecyclerView()
        when(mainViewModels.sortType){
            SortType.DATE->fragmentRunBinding.spFilter.setSelection(0)
            SortType.RUNNING_TYPE->fragmentRunBinding.spFilter.setSelection(1)
            SortType.DISTANCE->fragmentRunBinding.spFilter.setSelection(2)
            SortType.AVG_SPEED->fragmentRunBinding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED->fragmentRunBinding.spFilter.setSelection(4)
        }

        fragmentRunBinding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                adapterView : AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
               when(position){
                   0->mainViewModels.sortRuns(SortType.DATE)
                   1->mainViewModels.sortRuns(SortType.RUNNING_TYPE)
                   2->mainViewModels.sortRuns(SortType.DISTANCE)
                   3->mainViewModels.sortRuns(SortType.AVG_SPEED)
                   4->mainViewModels.sortRuns(SortType.CALORIES_BURNED)
               }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        mainViewModels.runs.observe(viewLifecycleOwner, {
            runAdapter.submitList(it)
        })
        fragmentRunBinding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment2_to_trackingFragment)
        }
        return fragmentRunBinding.root
    }

    private fun setUpRecyclerView() = fragmentRunBinding.rvRuns.apply {
             runAdapter = RunAdapter()
             adapter = runAdapter
             layoutManager = LinearLayoutManager(requireContext())
    }
    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermission(requireContext())) {
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}