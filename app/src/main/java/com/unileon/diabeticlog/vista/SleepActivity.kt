package com.unileon.diabeticlog.vista

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.Principal
import com.unileon.diabeticlog.modelo.ConexionSQLHelper

/**
 *
 * This class allows you to record sleep activity related data
 */

class SleepActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_activity)

        //shows the button to go back to the registration page
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
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
    // certain tips about sleep activity will be displayed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //dialog para mostrar los consejos
        val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Consejos sobre el sueño")
                .setMessage(Html.fromHtml("<br>La mayoría de los adultos deben dormir entre <b>7 y 9 horas</b> por noche.<br><br>" +
                        "Malos hábitos que suelen conducir a una mala calidad del sueño:<br><br>" +
                        "- Horarios irregulares<br>" +
                        "- Falta de exposición al sol<br>" +
                        "- Falta de ejercicio<br>" +
                        "- Ingesta regular de cafeína<br>" +
                        "- Beber o fumar<br><br>" +
                        "La calidad del sueño también se puede ver afectada por factores fisiológicos, psicológicos y ambientales:<br><br>" +
                        "- Cansancio<br>" +
                        "- Ansiedad<br>" +
                        "- Luz en la habitación<br>" +
                        "- Ruido<br>" +
                        "- Temperatura<br>" +
                        "- Humedad"))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

        //call to the database
        val mysql = ConexionSQLHelper(this)


        return when (item.itemId) {
            R.id.consejos -> {
                dialog.show()
                true
            }
            R.id.guardar -> {

                startActivity(Intent(this, Principal::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}