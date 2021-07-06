package com.example.runningtrackerapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.runningtrackerapp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModels @Inject constructor(
    val mainRepository: MainRepository
):ViewModel(){
}