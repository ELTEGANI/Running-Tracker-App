package com.example.runningtrackerapp.other

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runningtrackerapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDailog :DialogFragment() {
    private var yesListener:(() -> Unit)? = null
    fun setYesListener(listener:()->Unit){
        yesListener = listener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run")
            .setMessage("Are You Sure To Cancel The Current Run")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){_,_->
               yesListener?.let {yes->
                   yes()
               }
            }
            .setNegativeButton("No"){dialogInterface,_,->
                dialogInterface.cancel()
            }.create()
    }
}