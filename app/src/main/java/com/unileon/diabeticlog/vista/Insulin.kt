package com.unileon.diabeticlog.vista

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
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlInsulin
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * This class allows you to record insulin related data
 */

class Insulin : AppCompatActivity() {

    private val servidor = MetodosServidor()
    private var tipoInsulina: String = ""
    private var tipoAplicacion: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insulin)

        //shows the button to go back to the registration page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        // call the function to enter the insulin application time
        val muestraHora: TextInputEditText = findViewById(R.id.muestraHoraAplicacion)
        muestraHora.setOnClickListener { getInsulinApplicationHour() }

        //shows the date of the current day
        val mostrarFecha: TextView = findViewById(R.id.mostrarFecha)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        mostrarFecha.setText(currentDate)
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
    // certain tips about insulin will be displayed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog to show advices
        val d: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Consejos sobre insulina")
                .setMessage(
                        Html.fromHtml("<br>Preste especial atención a los siguientes síntomas:<br><br>" +
                                "- <b>Hipoglucemia</b> (niveles bajos de azúcar): Hambre, sudoración excesiva, sueño, temblor, nausea y confusión.<br>" +
                                "- <b>Hiperglucemia</b> (niveles altos de azúcar): Sensación de sed, aumento de la cantidad de orina, aumento de apetito.<br><br>" +
                                "Si presenta estos síntomas acérquese al centro de salud."))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

        //call to the database
        val mysql = ConexionSQLHelper(this)

        //variables to insert
        val horaAplicacion: TextInputEditText = findViewById(R.id.muestraHoraAplicacion)
        val unidades: EditText = findViewById(R.id.muestraUnidades)



        return when (item.itemId) {
            R.id.consejos -> {
                d.show()
                true
            }
            R.id.guardar -> {
                if(unidades.text.toString() == "") {
                    Toast.makeText(this, "Unidades requeridas", Toast.LENGTH_SHORT).show()
                } else if(horaAplicacion.text.toString() == "") {
                    Toast.makeText(this, "Hora de aplicación requerida", Toast.LENGTH_SHORT).show()
                } else if(tipoInsulina == "") {
                    Toast.makeText(this, "Tipo de insulina requerida", Toast.LENGTH_SHORT).show()
                } else {
                    mysql.insertInsulin(horaAplicacion.text.toString(), tipoInsulina, unidades.text.toString(), tipoAplicacion)
                    servidor.postRequestInsulin(postUrlInsulin, horaAplicacion.text.toString(), tipoInsulina, unidades.text.toString(), tipoAplicacion)
                    startActivity(Intent(this, Principal::class.java))
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //get the time the insulin is applied
    fun getInsulinApplicationHour() {

        //variable
        val muestraHoraAplicacion: TextInputEditText = findViewById(R.id.muestraHoraAplicacion)

        val c = Calendar.getInstance()
        // Variables to obtain the hour
        val hora = c[Calendar.HOUR_OF_DAY]
        val minuto = c[Calendar.MINUTE]

        val recogerHora = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) java.lang.String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) java.lang.String.valueOf("0$minute") else minute.toString()
            // I get the value a.m. or p.m., depending on user selection

            // I show the time with the desired format
            muestraHoraAplicacion.setText(horaFormateada + ":" + minutoFormateado)
        },  // These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

    fun onRadioButtonClicked(view: View) {

        if (view is RadioButton) {
            // button is selected
            val checked = view.isChecked

            //which one is selected
            when (view.getId()) {
                R.id.insulinaLenta ->
                    if (checked) {
                        tipoInsulina  = "Insulina lenta o basal"
                    }
                R.id.insulinaRapida ->
                    if (checked) {
                        tipoInsulina = "Insulina rápida"
                    }
                R.id.insulinaIntermedia ->
                    if (checked) {
                        tipoInsulina = "Insulina de acción intermedia"
                    }
                R.id.aplicacionNormal ->
                    if (checked) {
                        tipoAplicacion = "Aplicación normal"
                    }
                R.id.aplicacionExtra ->
                    if (checked) {
                        tipoAplicacion = "Aplicación extra"
                    }
            }
        }
    }
}