package com.unileon.diabeticlog.controlador.chart

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyAxisXValueFormatter : IAxisValueFormatter {

    private val mFormat: SimpleDateFormat

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        val millis: Long = TimeUnit.HOURS.toMillis(value.toLong())
        return mFormat.format(Date(millis))
    }

    init {
        mFormat = SimpleDateFormat("HH:mm")
    }
}