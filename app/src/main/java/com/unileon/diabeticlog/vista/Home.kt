package com.unileon.diabeticlog.vista

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.chart.MyAxisXValueFormatter
import com.unileon.diabeticlog.controlador.chart.MyAxisYValueFormatter
import com.unileon.diabeticlog.controlador.chart.MyMarkerView
import com.unileon.diabeticlog.controlador.data.DatosRegistrados
import com.unileon.diabeticlog.controlador.googleFit.GoogleFitness
import com.unileon.diabeticlog.controlador.notification.MyReceiver
import com.unileon.diabeticlog.controlador.recycler.RecyclerViewAdapter
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 *
 *
 * This class allows you to view the data registered in the
 * registration window and to be able to access them to edit or delete them
 */

class Home : Fragment(), RecyclerViewAdapter.OnItemClickListener {

    private lateinit var mostrarGrafica: CombinedChart
    private lateinit var mostrarDatos: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant", "ResourceAsColor")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val vista = inflater.inflate(R.layout.fragment_home, container, false)

        //init variables
        mostrarDatos = vista.findViewById(R.id.listaDatos)
        mostrarGrafica = vista.findViewById(R.id.chart)

        //show or not chart and list
        showChartOrRecyclerView(noMuestraTodo, muestraTodo)

        //shows the menu
        setHasOptionsMenu(true)



        //starts the recyclerview
        val recyclerView: RecyclerView = vista.findViewById(R.id.listaDatos)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        //color of the divider of each item in the list
        val didv = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        didv.setDrawable(ColorDrawable(R.color.black))
        recyclerView.addItemDecoration(didv)

        //obtain the data from the database
        val mysql = ConexionSQLHelper(context)
        listaDatos = mysql.readAllDataBase()


        // Check if user is signed in (non-null) and update UI accordingly.
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if(account?.email != null) {
            onlyIfHasPermissions()
        }

    /*    val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleClient = context?.let { GoogleSignIn.getClient(it, googleConf) }

        if (googleClient != null) {
            startActivityForResult(googleClient.signInIntent, 1)
        }*/

        //show the data list in the recycler
        val adapter = RecyclerViewAdapter(listaDatos, this)
        recyclerView.adapter = adapter

        //call the function to create the notification push
        val lista = containsAllData(listaDatos)
        establecerAlarmaClick(4, lista)

        //call the function to create the graph
        createLineChart(vista)

        return vista
    }


    //create a menu with several options
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_inicio, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // this function allows the user to change home layout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        return when (item.itemId) {
            R.id.graficaDatos -> {
                noMuestraTodo = true
                muestraTodo = true
                showChartOrRecyclerView(true, true)
                true
            }
            R.id.soloDatos -> {
                noMuestraTodo = true
                muestraTodo = false
                showChartOrRecyclerView(true, false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //function to show only the list or the list with the graph
    fun showChartOrRecyclerView(soloDatos: Boolean, datosConGrafica: Boolean) {

        if(soloDatos && !datosConGrafica) {
            mostrarDatos.visibility = View.VISIBLE
            mostrarGrafica.visibility = View.GONE
        } else if(soloDatos && datosConGrafica) {
            mostrarDatos.visibility = View.VISIBLE
            mostrarGrafica.visibility = View.VISIBLE
        }

    }


    //create the alarm and the notification of the mobile
    @RequiresApi(Build.VERSION_CODES.N)
    private fun establecerAlarmaClick(`when`: Int, lista: Array<String>) {
        val manager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // val pIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        //manager[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + `when` * 1000] = pIntent
        val intent = Intent(context, MyReceiver::class.java)
        intent.putExtra("listaDatos", lista)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        //we set the time the alarm repeats
        val calendar: android.icu.util.Calendar = android.icu.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 15)
        }

        //we establish how often we want it to be repeated
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

    }

    //if it contains all the data, some or none of the insulin, food or sport, it returns one message or another
    fun containsAllData(lista: ArrayList<DatosRegistrados>): Array<String> {

        //variables
        var containsInsulina: Boolean = false
        var containsActividadFisica: Boolean = false
        var containsAlimentacion: Boolean = false
        var titulo: String = ""
        var mensaje: String = ""

        for(i in 0 until lista.size){
            if(lista[i].tipoRegistro == "Insulina"){
                containsInsulina = true
            } else if(lista[i].tipoRegistro == "Actividad fisica"){
                containsActividadFisica = true
            } else if(lista[i].tipoRegistro == "Alimentacion"){
                containsAlimentacion = true
            }
        }

        //different messages are set for notification
        if(!containsActividadFisica && containsInsulina && containsAlimentacion){
            titulo = "Recordatorio de registro!"
            mensaje = "¿No has realizado ninguna actividad hoy? Es importante realizar deporte."
        } else if(!containsInsulina && containsAlimentacion){
            titulo = "Recordatorio de registro!"
            mensaje = "No olvide registrar la insulina aplicada o que se tiene que aplicar."
        } else if(!containsAlimentacion && containsInsulina){
            titulo = "Recordatorio de registro!"
            mensaje = "No olvide registrar la ingesta de carbohidratos."
        } else if(!containsInsulina && !containsAlimentacion) {
            titulo = "Recordatorio de registro!"
            mensaje = "No olvide registrar la ingesta de carbohidratos o la insulina aplicada."
        } else if(containsInsulina && containsActividadFisica && containsAlimentacion){
            titulo = "Enhorabuena!!"
            mensaje = "Hoy has realizado un excelente trabajo con tu diario."
        }

        val devolver = arrayOf(titulo, mensaje)

        return devolver
    }

    //if you click on the item of insulin, feeding or activity
    // it takes you to a screen to be able to edit or delete it
    override fun onItemClick(position: Int) {
        val tipo: String = listaDatos.get(position).tipoRegistro.toString()

        if(tipo == "Insulina") {
            val intent = Intent(context, EditDeleteInsulin::class.java)
            intent.putExtra("id", listaDatos.get(position).id)
            intent.putExtra("nombre", listaDatos.get(position).nombre)
            intent.putExtra("fecha", listaDatos.get(position).fecha)
            intent.putExtra("hora", listaDatos.get(position).hora)
            startActivity(intent)
        } else if(tipo == "Actividad fisica") {
            val intent = Intent(context, EditDeleteSport::class.java)
            intent.putExtra("id", listaDatos.get(position).id)
            intent.putExtra("nombre", listaDatos.get(position).nombre)
            intent.putExtra("fecha", listaDatos.get(position).fecha)
            intent.putExtra("hora", listaDatos.get(position).hora)
            startActivity(intent)
        } else if(tipo == "Alimentacion") {
            val intent = Intent(context, EditDeleteFeeding::class.java)
            intent.putExtra("id", listaDatos.get(position).id)
            intent.putExtra("nombre", listaDatos.get(position).nombre)
            intent.putExtra("fecha", listaDatos.get(position).fecha)
            intent.putExtra("hora", listaDatos.get(position).hora)
            startActivity(intent)
        } else if(tipo == "Glucosa") {
            val intent = Intent(context, EditDeleteGlucose::class.java)
            intent.putExtra("id", listaDatos.get(position).id)
            intent.putExtra("datos", listaDatos.get(position).datos)
            intent.putExtra("fecha", listaDatos.get(position).fecha)
            intent.putExtra("hora", listaDatos.get(position).hora)
            startActivity(intent)
        }
    }


    //this function create the glucose graph
    @RequiresApi(Build.VERSION_CODES.O)
    fun createLineChart(vista: View) {

        //variable
        val chart: CombinedChart = vista.findViewById(R.id.chart)

        // enable touch gestures
        chart.setTouchEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)

        // disable scaling
        // enable scaling and dragging
        chart.setScaleEnabled(true)
        chart.setScaleXEnabled(true)
        chart.setScaleYEnabled(false)

        // set listeners
      //  chart.setOnChartValueSelectedListener(this)

        //disable description and legend text
        chart.getDescription().setEnabled(false)
        chart.getLegend().setEnabled(false)

        //draw bars behind lines
        chart.drawOrder = arrayOf(DrawOrder.BAR, DrawOrder.LINE)


        //marker to see the largest values
        val markerView = context?.let {
            MyMarkerView(
                it,
                R.layout.custom_maker_view
            )
        }
        if (markerView != null) {
            markerView.setChartView(chart)
        }

        if(listaDatos.size != 0) {
            chart.marker = markerView
        }


        //the function is called to render the data of the graph
        renderData(chart)

    }

    //this function render the data
    @RequiresApi(Build.VERSION_CODES.O)
    fun renderData(chart: CombinedChart) {

        //X axis
        val xAxis: XAxis = chart.getXAxis()

        xAxis.axisMaximum = 23f
        xAxis.axisMinimum = 0f
        xAxis.valueFormatter =
            MyAxisXValueFormatter()

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE)
        xAxis.setDrawGridLines(false)
        xAxis.setLabelCount(5, true)
        xAxis.setGranularity(1f)

        //maximun limit line
        val maximunll = LimitLine(180f, "")
        maximunll.lineWidth = 1f
        maximunll.lineColor = Color.DKGRAY

        //minimun limit line
        val minimunll = LimitLine(70f, "")
        minimunll.lineWidth = 1f
        minimunll.lineColor = Color.DKGRAY
       // ll2.enableDashedLine(10f, 10f, 0f)
      //  ll2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
      //  ll2.textSize = 10f

        //Y axis
        val leftAxis: YAxis = chart.getAxisLeft()

        chart.getAxisRight().setEnabled(false)
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
     //   leftAxis.removeAllLimitLines()

        leftAxis.axisMaximum = 350F
        leftAxis.valueFormatter =
            MyAxisYValueFormatter()
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawZeroLine(false)
        leftAxis.setLabelCount(3,false)


        leftAxis.setDrawLimitLinesBehindData(true)
        leftAxis.addLimitLine(minimunll)
        leftAxis.addLimitLine(maximunll)

        val data = CombinedData()

        data.setData(generateLineData(chart))
        data.setData(generateBarData())

        chart.data = data
        //call the function to sets the data
       // setData(chart)
  }


  //this function sets the data that appears on the graph
  private fun generateLineData(chart: CombinedChart): LineData {


      val dataSets: ArrayList<ILineDataSet> = ArrayList()

      val setGlucose: LineDataSet = setDataGlucose(chart)
      val setInsuline: LineDataSet = setDataInsuline(chart)
      val setFeeding: LineDataSet = setDataFeeding(chart)
      dataSets.add(setGlucose) // add the data sets
      dataSets.add(setInsuline)
      dataSets.add(setFeeding)

      // create a data object with the data sets
      val data = LineData(dataSets)

      //set data
     // chart.setData(data)
      return data


  }

    //this function sets the data of glucose
    fun setDataGlucose(chart: CombinedChart): LineDataSet {

        //variables
        val values: ArrayList<Entry> = ArrayList()
        val mysql = ConexionSQLHelper(context)
        val listaGlucosa = mysql.readAllGlucoseTable()

        //glucose values are entered in the graph
        if(listaGlucosa.size != 0) {
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
                set1 = LineDataSet(values, "Glucosa")

                if(listaGlucosa.size == 0){
                    set1.setDrawValues(false)
                    set1.setDrawCircles(false)
                    set1.isHighlightEnabled = false

                } else {
                    set1.setDrawIcons(false)

                    // draw dashed line
                    //  set1.enableDashedLine(10f, 5f, 0f)
                    //  set1.enableDashedHighlightLine(10f, 5f, 0f)

                    // black lines and points
                    set1.color = Color.RED
                    set1.mode = LineDataSet.Mode.CUBIC_BEZIER
                    set1.setCircleColor(Color.RED)

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

            }

            return set1

        } else {
            values.add(Entry(0f,0f))

            val set1: LineDataSet
            if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0
            ) {
                set1 = chart.getData().getDataSetByIndex(0) as LineDataSet
                set1.setValues(values)
                chart.getData().notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                set1 = LineDataSet(values, "Glucosa")
                set1.setDrawValues(false)
                set1.setDrawCircles(false)
                set1.isHighlightEnabled = false

            }

            return set1
        }



    }

    //this function sets the data of insuline
    fun setDataInsuline(chart: CombinedChart): LineDataSet {

        //variables
        val values: ArrayList<Entry> = ArrayList()
        val mysql = ConexionSQLHelper(context)
        val listaInsulina = mysql.readAllInsulinTable()

        //insuline values are entered in the graph
        if(listaInsulina.size != 0) {
            for (i in 0..listaInsulina.size-1) {
                val datosInsulina = listaInsulina.get(i).datos.toString().split(" ").get(0)
                val y = datosInsulina.toFloat()
                val hours = listaInsulina.get(i).hora.toString().split(":").get(0)
                val minutes = listaInsulina.get(i).hora.toString().split(":").get(1)
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
                set1 = LineDataSet(values, "Insulina")

                if(listaInsulina.size == 0){
                    set1.setDrawValues(false)
                    set1.setDrawCircles(false)
                    set1.isHighlightEnabled = false

                } else {
                    set1.setDrawIcons(false)

                    // draw dashed line
                    //  set1.enableDashedLine(10f, 5f, 0f)
                    //  set1.enableDashedHighlightLine(10f, 5f, 0f)

                    // black lines and points
                    set1.color = Color.YELLOW
                    set1.mode = LineDataSet.Mode.CUBIC_BEZIER
                    set1.setCircleColor(Color.YELLOW)

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

                }

            }

            return set1

        } else {
            values.add(Entry(0f, 0f))

            val set1: LineDataSet
            if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0
            ) {
                set1 = chart.getData().getDataSetByIndex(0) as LineDataSet
                set1.setValues(values)
                chart.getData().notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                set1 = LineDataSet(values, "Insulina")
                set1.setDrawValues(false)
                set1.setDrawCircles(false)
                set1.isHighlightEnabled = false

            }

            return set1
        }



    }

    //this function sets the data of feeding
    fun setDataFeeding(chart: CombinedChart): LineDataSet {

        //variables
        val values: ArrayList<Entry> = ArrayList()
        val mysql = ConexionSQLHelper(context)
        val listaAlimentos = mysql.readAllFeedingTable()

        //feeding values are entered in the graph
        if(listaAlimentos.size != 0) {
            for (i in 0..listaAlimentos.size-1) {
                val datosAlimentos = listaAlimentos.get(i).datos.toString().split(" ").get(0)
                val y = datosAlimentos.toFloat()
                val hours = listaAlimentos.get(i).hora.toString().split(":").get(0)
                val minutes = listaAlimentos.get(i).hora.toString().split(":").get(1)
                val x = (hours + "." + minutes).toFloat()

                values.add(Entry(x, y.toFloat()))
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
                set1 = LineDataSet(values, "Alimentacion")

                if(listaAlimentos.size == 0){
                    set1.setDrawValues(false)
                    set1.setDrawCircles(false)
                    set1.isHighlightEnabled = false

                } else {
                    set1.setDrawIcons(false)

                    // draw dashed line
                    //  set1.enableDashedLine(10f, 5f, 0f)
                    //  set1.enableDashedHighlightLine(10f, 5f, 0f)

                    // black lines and points
                    set1.color = Color.BLUE
                    set1.mode = LineDataSet.Mode.CUBIC_BEZIER
                    set1.setCircleColor(Color.BLUE)

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

                }

            }

            return set1

        } else {
            values.add(Entry(0f,0f))

            val set1: LineDataSet
            if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0
            ) {
                set1 = chart.getData().getDataSetByIndex(0) as LineDataSet
                set1.setValues(values)
                chart.getData().notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                set1 = LineDataSet(values, "Alimentacion")
                set1.setDrawValues(false)
                set1.setDrawCircles(false)
                set1.isHighlightEnabled = false
            }

            return set1

        }



    }




    fun generateBarData(): BarData {

        //variables
        val values: ArrayList<BarEntry> = ArrayList()
        val bar = BarData()
        val mysql = ConexionSQLHelper(context)
        val listaDeporte = mysql.readAllSportTable()

        //sport values are entered in the graph
        if(listaDeporte.size != 0) {
            for (i in 0..listaDeporte.size-1) {
                val hours = listaDeporte.get(i).hora.toString().split(":").get(0)
                val minutes = listaDeporte.get(i).hora.toString().split(":").get(1)
                val x = (hours + "." + minutes).toFloat()

                val parts: List<String> = listaDeporte.get(i).datos.toString().split(" ")
                var hoursDuration = ""
                var minutesDuration = ""
                if (parts.size > 2) {
                    hoursDuration = parts.get(0)
                    minutesDuration = parts.get(2)
                } else {
                    if (parts.get(1).equals("h")) {
                        hoursDuration = parts.get(0)
                    } else {
                        minutesDuration = parts.get(0)
                    }
                }

                var h = 0
                var m: Int

                if (hoursDuration !== "") {
                    h = hoursDuration.toInt() * 60
                }

                m = if (minutesDuration == "") {
                    h
                } else {
                    minutesDuration.toInt() + h
                }


                values.add(BarEntry(x, m.toFloat()))
            }

            val set = BarDataSet(values, "Bar DataSet")
            set.setColor(Color.rgb(60, 220, 78))
            // set.setValueTextColor(Color.rgb(60, 220, 78))
            set.setValueTextSize(12f)
            //   set.setHighLightColor(Color.RED)
            set.setAxisDependency(YAxis.AxisDependency.LEFT)

            bar.addDataSet(set)

            return bar

        } else {
            values.add(BarEntry(0f,0f))

            val set = BarDataSet(values, "Bar DataSet")
            set.setColor(Color.rgb(60, 220, 78))
            // set.setValueTextColor(Color.rgb(60, 220, 78))
            set.setValueTextSize(12f)
            //   set.setHighLightColor(Color.RED)
            set.setAxisDependency(YAxis.AxisDependency.LEFT)
            set.setDrawValues(false)
            bar.addDataSet(set)

            return bar
        }


    }

    //access to data of the client of fitness
    @RequiresApi(Build.VERSION_CODES.O)
    fun onlyIfHasPermissions() {
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
            .accessActivitySessions(FitnessOptions.ACCESS_READ)
            .build()

        val account = context?.let { GoogleSignIn.getAccountForExtension(it, fitnessOptions) }
        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            val fit = GoogleFitness()
            context?.let { fit.accessGoogleFit(fitnessOptions, it) }
        }

    }

    //create a client of fitness
 /*   @RequiresApi(Build.VERSION_CODES.O)
    fun createApiFitness() {
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
            .accessActivitySessions(FitnessOptions.ACCESS_READ)
            .build()

        val account = context?.let { GoogleSignIn.getAccountForExtension(it, fitnessOptions) }

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                account,
                fitnessOptions
            )
        } else {
            val fit = GoogleFitness()
            context?.let { fit.accessGoogleFit(fitnessOptions, it) }
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fit = GoogleFitness()
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> context?.let {
                    fit.accessGoogleFit(
                        fitnessOptions,
                        it
                    )
                }
                else -> {
                    // Result wasn't from Google Fit
                    Log.i(TAG, "Result wasn't from Google Fit")
                }
            }
            else -> {
                // Permission not granted
                Log.i(TAG, "Permission not granted")
            }
        }/*
        if(requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if(account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(context, "OLEEEE", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "MIERDA", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }*/
    }



  companion object {

      @JvmStatic
      fun newInstance() = Home()
      var listaDatos = ArrayList<DatosRegistrados>()
      var muestraTodo = false
      var noMuestraTodo = false
      lateinit var fitnessOptions: FitnessOptions
      val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
      val TAG = "GoogleFit"
  }
}