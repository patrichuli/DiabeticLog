package com.unileon.diabeticlog.vista

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.controlador.data.DatosRegistrados
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.deleteUrlGlucose
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlGlucose
import java.util.*

/**
 *
 *  This class allows you to edit the glucose information or delete it directly
 */

class EditDeleteGlucose : AppCompatActivity() {

    //variables
    private var tieneCicloMenstrual = ""
    private var eliminar = DatosRegistrados()
    private val mysql = ConexionSQLHelper(this)
    private val servidor = MetodosServidor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_delete_glucose)

        //shows the button to go back to the home page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        //variables
        val fechaMostrada: TextView = findViewById(R.id.mostrarFecha)
        val horaGlucosa: TextView = findViewById(R.id.muestraHoraGlucosa)
        val muestraInicioCiclo: TextInputEditText = findViewById(R.id.muestraInicioCiclo)
        val muestraFinCiclo: TextInputEditText = findViewById(R.id.muestraFinCiclo)
        val textInicioCiclo: TextView = findViewById(R.id.textInicioCiclo)
        val textFinCiclo: TextView = findViewById(R.id.textFinCiclo)
        val inicioCiclo: TextInputLayout = findViewById(R.id.inicioCiclo)
        val finCiclo: TextInputLayout = findViewById(R.id.finCiclo)
        val nivelesGlucosa: EditText = findViewById(R.id.muestraNivelesGlucosa)
        val cicloMenstrual: RadioGroup = findViewById(R.id.groupCicloMenstrual)


        //variables from the Home Activity
        val id = intent.getIntExtra("id", 1)
        val datos = intent.getStringExtra("datos")
        val fecha = intent.getStringExtra("fecha")
        val hora = intent.getStringExtra("hora")

        eliminar.datos = datos.toString().split(" ").get(0)
        eliminar.fecha = fecha
        eliminar.hora = hora
        eliminar.id = id

        //we take only the int value
        val datosGlucosa = datos.toString().split(" ").get(0)

        //read the data from the database
        val listaDatos = mysql.readGlucose(fecha.toString(), hora.toString(), datosGlucosa)

        if(listaDatos.size != 0){
            fechaMostrada.setText(listaDatos[0])
            horaGlucosa.setText(listaDatos[1])
            nivelesGlucosa.setText(listaDatos[2])

            for (i in 0 until cicloMenstrual.getChildCount()) {
                if( (cicloMenstrual.getChildAt(i) as RadioButton).text == listaDatos[3] ){
                    (cicloMenstrual.getChildAt(i) as RadioButton).isChecked = true
                    tieneCicloMenstrual = listaDatos[3]
                }
            }

            if(tieneCicloMenstrual == "Si") {
                textInicioCiclo.setVisibility(View.VISIBLE)
                textFinCiclo.setVisibility(View.VISIBLE)
                inicioCiclo.setVisibility(View.VISIBLE)
                finCiclo.setVisibility(View.VISIBLE)
            }
            muestraInicioCiclo.setText(listaDatos[4])
            muestraFinCiclo.setText(listaDatos[5])

        }

        // call the function to enter the insulin application time
        val muestraHora: TextInputEditText = findViewById(R.id.muestraHoraGlucosa)
        muestraHora.setOnClickListener { getGlucoseApplicationHour() }

        //clicking lets you choose a date
        muestraInicioCiclo.setOnClickListener { getMenstrualCycleStartDate() }
        muestraFinCiclo.setOnClickListener { getMenstrualCycleEndDate() }
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog to delete item
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setMessage("Este elemento se eliminará\n " +
                    "¿Estás seguro?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Borrar", DialogInterface.OnClickListener { dialog, id ->
                mysql.deleteGlucose(eliminar.fecha.toString(), eliminar.hora.toString(), eliminar.datos.toString())
                servidor.deleteRequestGlucose(deleteUrlGlucose, eliminar.id)
                startActivity(Intent(this, Principal::class.java))
            })
            .setCancelable(false)
            .create()

        //variable to insert
        val horaGlucosa: TextInputEditText = findViewById(R.id.muestraHoraGlucosa)
        val muestraInicioCiclo: TextInputEditText = findViewById(R.id.muestraInicioCiclo)
        val muestraFinCiclo: TextInputEditText = findViewById(R.id.muestraFinCiclo)
        val nivelesGlucosa: EditText = findViewById(R.id.muestraNivelesGlucosa)

        return when (item.itemId) {
            R.id.eliminar -> {
                dialog.show()
                true
            }
            R.id.editar -> {
                if(nivelesGlucosa.equals(0)) {
                    Toast.makeText(this, "Nivel de glucosa requerido", Toast.LENGTH_LONG).show()
                } else {
                    mysql.updateGlucose(eliminar.fecha.toString(), eliminar.hora.toString(), horaGlucosa.text.toString(), eliminar.datos.toString().split(" ").get(0), nivelesGlucosa.text.toString(), tieneCicloMenstrual, muestraInicioCiclo.text.toString(), muestraFinCiclo.text.toString())
                    servidor.putRequestGlucose(postUrlGlucose, eliminar.id, horaGlucosa.text.toString(), nivelesGlucosa.text.toString(), tieneCicloMenstrual, muestraInicioCiclo.text.toString(), muestraFinCiclo.text.toString())
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

        //variables to obtain the hour
        val hora = muestraHoraGlucosa.text.toString().split(":").get(0).toInt()
        val minuto = muestraHoraGlucosa.text.toString().split(":").get(1).toInt()

        val recogerHora = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) java.lang.String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) java.lang.String.valueOf("0$minute") else minute.toString()
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

        //variable
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

    fun getMenstrualCycleStartDate() {

        //variable
        val muestraInicioCiclo: TextInputEditText = findViewById(R.id.muestraInicioCiclo)

        // Get Current Date
        val c: Calendar = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)


        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                muestraInicioCiclo.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
    }

    fun getMenstrualCycleEndDate() {

        //variable
        val muestraFinCiclo: TextInputEditText = findViewById(R.id.muestraFinCiclo)

        // Get Current Date
        val c: Calendar = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)


        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                muestraFinCiclo.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
    }

}