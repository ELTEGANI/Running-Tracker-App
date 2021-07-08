package com.example.runningtrackerapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentSetupBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SetupFragment : Fragment() {
    private lateinit var setupBinding: FragmentSetupBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?
    ): View {
        setupBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup,container,false)
        setupBinding.lifecycleOwner = this

        setupBinding.tvContinue.setOnClickListener {
            findNavController().navigate(R.id.action_setupFragment2_to_runFragment2)
        }
        return setupBinding.root
    }

}