package com.unileon.diabeticlog.controlador.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

class MyAxisYValueFormatter : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase): String {

        if(value.toInt() == 0){
            return ""
        } else {
            return value.toInt().toString() + " mg/L"
        }

    }

}
