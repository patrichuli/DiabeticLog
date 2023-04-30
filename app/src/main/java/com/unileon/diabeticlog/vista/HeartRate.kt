package com.unileon.diabeticlog.vista

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.modelo.ConexionSQLHelper


/**
 *
 * This class allows you to record heart rate related data
 */

class HeartRate : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)

        //shows the button to go back to the registration page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        //create
        //createLineChart()

    }

    //arrow to go back
    override fun onSupportNavigateUp(): Boolean {

        onBackPressed()
        return true
    }

    //create the menu with the save and advices options
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_cards, menu)
        return true
    }

    //If you press the save button, the data will be saved and if you click on tips,
    // certain tips about heart rate will be displayed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog para mostrar los consejos
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Consejos sobre la frecuencia cardíaca")
                .setMessage(Html.fromHtml("<br>La frecuencia cardíaca en reposo se refiere a la cantidad de veces que el corazón late por minuto cuando se está en reposo. Es un indicador importante de la salud cardiovascular, y el mejor momento " +
                        "para medirla es justo al despertarse por la mañana. El valor ideal de frecuencia cardíaca en reposo está entre <b>50 y 80 ppm</b>. " +
                        "Las personas con valores fuera de este rango tienen más probabilidades de desarrollar enfermedades cardiovasculares.<br><br>" +
                        "Para mejorar la frecuencia cardíaca a largo plazo:<br><br>" +
                        "- Realizar ejercicio regularmente<br>" +
                        "- Mantenerse hidratado<br>" +
                        "- Limitar la ingesta de estimulantes, como la cafeína y la nicotina<br>" +
                        "- Limitar el consumo de alcohol<br>" +
                        "- Llevar una dieta saludable y equilibrada<br>" +
                        "- Dormir lo suficiente<br>" +
                        "- Mantener un peso corporal saludable<br>" +
                        "- Reducir o eliminar las fuentes de estrés considerable a largo plazo<br>" +
                        "- Buscar asesoramiento o servicios psicológicos<br>" +
                        "- Salir al aire libre<br>" +
                        "- Practicar técnicas de relajación<br>"))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

        //call to the database
        val mysql = ConexionSQLHelper(this)


        return when (item.itemId) {
            R.id.consejos -> {
                dialog.show()
                true
            }
            R.id.guardar -> {

                startActivity(Intent(this, Principal::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //this function create the glucose graph
  /*  @RequiresApi(Build.VERSION_CODES.O)
    fun createLineChart() {

        //variable
        val chart: LineChart = findViewById(R.id.chartHeartRatio)

        //characteristics of the line chart
        chart.setTouchEnabled(true)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.getDescription().setEnabled(false)
        chart.getLegend().setEnabled(false)

        //marker to see the largest values
        val markerView = MyMarkerView(
            this,
            R.layout.custom_maker_view
        )
        if (markerView != null) {
            markerView.setChartView(chart)
        }
        chart.marker = markerView

        //the function is called to render the data of the graph
        renderData(chart)

    }

    //this function render the data
    @RequiresApi(Build.VERSION_CODES.O)
    fun renderData(chart: LineChart) {

        //X axis
        val xAxis: XAxis = chart.getXAxis()

        xAxis.axisMaximum = 23f
        xAxis.valueFormatter =
            MyAxisXValueFormatter()

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setDrawGridLines(false)
        xAxis.setLabelCount(5, true)
        xAxis.setGranularity(1f)

        //Y axis
        val leftAxis: YAxis = chart.getAxisLeft()
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)

        leftAxis.axisMaximum = 220F
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawZeroLine(false)
        leftAxis.setDrawLimitLinesBehindData(false)
        leftAxis.setLabelCount(5,false)

        chart.getAxisRight().setEnabled(false)

        //call the function to sets the data
        setData(chart)
    }

    //this function sets the data that appears on the graph
    private fun setData(chart: LineChart) {

        //variables
        val values: ArrayList<Entry> = ArrayList()
        val mysql = ConexionSQLHelper(this)

        //heart ratio values are entered in the graph
        val listaGlucosa = mysql.readAllGlucoseTable()

        //glucose values are entered in the graph
        for (i in 0..listaGlucosa.size-1) {
            val datosGlucosa = listaGlucosa.get(i).datos.toString().split(" ").get(0)
            val y = datosGlucosa.toFloat()
            val hours = listaGlucosa.get(i).hora.toString().split(":").get(0)
            val minutes = listaGlucosa.get(i).hora.toString().split(":").get(1)
            val x = (hours + "." + minutes).toFloat()

            values.add(Entry(x, y))
        }

        val set1: LineDataSet
        if (chart.getData() != null &&
            chart.getData().getDataSetCount() > 0
        ) {
            set1 = chart.getData().getDataSetByIndex(0) as LineDataSet
            set1.setValues(values)
            chart.getData().notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            set1 = LineDataSet(values, "Ritmo cardíaco")

            if(listaGlucosa.size == 0) {
                set1.setDrawValues(false)
                set1.setDrawCircles(false)
                set1.isHighlightEnabled = false
            } else {
                set1.setDrawIcons(false)

                // draw dashed line
                //  set1.enableDashedLine(10f, 5f, 0f)
                //  set1.enableDashedHighlightLine(10f, 5f, 0f)

                // black lines and points
                set1.color = Color.DKGRAY
                set1.mode = LineDataSet.Mode.CUBIC_BEZIER
                set1.setCircleColor(Color.DKGRAY)

                // line thickness and point size
                set1.lineWidth = 1f
                set1.circleRadius = 3f

                // draw points as solid circles
                set1.setDrawCircleHole(false)

                // text size of values
                set1.valueTextSize = 12f

                // set the filled area
                set1.setDrawFilled(false)

                // customize legend entry
                set1.formLineWidth = 1f
                set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
                set1.formSize = 15f

                // set color of filled area
                /*   if (Utils.getSDKInt() >= 18) {
                       val drawable =
                           context?.let { ContextCompat.getDrawable(it, R.drawable.fade_blue) }
                       set1.fillDrawable = drawable
                   } else {
                       set1.fillColor = Color.DKGRAY
                   }*/

            }

            val dataSets: ArrayList<ILineDataSet> = ArrayList()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            //set data
            chart.setData(data)
        }
    }*/
}