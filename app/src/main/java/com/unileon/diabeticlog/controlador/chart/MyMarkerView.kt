package com.unileon.diabeticlog.controlador.chart

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.vista.Home.Companion.listaDatos


/**
 * Custom implementation of the MarkerView.
 *
 * @author Patricia Gonzalez
 */

class MyMarkerView(context: Context, layoutResource: Int):  MarkerView(context, layoutResource) {

    private val valueData: TextView
    private val valueName: TextView
    private val valueImage: ImageView

    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        val value = entry?.y?.toInt() ?: 0
        val valueX = entry?.x.toString()
        var hours = valueX.split(".")[0]
        var minutes = valueX.split(".")[1]
        if(minutes.length < 2) {
            minutes += "0"
        }
        if(hours.length < 2) {
            hours = "0$hours"
        }
        val total = "$hours:$minutes"
        var unidades = ""

        for(i in 0 until listaDatos.size) {
            if(listaDatos[i].datos.toString().split(" ")[0] == value.toString() && listaDatos[i].hora == total) {

                unidades = listaDatos.get(i).datos.toString().split(" ").get(1)
                valueName.setText(listaDatos.get(i).nombre)
                System.out.println(listaDatos.get(i).imagen)
                if(listaDatos.get(i).imagen.equals(0)) {
                    valueImage.setImageBitmap(listaDatos.get(i).imagenBitmap)

                } else {
                    valueImage.setImageResource(listaDatos.get(i).imagen)
                }

                var resText = ""
                resText = if(value.toString().length > 8){
                    value.toString().substring(0,7)
                } else{
                    "$value $unidades"
                }
                valueData.text = resText
                super.refreshContent(entry, highlight)
            } else if(listaDatos.get(i).datoAdicional.toString().split(" ")[0] == value.toString() && listaDatos.get(i).hora == total) {

                unidades = listaDatos.get(i).datoAdicional.toString().split(" ").get(1)
                valueName.setText(listaDatos.get(i).nombre)
                valueImage.setImageResource(listaDatos.get(i).imagen)
                var resText = ""
                resText = if(value.toString().length > 8){
                    value.toString().substring(0,7)
                } else{
                    "$value $unidades"
                }
                valueData.text = resText
                super.refreshContent(entry, highlight)
            }

        }

    }

    override fun getOffsetForDrawingAtPoint(xpos: Float, ypos: Float): MPPointF {
        return MPPointF(-width / 2f, -height - 10f)
    }

    init {
        valueData = findViewById<View>(R.id.valoresGrafica) as TextView
        valueName = findViewById<View>(R.id.marker_nombre) as TextView
        valueImage = findViewById<View>(R.id.marker_imagen) as ImageView
    }
}
