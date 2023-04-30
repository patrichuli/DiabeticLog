package com.unileon.diabeticlog.vista

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.controlador.data.DatosRegistrados
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.deleteUrlSport
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlSport
import java.lang.String
import java.util.*

/**
 *
 * This class allows you to edit the sport activity information or delete it directly
 */
class EditDeleteSport : AppCompatActivity() {

    private val servidor = MetodosServidor()
    private val mysql = ConexionSQLHelper(this)
    private var eliminar = DatosRegistrados()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_delete_sport)

        //shows the button to go back to the home page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        //variables
        val barra: SeekBar = findViewById(R.id.seekBar)
        val fechaMostrada: TextView = findViewById(R.id.mostrarFecha)
        val tipoActividad: TextInputEditText = findViewById(R.id.muestraActividad)
        val inicioActividad: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        val finActividad: TextInputEditText = findViewById(R.id.muestraFin)
        val intensidad: TextView = findViewById(R.id.muestraIntensidad)
        val calorias: TextInputEditText = findViewById(R.id.muestraCalorias)

        //variables from the Home Activity
        val id = intent.getIntExtra("id", 1)
        val nombre = intent.getStringExtra("nombre")
        val fecha = intent.getStringExtra("fecha")
        val hora = intent.getStringExtra("hora")

        eliminar.nombre = nombre
        eliminar.fecha = fecha
        eliminar.hora = hora
        eliminar.id = id

        //read the data from the database
        val listaDatos = mysql.readSport(fecha.toString(), hora.toString(), nombre.toString())

        if(listaDatos.size != 0){
            fechaMostrada.setText(listaDatos[0])
            tipoActividad.setText(listaDatos[1])
            inicioActividad.setText(listaDatos[2])
            finActividad.setText(listaDatos[3])
            intensidad.setText(listaDatos[4])
            calorias.setText(listaDatos[5])

        }

        //bar showing intensity
        barra.setProgress(listaDatos[4].toInt())
        barra.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int,
                    fromUser: Boolean
            ) {
                // intensidad.text = "$progress"
                // setProgress(listaDatos[5].toInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        //clicking lets you choose a date
        inicioActividad.setOnClickListener { getActivityStartTime() }
        finActividad.setOnClickListener { getActivityEndTime() }


    }

    //arrow to go back
    override fun onSupportNavigateUp(): Boolean {

        onBackPressed()
        return true
    }

    //create the menu with the edit and delete options
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_editar_eliminar, menu)
        return true
    }

    //if you press the delete button, the data will be deleted and if you click on edit it will be updated
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog to delete item
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setMessage("Este elemento se eliminará\n " +
                        "¿Estás seguro?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", DialogInterface.OnClickListener { dialog, which ->
                    mysql.deleteSport(eliminar.fecha.toString(), eliminar.hora.toString(), eliminar.nombre.toString())
                    servidor.deleteRequestSport(deleteUrlSport, eliminar.id)
                    startActivity(Intent(this, Principal::class.java))
                })
                .setCancelable(false)
                .create()

        //variable to insert
        val tipoActividad: TextInputEditText = findViewById(R.id.muestraActividad)
        val inicioActividad: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        val finActividad: TextInputEditText = findViewById(R.id.muestraFin)
        val intensidad: TextView = findViewById(R.id.muestraIntensidad)
        val calorias: TextInputEditText = findViewById(R.id.muestraCalorias)


        return when (item.itemId) {
            R.id.eliminar -> {
                dialog.show()
                true

            }
            R.id.editar -> {
                if(inicioActividad.text.toString() == "" || finActividad.text.toString() == "") {
                    Toast.makeText(this, "Tiempo requerido", Toast.LENGTH_LONG).show()
                } else {
                    mysql.updateSport(eliminar.fecha.toString(), eliminar.hora.toString(), eliminar.nombre.toString(), tipoActividad.text.toString(), inicioActividad.text.toString(), finActividad.text.toString(), intensidad.text.toString(), calorias.text.toString())
                    servidor.putRequestSport(postUrlSport, eliminar.id, tipoActividad.text.toString(), inicioActividad.text.toString(), finActividad.text.toString(), intensidad.text.toString(), calorias.text.toString() )
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
        val horaInicio: TextInputEditText = findViewById(R.id.muestraHoraIngesta)

        val hora = horaInicio.text.toString().split(":").get(0).toInt()
        val minuto = horaInicio.text.toString().split(":").get(1).toInt()

        val recogerHora = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute -> // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) String.valueOf("0$minute") else minute.toString()
            // I get the value a.m. or p.m., depending on user selection
            /* val AM_PM: String
             AM_PM = if (hourOfDay < 12) {
                 "a.m."
             } else {
                 "p.m."
             }*/

            // I show the time with the desired format
            horaInicio.setText(horaFormateada + ":" + minutoFormateado)
        }, // These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

    //get the activity end time
    fun getActivityEndTime() {

        //variable
        val horaFin: TextInputEditText = findViewById(R.id.muestraFin)

        val c = Calendar.getInstance()
        // Variables to obtain the hour
        val hora = c[Calendar.HOUR_OF_DAY]
        val minuto = c[Calendar.MINUTE]

        val recogerHora = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute -> // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) String.valueOf("0$minute") else minute.toString()
            // I get the value a.m. or p.m., depending on user selection
            /*  val AM_PM: String
              AM_PM = if (hourOfDay < 12) {
                  "a.m."
              } else {
                  "p.m."
              }*/

            // I show the time with the desired format
            horaFin.setText(horaFormateada + ":" + minutoFormateado)
        },  // These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

}