package com.unileon.diabeticlog.vista

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
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.controlador.data.DatosRegistrados
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.deleteUrlInsulin
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlInsulin

/**
 *
 * This class allows you to edit the insulin information or delete it directly
 */

class EditDeleteInsulin : AppCompatActivity() {

    //variables
    private var tipoInsulina: String = ""
    private var tipoAplicacion: String = ""
    private var eliminar = DatosRegistrados()

    //call to the database
    private val mysql = ConexionSQLHelper(this)
    private val servidor = MetodosServidor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_delete_insulin)

        //shows the button to go back to the home page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        //variables
        val fechaMostrada: TextView = findViewById(R.id.mostrarFecha)
        val horaAplicacion: TextView = findViewById(R.id.muestraHoraAplicacion)
        val unidades: EditText = findViewById(R.id.muestraUnidades)
        val insulina: RadioGroup = findViewById(R.id.radioGroupInsulina)
        val aplicacion: RadioGroup = findViewById(R.id.radioGroupAplicacion)

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
        val listaDatos = mysql.readInsulin(fecha.toString(), hora.toString(), nombre.toString())

        if(listaDatos.size != 0){
            fechaMostrada.setText(listaDatos[0])
            horaAplicacion.setText(listaDatos[1])

            for (i in 0 until insulina.getChildCount()) {
                if( (insulina.getChildAt(i) as RadioButton).text == listaDatos[2] ){
                    (insulina.getChildAt(i) as RadioButton).isChecked = true
                    tipoInsulina = listaDatos[2]
                }
            }

            unidades.setText(listaDatos[3])

            for (i in 0 until aplicacion.getChildCount()) {
                if( (aplicacion.getChildAt(i) as RadioButton).text == listaDatos[4] ){
                    (aplicacion.getChildAt(i) as RadioButton).isChecked = true
                    tipoAplicacion = listaDatos[4]
                }
            }


        }

        // call the function to enter the insulin application time
        horaAplicacion.setOnClickListener { getInsulinApplicationHour() }
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
        val d: AlertDialog = AlertDialog.Builder(this)
                .setMessage("Este elemento se eliminará\n " +
                        "¿Estás seguro?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar", DialogInterface.OnClickListener { dialog, id ->
                    mysql.deleteInsulin(eliminar.fecha.toString(), eliminar.hora.toString(), eliminar.nombre.toString())
                    servidor.deleteRequestInsulin(deleteUrlInsulin, eliminar.id)
                    startActivity(Intent(this, Principal::class.java))
                })
                .setCancelable(false)
                .create()

        //variables to insert
        val unidades: EditText = findViewById(R.id.muestraUnidades)
        val horaAplicacion: TextView = findViewById(R.id.muestraHoraAplicacion)

        return when (item.itemId) {
            R.id.eliminar -> {
                d.show()
                true
            }
            R.id.editar -> {
                if(unidades.equals(0)) {
                    Toast.makeText(this, "Unidades requeridas", Toast.LENGTH_LONG).show()
                } else {
                    mysql.updateInsulin(eliminar.fecha.toString(), eliminar.hora.toString(), horaAplicacion.text.toString(), eliminar.nombre.toString(), tipoInsulina, unidades.text.toString(), tipoAplicacion)
                    servidor.putRequestInsulin(postUrlInsulin, eliminar.id, horaAplicacion.text.toString(), tipoInsulina, unidades.text.toString(), tipoAplicacion)
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
        val muestraHora: TextInputEditText = findViewById(R.id.muestraHoraAplicacion)

        //variables to obtain the hour
        val hora = muestraHora.text.toString().split(":").get(0).toInt()
        val minuto = muestraHora.text.toString().split(":").get(1).toInt()

        val recogerHora = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            // Format the hour obtained: prepend 0 if they are less than 10
            val horaFormateada = if (hourOfDay < 10) java.lang.String.valueOf("0$hourOfDay") else hourOfDay.toString()
            // Format the minute obtained: prepend 0 if they are less than 10
            val minutoFormateado = if (minute < 10) java.lang.String.valueOf("0$minute") else minute.toString()
            // I get the value a.m. or p.m., depending on user selection

            // I show the time with the desired format
            muestraHora.setText(horaFormateada + ":" + minutoFormateado)
        }, // These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

    fun onRadioButtonClicked(view: View) {

        if (view is RadioButton) {
            //button is selected
            val checked = view.isChecked

            //which one is selected
            when (view.getId()) {
                R.id.insulinaLenta ->
                    if (checked) {
                        tipoInsulina = "Insulina lenta o basal"
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