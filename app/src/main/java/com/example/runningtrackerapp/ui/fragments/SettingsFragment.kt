package com.example.runningtrackerapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.databinding.FragmentSettingsBinding
import com.example.runningtrackerapp.other.Constants.KEY_NAME
import com.example.runningtrackerapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var fragmentSettingsBinding: FragmentSettingsBinding
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentSettingsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings,container,false)
        fragmentSettingsBinding.lifecycleOwner = this
        return fragmentSettingsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSharedPreferences()
        fragmentSettingsBinding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success){
                Snackbar.make(
                   view,
                    "Saved Changes",
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                Snackbar.make(
                    view,
                    "Please Fill out All Fields",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun loadSharedPreferences(){
        val name = sharedPreferences.getString(KEY_NAME,"")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT,80f)
        fragmentSettingsBinding.etName.setText(name)
        fragmentSettingsBinding.etWeight.setText(weight.toString())

    }
    private fun applyChangesToSharedPref():Boolean{
      val nameText = fragmentSettingsBinding.etName.text.toString()
      val weight = fragmentSettingsBinding.etWeight.text.toString()
      if(nameText.isEmpty() || weight.isEmpty()){
             return false
      }
       sharedPreferences.edit()
           .putString(KEY_NAME,nameText)
           .putFloat(KEY_WEIGHT,weight.toFloat())
           .apply()
        val toolBar = "Let's go $nameText"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolBar
        return true

    }

}