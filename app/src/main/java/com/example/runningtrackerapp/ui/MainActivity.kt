package com.example.runningtrackerapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(activityMainBinding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navigationController = navHostFragment.navController
        NavigationUI.setupWithNavController(activityMainBinding.bottomNavigationView,navigationController)
        activityMainBinding.bottomNavigationView.setupWithNavController(navigationController)
        navigationController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
               R.id.settingsFragment2,R.id.runFragment2,R.id.statisticFragment->
                   activityMainBinding.bottomNavigationView.visibility = View.VISIBLE
                else->activityMainBinding.bottomNavigationView.visibility = View.GONE
            }
        }
    }
}