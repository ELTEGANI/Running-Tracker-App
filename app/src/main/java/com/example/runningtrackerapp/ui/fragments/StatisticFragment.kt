package com.example.runningtrackerapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentStatisticBinding
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.ui.viewmodels.StatisticViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticFragment : Fragment() {

    private val statisticViewModels: StatisticViewModels by viewModels()
    private lateinit var fragmentStatisticBinding: FragmentStatisticBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        fragmentStatisticBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistic,container,false)
        fragmentStatisticBinding.lifecycleOwner = this
        return fragmentStatisticBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscriberToObservers()
    }

    @SuppressLint("SetTextI18n")
    private fun subscriberToObservers(){
        statisticViewModels.totalTimeRun.observe(viewLifecycleOwner,{
            it?.let {
                fragmentStatisticBinding.tvTotalTime.text = TrackingUtility.getFormattedStopWatchTime(it)
            }
        })
        statisticViewModels.totalDistance.observe(viewLifecycleOwner,{
            it?.let {
                val km = it / 1000f
                "${round(km * 10f) / 10f}km".also { fragmentStatisticBinding.tvTotalDistance.text = it }
            }
        })
        statisticViewModels.totalAvgSpeed.observe(viewLifecycleOwner,{
            it?.let {
                "${round(it * 10f) / 10f}km/h".also { fragmentStatisticBinding.tvAverageSpeed.text = it }
            }
        })
        statisticViewModels.totalCalories.observe(viewLifecycleOwner,{
            it?.let {
                fragmentStatisticBinding.tvTotalCalories.text = "${it}kcal"
            }
        })
    }
}