package com.unileon.diabeticlog.vista

import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlFeeding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * This class allows you to record feeding related data
 */

class Feeding : AppCompatActivity() {

    private val servidor = MetodosServidor()
    //variables for the image
    private val REQUEST_IMAGE_CAPTURE = 1
    private var imagenComprimida = byteArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeding)

        //shows the button to go back to the registration page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)


        //shows the date of the current day
        val mostrarFecha: TextView = findViewById(R.id.mostrarFecha)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        mostrarFecha.setText(currentDate)

        // call the function to enter the time of the ingestion
        val muestraInicio: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        muestraInicio.setOnClickListener { getFoodIntakeTime() }

        // call the function to open a pdf with the carbohydrate rations
        val consultarPDF: TextView = findViewById(R.id.consultarTabla)
        consultarPDF.setOnClickListener { getCarbohydrateTable() }

        // call the function to take photos of the food
        val camara: ImageButton = findViewById(R.id.anadirFotos)
        camara.setOnClickListener { takeFoodPicture() }

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
    // certain tips about feeding will be displayed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog to show advices
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Consejos sobre alimentación")
                .setMessage(Html.fromHtml("<br>Es necesario mantener una dieta equilibrada y completa.<br><br>" +
                        "<b>Las grasas:</b> Limita el consumo de grasas poco saludables (saturadas y trans).<br><br>" +
                        "<b>Hidratos de Carbono:</b> deben restringirse los azúcares simples y los que se ingieran deben ser complejos. " +
                        "Se pueden emplear edulcorantes, se debe aumentar el consumo de fibra, cereales y legumbres. " +
                        "La fibra enlentece la absorción de los hidratos de carbono. " +
                        "Constituyen entre el 60-70% de las calorías totales de la dieta.<br><br>" +
                        "<b>La sal:</b> Reducir la cantidad de sodio en la dieta puede ayudar a mucha gente a bajar la presión arterial.<br><br>" +
                        "<b>Alcohol:</b> Realiza un consumo moderado. La ingesta de alcohol no acompañada de ingestión de otros alimentos en las personas con diabetes tratadas con insulina (o con hipoglucemiantes orales) puede producir hipoglucemia"))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

        //call to the database
        val mysql = ConexionSQLHelper(this)

        //variables to insert
        val horaIngesta: TextInputEditText = findViewById(R.id.muestraHoraIngesta)
        val raciones: EditText = findViewById(R.id.muestraRaciones)
        val alimento: EditText = findViewById(R.id.textoAlimento)

        return when (item.itemId) {
            R.id.consejos -> {
                dialog.show()
                true
            }
            R.id.guardar -> {
                if(raciones.text.toString() == ""){
                    Toast.makeText(this, "Raciones requeridas", Toast.LENGTH_SHORT).show()
                } else if(horaIngesta.text.toString() == "") {
                    Toast.makeText(this, "Hora requerida", Toast.LENGTH_SHORT).show()
                } else if(alimento.text.toString() == "") {
                    Toast.makeText(this, "Alimento requerido", Toast.LENGTH_SHORT).show()
                } else {
                    mysql.insertFeeding(horaIngesta.text.toString(), alimento.text.toString(), imagenComprimida, raciones.text.toString())
                    servidor.postRequestFeeding(postUrlFeeding, horaIngesta.text.toString(), alimento.text.toString(), imagenComprimida, raciones.text.toString())
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
        val muestraHoraIngesta: TextInputEditText = findViewById(R.id.muestraHoraIngesta)

        val c = Calendar.getInstance()
        // Variables to obtain the hour
        val hora = c[Calendar.HOUR_OF_DAY]
        val minuto = c[Calendar.MINUTE]

        val recogerHora = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute -> // Format the hour obtained: prepend 0 if they are less than 10
                    val horaFormateada =
                            if (hourOfDay < 10) java.lang.String.valueOf("0$hourOfDay") else hourOfDay.toString()
                    // Format the minute obtained: prepend 0 if they are less than 10
                    val minutoFormateado =
                            if (minute < 10) java.lang.String.valueOf("0$minute") else minute.toString()
                    // I get the value a.m. or p.m., depending on user selection

                    // I show the time with the desired format
                    muestraHoraIngesta.setText(horaFormateada + ":" + minutoFormateado)
                },// These values must be in that order
                // When placing in false it is shown in 12 hour format and true in 24 hour format
                // But the system returns the time in 24 hour format
                hora, minuto, true)


        recogerHora.show()
    }

    // open a pdf to see the table of carbohydrate rations
    fun getCarbohydrateTable() {

        // show the pdf from a webView
        /*val pdfView = WebView(this)
        setContentView(webView)
        webVw.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://docs.google.com/viewer?url=" + "https://www.fundaciondiabetes.org/upload/publicaciones_ficheros/71/TABLAHC.pdf")
        */

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
            imagenComprimida = stream.toByteArray()
        }
    }



}