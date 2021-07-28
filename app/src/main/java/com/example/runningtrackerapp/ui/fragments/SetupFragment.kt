package com.example.runningtrackerapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentSetupBinding
import com.example.runningtrackerapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningtrackerapp.other.Constants.KEY_NAME
import com.example.runningtrackerapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class SetupFragment : Fragment() {
    private lateinit var setupBinding: FragmentSetupBinding
    @Inject
    lateinit var sharedPref:SharedPreferences

    @set:Inject
    var isFirstTimeOpen = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?
    ): View {
        if(!isFirstTimeOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment2,true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment2_to_runFragment2,
                savedInstanceState,
                navOptions
            )
        }
        setupBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup,container,false)
        setupBinding.lifecycleOwner = this

        setupBinding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPreference()
            if(success){
                findNavController().navigate(R.id.action_setupFragment2_to_runFragment2)
            }else{
                Snackbar.make(
                    requireView(),
                    "Please Enter all The Fields",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        return setupBinding.root
    }

    private fun writePersonalDataToSharedPreference():Boolean{
        val name = setupBinding.etName.text.toString()
        val weight = setupBinding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()
        val toolBar = "Let's go $name"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolBar
        return true
    }
}