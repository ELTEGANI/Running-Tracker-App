package com.example.runningtrackerapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.runningtrackerapp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModels @Inject constructor(
    val mainRepository: MainRepository
): ViewModel(){
    val totalTimeRun = mainRepository.getTotalTimeInMilis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCalories = mainRepository.getTotalColariesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

}