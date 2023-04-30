package com.unileon.diabeticlog.vista

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlGlucose
import java.lang.String
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * This class allows you to record glucose related data
 */
class Glucose : AppCompatActivity() {

    private val servidor = MetodosServidor()
    //variable
    private var tieneCicloMenstrual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glucose)

        //shows the button to go back to the registration page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        // call the function to enter the insulin application time
        val muestraHora: TextInputEditText = findViewById(R.id.muestraHoraGlucosa)
        muestraHora.setOnClickListener { getGlucoseApplicationHour() }

        //shows the date of the current day
        val mostrarFecha: TextView = findViewById(R.id.mostrarFecha)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        mostrarFecha.setText(currentDate)

        //variables
        val muestraInicioCiclo: TextInputEditText = findViewById(R.id.muestraInicioCiclo)
        val muestraFinCiclo: TextInputEditText = findViewById(R.id.muestraFinCiclo)

        //clicking lets you choose a date
        muestraInicioCiclo.setOnClickListener { getMenstrualCycleStartDate() }
        muestraFinCiclo.setOnClickListener { getMenstrualCycleEndDate() }


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
    // certain tips about glucose will be displayed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog to show the advices
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Consejos sobre la glucosa")
                .setMessage(
                        Html.fromHtml("<br>Mantener los niveles de glucosa en las siguientes cifras te ayudará a llevar un control óptimo de la diabetes:<br>" +
                                "<br>" +
                                "• 70-130 mg/dl en ayunas (en ayuno por la mañana)<br>" +
                                "• Menos de 180 mg/dl a las dos horas después de comer<br>" +
                                "• 100-130 mg/dl antes de ir a dormir <br><br>" +
                                "<b>1.</b> Compra a conciencia.<br> " +
                                "<b>2.</b> Consigue enlatados sin sal.<br>" +
                                "<b>3.</b> Llena tu despensa de productos saludables.<br>" +
                                "<b>4.</b> Elige carnes magras y lácteos sin grasa.<br>" +
                                "<b>5.</b> Opta por las grasas saludables.<br>" +
                                "<b>6.</b> Vigila tu peso.<br>" +
                                "<b>7.</b> Realiza actividad física.<br>" +
                                "<b>8.</b> Descansa lo suficiente.<br>" +
                                "<b>9.</b> Reduce el estrés.<br>" +
                                "<b>10.</b> Modera el consumo de alcohol.<br>"))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

        //call to the database
        val mysql = ConexionSQLHelper(this)

        //variables to insert
        val horaGlucosa: TextInputEditText = findViewById(R.id.muestraHoraGlucosa)
        val nivelesGlucosa: EditText = findViewById(R.id.muestraNivelesGlucosa)
        val inicioCiclo: TextInputEditText = findViewById(R.id.muestraInicioCiclo)
        val finCiclo: TextInputEditText = findViewById(R.id.muestraFinCiclo)


        return when (item.itemId) {
            R.id.consejos -> {
                dialog.show()
                true
            }
            R.id.guardar -> {
                if(nivelesGlucosa.text.toString() == ""){
                    Toast.makeText(this, "Nivel de glucosa requerido", Toast.LENGTH_SHORT).show()
                } else if(horaGlucosa.text.toString() == "") {
                    Toast.makeText(this, "Hora de aplicación requerida", Toast.LENGTH_SHORT).show()
                } else {
                    mysql.insertGlucose(horaGlucosa.text.toString(), nivelesGlucosa.text.toString(), tieneCicloMenstrual, inicioCiclo.text.toString(), finCiclo.text.toString())
                    servidor.postRequestGlucose(postUrlGlucose, horaGlucosa.text.toString(), nivelesGlucosa.text.toString(), tieneCicloMenstrual, inicioCiclo.text.toString(), finCiclo.text.toString())
                    startActivity(Intent(this, Principal::class.java))
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //get the time the insulin is applied
    fun getGlucoseApplicationHour() {

        //variable
        val muestraHoraGlucosa: TextInputEditText = findViewById(R.id.muestraHoraGlucosa)

        val c = Calendar.getInstance()
        // Variables to obtain the hour
        val hora = c[Calendar.HOUR_OF_DAY]
        val minuto = c[Calendar.MINUTE]

        val recogerHora = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) String.valueOf("0$minute") else minute.toString()
            // I get the value a.m. or p.m., depending on user selection

            // I show the time with the desired format
            muestraHoraGlucosa.setText(horaFormateada + ":" + minutoFormateado)
        },  // These values must be in that order
            // When placing in false it is shown in 12 hour format and true in 24 hour format
            // But the system returns the time in 24 hour format
            hora, minuto, true)


        recogerHora.show()
    }

    fun onRadioButtonClicked(view: View) {

        //variables
        val textInicioCiclo: TextView = findViewById(R.id.textInicioCiclo)
        val textFinCiclo: TextView = findViewById(R.id.textFinCiclo)
        val inicioCiclo: TextInputLayout = findViewById(R.id.inicioCiclo)
        val finCiclo: TextInputLayout = findViewById(R.id.finCiclo)

        if (view is RadioButton) {
            //the button is selected
            val checked = view.isChecked

            //which one is selected
            when (view.getId()) {
                R.id.cicloMenstrualSi ->
                    if (checked) {
                        tieneCicloMenstrual = "Si"
                        textInicioCiclo.setVisibility(View.VISIBLE)
                        textFinCiclo.setVisibility(View.VISIBLE)
                        inicioCiclo.setVisibility(View.VISIBLE)
                        finCiclo.setVisibility(View.VISIBLE)
                    }
                R.id.cicloMenstrualNo ->
                    if (checked) {
                        tieneCicloMenstrual = "No"
                        textInicioCiclo.setVisibility(View.INVISIBLE)
                        textFinCiclo.setVisibility(View.INVISIBLE)
                        inicioCiclo.setVisibility(View.INVISIBLE)
                        finCiclo.setVisibility(View.INVISIBLE)
                    }

            }
        }
    }

    //get the start date of menstrual cycle
    fun getMenstrualCycleStartDate() {

        //variable
        val muestraInicioCiclo: TextInputEditText = findViewById(R.id.muestraInicioCiclo)

        // Get Current Date
        val c: Calendar = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        //create the date picker dialog
        val datePickerDialog = DatePickerDialog(
            this,
            OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                muestraInicioCiclo.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year) },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
    }

    //get the end date of menstrual cycle
    fun getMenstrualCycleEndDate() {

        //variable
        val muestraFinCiclo: TextInputEditText = findViewById(R.id.muestraFinCiclo)

        // Get Current Date
        val c: Calendar = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        //create the date picker dialog
        val datePickerDialog = DatePickerDialog(
            this,
            OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                muestraFinCiclo.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year) },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
    }

}