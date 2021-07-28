package com.example.runningtrackerapp.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentStatisticBinding
import com.example.runningtrackerapp.other.CustomMarkerView
import com.example.runningtrackerapp.other.TrackingUtility
import com.example.runningtrackerapp.ui.viewmodels.StatisticViewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
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
        setUpBarChart()
    }

    private fun setUpBarChart(){
        fragmentStatisticBinding.barChart.xAxis.apply{
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        fragmentStatisticBinding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        fragmentStatisticBinding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        fragmentStatisticBinding.barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false

        }
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
        statisticViewModels.runSortedByDate.observe(viewLifecycleOwner,{
            it?.let {
                val allAvgSpeed = it.indices.map {
                    i->BarEntry(i.toFloat(),it[i].avgSpeedInKmh)
                }
                val barDataSet = BarDataSet(allAvgSpeed,"Avg Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                fragmentStatisticBinding.barChart.data = BarData(barDataSet)
                fragmentStatisticBinding.barChart.marker = CustomMarkerView(it.reversed()
                ,requireContext(),R.layout.marker_view)
                fragmentStatisticBinding.barChart.invalidate()
            }
        })
    }
}