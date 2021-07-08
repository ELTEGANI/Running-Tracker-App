package com.example.runningtrackerapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentRunBinding
import com.example.runningtrackerapp.databinding.FragmentSetupBinding
import com.example.runningtrackerapp.ui.viewmodels.MainViewModels
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RunFragment : Fragment() {

    private val mainViewModels: MainViewModels by viewModels()
    private lateinit var fragmentRunBinding: FragmentRunBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentRunBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_run,container,false)
        fragmentRunBinding.lifecycleOwner = this

        fragmentRunBinding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment2_to_trackingFragment)
        }
        return fragmentRunBinding.root
    }

}