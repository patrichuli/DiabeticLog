package com.unileon.diabeticlog.vista

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlSport
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * This class allows you to record sport related data
 */

class SportActivity : AppCompatActivity() {

    private val servidor = MetodosServidor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_physical_activity)

        //shows the button to go back to the registration page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        //variables
        val barra: SeekBar = findViewById(R.id.seekBar)
        val mostrarIntensidad: TextView = findViewById(R.id.muestraIntensidad)

        //bar showing intensity
        barra.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int,
                    fromUser: Boolean
            ) {
                mostrarIntensidad.text = "$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        //shows the date of the current day
        val mostrarFecha: TextView = findViewById(R.id.mostrarFecha)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        mostrarFecha.setText(currentDate)

        // call the function to enter the start time of the activity
        val muestraHoraInicio: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        muestraHoraInicio.setOnClickListener { getActivityStartTime() }

        // call the function to enter the end time of the activity
        val muestraHoraFin: TextInputEditText = findViewById(R.id.muestraFin)
        muestraHoraFin.setOnClickListener { getActivityEndTime() }


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
    // certain tips about sport will be displayed
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog to show advices
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Consejos sobre actividad física")
                .setMessage(Html.fromHtml("<br>Se recomiendan dos tipos de actividad física: Ejercicio aeróbico y Ejercicio con pesas.<br><br>" +
                        "<b>Ejercicio aeróbico</b><br><br>" +
                        "Trate de hacer 30 minutos de ejercicio aeróbico de intensidad moderada a alta por lo menos 5 días a la semana.<br><br>" +
                        "Algunos ejemplos de actividades aeróbicas:<br><br>" +
                        "- Caminar rápidamente<br>" +
                        "- Trotar/correr<br>" +
                        "- Nadar<br>" +
                        "- Bailar<br><br>" +
                        "<b>Ejercicio con pesas</b><br><br>" +
                        "Trate de complementar el ejercicio aeróbico con algún tipo de ejercicio con pesas 2-3 días a la semana.<br><br>" +
                        "Algunos ejemplos de actividades con resistencia:<br><br>" +
                        "- Usar máquinas de pesas o pesas en el gimnasio<br>" +
                        "- Usar bandas de resistencia<br>" +
                        "- Calistenia<br>"))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

        //call to the database
        val mysql = ConexionSQLHelper(this)

        //variables to insert
        val tipoActividad: TextInputEditText = findViewById(R.id.muestraActividad)
        val inicioActividad: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        val finActividad: TextInputEditText = findViewById(R.id.muestraFin)
        val intensidad: TextView = findViewById(R.id.muestraIntensidad)
        val calorias: TextInputEditText = findViewById(R.id.muestraCalorias)

        return when (item.itemId) {
            R.id.consejos -> {
                dialog.show()
                true
            }
            R.id.guardar -> {
                if(inicioActividad.text.toString() == "" || finActividad.text.toString() == "") {
                    Toast.makeText(this, "Tiempo requerido", Toast.LENGTH_SHORT).show()
                } else if(calorias.text.toString() == "") {
                    Toast.makeText(this, "Calorías requeridas", Toast.LENGTH_SHORT).show()
                } else if(tipoActividad.text.toString() == "") {
                    Toast.makeText(this, "Tipo de actividad requerido", Toast.LENGTH_SHORT).show()
                } else {
                    mysql.insertSport(tipoActividad.text.toString(), inicioActividad.text.toString(), finActividad.text.toString(), intensidad.text.toString(), calorias.text.toString())
                    servidor.postRequestSport(postUrlSport, tipoActividad.text.toString(), inicioActividad.text.toString(), finActividad.text.toString(), intensidad.text.toString(), calorias.text.toString())
                    startActivity(Intent(this, Principal::class.java))

                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //get the activity start time
    fun getActivityStartTime() {

        //variable
        val muestraHoraInicio: TextInputEditText = findViewById(R.id.muestraHoraIngesta)

        val c = Calendar.getInstance()
        // Variables to obtain the hour
        val hora = c[Calendar.HOUR_OF_DAY]
        val minuto = c[Calendar.MINUTE]

        val recogerHora = TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute -> // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) java.lang.String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) java.lang.String.valueOf("0$minute") else minute.toString()
            // I get the value a.m. or p.m., depending on user selection
            /* val AM_PM: String
             AM_PM = if (hourOfDay < 12) {
                 "a.m."
             } else {
                 "p.m."
             }*/

            // I show the time with the desired format
            muestraHoraInicio.setText(horaFormateada + ":" + minutoFormateado)
        }, // These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

    //get the activity end time
    fun getActivityEndTime() {

        val muestraHoraFin: TextInputEditText = findViewById(R.id.muestraFin)

        val c = Calendar.getInstance()
        //Variables to obtain the hour
        val hora = c[Calendar.HOUR_OF_DAY]
        val minuto = c[Calendar.MINUTE]

        val recogerHora = TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute -> // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) java.lang.String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) java.lang.String.valueOf("0$minute") else minute.toString()
            // I get the value a.m. or p.m., depending on user selection
            /*  val AM_PM: String
              AM_PM = if (hourOfDay < 12) {
                  "a.m."
              } else {
                  "p.m."
              }*/

            // I show the time with the desired format
            muestraHoraFin.setText(horaFormateada + ":" + minutoFormateado)
        }, // These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

}