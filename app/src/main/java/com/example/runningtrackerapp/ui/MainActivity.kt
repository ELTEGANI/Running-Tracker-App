package com.example.runningtrackerapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.ActivityMainBinding
import com.example.runningtrackerapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

         navigateToTrackingFragmentIfNeed(intent)
        setSupportActionBar(activityMainBinding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navigationController = navHostFragment.navController
        NavigationUI.setupWithNavController(activityMainBinding.bottomNavigationView,navigationController)
        activityMainBinding.bottomNavigationView.setupWithNavController(navigationController)
        activityMainBinding.bottomNavigationView.setOnNavigationItemReselectedListener {
            /*NO OP*/
        }
        navigationController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
               R.id.settingsFragment2,R.id.runFragment2,R.id.statisticFragment->
                   activityMainBinding.bottomNavigationView.visibility = View.VISIBLE
                else->activityMainBinding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { navigateToTrackingFragmentIfNeed(it) }
    }

    private fun navigateToTrackingFragmentIfNeed(intent: Intent){
        if(intent.action == ACTION_SHOW_TRACKING_FRAGMENT){
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}