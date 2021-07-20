package com.example.runningtrackerapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtrackerapp.db.Run
import com.example.runningtrackerapp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModels @Inject constructor(
    val mainRepository: MainRepository
):ViewModel(){

    fun insertRun(run: Run) = viewModelScope.launch{
          mainRepository.insertRun(run)
    }
}