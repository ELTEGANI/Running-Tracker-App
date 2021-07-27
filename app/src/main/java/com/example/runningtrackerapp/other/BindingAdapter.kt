package com.example.runningtrackerapp.other

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.runningtrackerapp.db.Run
import java.text.SimpleDateFormat
import java.util.*



@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, run: Run) {
    Glide.with(imageView.context)
        .load(run.image)
        .into(imageView)
}


@BindingAdapter("tvDate")
fun TextView.setDateTextView(run: Run) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = run.timeStamp
    }
    run.let {
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        text = dateFormat.format(calendar.time)
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("tvDistance")
fun TextView.setDistanceTextView(run: Run) {
    run.let {
        text = "${run.distanceInMeters / 1000f}km"
    }
}



@BindingAdapter("tvTime")
fun TextView.setTimeTextView(run: Run) {
    run.let {
        text = TrackingUtility.getFormattedStopWatchTime(run.timeInMilis)
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("tvCalories")
fun TextView.setTvCaloriesTextView(run: Run) {
    run.let {
        text = "${run.caloriesBurned}kcal"
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("tvAvgSpeed")
fun TextView.setAvgSpeedTextView(run: Run) {
    run.let {
        text = "${run.avgSpeedInKmh}km/h"
    }
}


