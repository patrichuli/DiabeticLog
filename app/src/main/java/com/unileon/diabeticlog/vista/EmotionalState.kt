package com.unileon.diabeticlog.vista

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlEmotionalState

/**
 *
 * This class allows you to record emotional state related data
 */

class EmotionalState : AppCompatActivity() {

    val servidor = MetodosServidor()

    //variables to insert
    private lateinit var mostrarEmocion: TextView
    private lateinit var motivo: EditText
    private var intensidad: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotional_state)

        //shows the button to go back to the registration page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        mostrarEmocion = findViewById(R.id.muestraEmocion)
        motivo = findViewById(R.id.muestraMotivo)

        // button that shows the information
        val muestraInfo: ImageButton = findViewById(R.id.informacion)
        muestraInfo.setOnClickListener{ showInformation() }

        // button to add the emotional state
        val botonEmocion: ImageButton = findViewById(R.id.anadirEmocion)
        botonEmocion.setOnClickListener { chooseEmotionalState() }


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
    // certain tips about emotional state will be displayed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog to show advices
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Consejos sobre el estado emocional")
                .setMessage(
                        Html.fromHtml("<br>LLevar una vida <b>emocionalmente sana</b> ayudará a controlar mucho mejor su diabetes.<br><br>" +
                                "Algunos consejos para afrontar la cara emocional de la diabetes:<br><br>" +
                                "- Ábrete a la gente en quien confías<br>" +
                                "- Pide más apoyo si lo necesitas<br>" +
                                "- Aprende a cuidar de ti mismo<br>" +
                                "- Organízate bien<br>" +
                                "- Céntrate en tus puntos fuertes<br>" +
                                "- Sigue tu plan de control de diabetes<br>" +
                                "- Tómate el tiempo que necesites<br><br>" +
                                "Es importante recordar que esto no es un diagnóstico. " +
                                "Solo un <b>profesional de la salud</b> puede determinar y darte un plan de tratamiento, así que no dudes en contactar a uno si lo necesitas.<br>"))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

        //call to the database and read data
        val mysql = ConexionSQLHelper(this)
        val datos = mysql.readEmotion()


        return when (item.itemId) {
            R.id.consejos -> {
                dialog.show()
                true
            }
            R.id.guardar -> {
                if(mostrarEmocion.text == "") {
                    Toast.makeText(this, "Emoción requerida", Toast.LENGTH_SHORT).show()
                } else {
                    // we add the registered data to the database
                    if(datos.size == 0){
                        mysql.insertEmotion(mostrarEmocion.text.toString(), intensidad, motivo.text.toString())
                        servidor.postRequestEmotion(postUrlEmotionalState, mostrarEmocion.text.toString(), intensidad, motivo.text.toString())

                    } else {
                        mysql.updateEmotion(mostrarEmocion.text.toString(), intensidad, motivo.text.toString())
                        servidor.putRequestEmotion(postUrlEmotionalState, mostrarEmocion.text.toString(), intensidad, motivo.text.toString())


                    }

                    startActivity(Intent(this, Principal::class.java))

                }
                true

            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    fun showInformation() {

        //variable
        val dialog = AlertDialog.Builder(this)

        //create and show the dialog
        with(dialog) {
            setMessage("En el siguiente espacio podrá expresar de una manera mejor y más extensa cómo se siente y/o el motivo" +
                    " de por qué se siente así.")
            setPositiveButton(android.R.string.ok, null)
            show()
        }

    }


    fun onRadioButtonClicked(view: View) {


        if (view is RadioButton) {
            // button is selected
            val checked = view.isChecked

            //which one is selected
            when (view.getId()) {
                R.id.radioButton_demasiado ->
                    if (checked) {
                        intensidad = "Demasiado"
                    }
                R.id.radioButton_muy ->
                    if (checked) {
                        intensidad = "Muy"
                    }
                R.id.radioButton_algo ->
                    if (checked) {
                        intensidad = "Algo"
                    }
                R.id.radioButton_poco ->
                    if (checked) {
                        intensidad = "Poco"
                    }
                R.id.radioButton_casinada ->
                    if (checked) {
                        intensidad = "Casi nada"
                    }
            }
        }

    }

    fun chooseEmotionalState() {

        //variable
        val dialog = AlertDialog.Builder(this)

        //create and show the dialog
        with(dialog) {
            setTitle("Selecciona la emoción que sientes")
            setCancelable(false)
            setSingleChoiceItems(itemsEmotion, -1, DialogInterface.OnClickListener { dialog, which ->
                mostrarEmocion.setText(itemsEmotion[which])
                dialog.dismiss()
            })
            setNegativeButton("Cancelar",  null)
            show()
        }

    }



    companion object{
        //list with all emotional states
        val itemsEmotion: Array<String> = arrayOf("Entusiasmado", "Optimista", "Orgulloso", "Feliz", "Bien", "Satisfecho", "Amoroso", "Agradecido", "Tranquilo", "Relajado", "Confundido", "Incómodo", "Indeciso", "Adormecido", "Cansado", "Inseguro", "Indiferente", "Aburrido", "Apático", "Triste", "Irritable", "Dolido", "Frustrado", "Avergonzado", "Culpable", "Enfadado", "Furioso", "Estresado", "Asustado", "Deprimido", "Ansioso", "Abrumado")

    }


}