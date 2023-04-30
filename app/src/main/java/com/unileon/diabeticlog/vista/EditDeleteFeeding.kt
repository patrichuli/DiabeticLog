package com.unileon.diabeticlog.vista

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.controlador.data.DatosRegistrados
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.deleteUrlFeeding
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlFeeding
import java.io.ByteArrayOutputStream
import java.lang.String
import java.util.*

/**
 *
 * This class allows you to edit the feeding information or delete it directly
 */
class EditDeleteFeeding : AppCompatActivity() {

    //call to the database
    private val mysql = ConexionSQLHelper(this)
    private val servidor = MetodosServidor()
    //variables for the image
    private val REQUEST_IMAGE_CAPTURE = 1
    private var encode = byteArrayOf()
    //variable
    private var eliminar = DatosRegistrados()



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_delete_feeding)

        //shows the button to go back to the home page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        //variables
        val fechaMostrada: TextView = findViewById(R.id.mostrarFecha)
        val horaIngesta: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        val raciones: EditText = findViewById(R.id.muestraRaciones)
        val alimento: EditText = findViewById(R.id.textoAlimento)
        val consultarPDF: TextView = findViewById(R.id.consultarTabla)
        val camara: ImageButton = findViewById(R.id.anadirFotos)
        val foto: ImageView = findViewById(R.id.fotoComida)

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
        val listaDatos = mysql.readFeeding(fecha.toString(), hora.toString(), nombre.toString())

        if(listaDatos.size != 0){
            fechaMostrada.setText(listaDatos[0])
            horaIngesta.setText(listaDatos[1])
            alimento.setText(listaDatos[2])
            raciones.setText(listaDatos[4])

            val encodeByte: ByteArray = Base64.getDecoder().decode(listaDatos[3])
            encode = encodeByte
            val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            foto.setImageBitmap(bitmap)

        }

        // call the function to enter the time of the ingestion
        horaIngesta.setOnClickListener { getFoodIntakeTime() }

        // call the function to open a pdf with the carbohydrate rations
        consultarPDF.setOnClickListener { getCarbohydrateTable() }

        // call the function to take photos of the food
        camara.setOnClickListener { takeFoodPicture() }


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
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Borrar", DialogInterface.OnClickListener { dialog, which ->
                    mysql.deleteFeeding(eliminar.fecha.toString(), eliminar.hora.toString(), eliminar.nombre.toString())
                    servidor.deleteRequestFeeding(deleteUrlFeeding, eliminar.id)
                    startActivity(Intent(this, Principal::class.java))
                })
                .setCancelable(false)
                .create()

        //variables to insert
        val horaIngesta: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        val raciones: EditText = findViewById(R.id.muestraRaciones)
        val alimento: EditText = findViewById(R.id.textoAlimento)


        return when (item.itemId) {
            R.id.eliminar -> {
                dialog.show()
                true
            }
            R.id.editar -> {
                if(raciones.equals(0)){
                    Toast.makeText(this, "Raciones requeridas", Toast.LENGTH_LONG).show()
                } else {
                    mysql.updateFeeding(eliminar.fecha.toString(), eliminar.hora.toString(), horaIngesta.text.toString(), eliminar.nombre.toString(), alimento.text.toString(), encode, raciones.text.toString())
                    servidor.putRequestFeeding(postUrlFeeding, eliminar.id, horaIngesta.text.toString(), alimento.text.toString(), encode, raciones.text.toString())
                    startActivity(Intent(this, Principal::class.java))
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //get the time at which the food intake was made
    fun getFoodIntakeTime() {

        //variable
        val muestraHora: TextInputEditText = findViewById(R.id.muestraHoraIngesta)

        //variables to obtain the hour
        val hora = muestraHora.text.toString().split(":").get(0).toInt()
        val minuto = muestraHora.text.toString().split(":").get(1).toInt()

        val recogerHora = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute -> // Format the hour obtained: prepend 0 if they are less than 10
                    val horaFormateada =
                            if (hourOfDay < 10) String.valueOf("0$hourOfDay") else hourOfDay.toString()
                    // Format the minute obtained: prepend 0 if they are less than 10
                    val minutoFormateado =
                            if (minute < 10) String.valueOf("0$minute") else minute.toString()
                    // I get the value a.m. or p.m., depending on user selection

                    // I show the time with the desired format
                    muestraHora.setText(horaFormateada + ":" + minutoFormateado)
                }, // These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

    // open a pdf to see the table of carbohydrate rations
    fun getCarbohydrateTable() {

        //show the pdf and open it with the Google Drive viewer
        val intent = Intent()
        intent.setDataAndType(Uri.parse("https://www.fundaciondiabetes.org/upload/publicaciones_ficheros/71/TABLAHC.pdf"), "application/pdf")
        startActivity(intent)
    }

    //allows you to take photos of the food
    fun takeFoodPicture() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val foto: ImageView = findViewById(R.id.fotoComida)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            foto.setImageBitmap(imageBitmap)

            val stream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            encode = stream.toByteArray()
        }
    }

}