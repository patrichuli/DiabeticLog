package com.unileon.diabeticlog.controlador

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unileon.diabeticlog.R

class Access : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access)

        /*  val pin: EditText = findViewById(R.id.password2)
          val preferences: SharedPreferences = getSharedPreferences("DiabeticLog_preferences_key", Context.MODE_PRIVATE)
          pin.setText(preferences.getString("pin", ""))*/

        //variables
        val correcto: Button = findViewById(R.id.buttonOK)
        correcto.setOnClickListener { clicOK() }

        val cancelar: Button = findViewById(R.id.buttonCancelar)
        cancelar.setOnClickListener { cancelAccess() }

    }

    //If you press the cancel button, it deletes what is in the password
    fun cancelAccess() {

        val pin: EditText = findViewById(R.id.password2)
        pin.text.clear()

    }

    //If you press the OK button, check if you have written the pin correctly,
    // then it is saved and enters the application
    fun clicOK(){


        //variables
        val pin: EditText = findViewById(R.id.password2)
        val pref: SharedPreferences = getSharedPreferences("Diabeticlog_preferences_key", Context.MODE_PRIVATE)
        val myP = pref.getString("pin", "")

        //if they are the same, I save the pin and enter the app
        if(pin.length() >= 5 && (pin.text.toString() == myP)){

            //once entered and correct, we enter the app
            startActivity(Intent(this, Principal::class.java))

        } else if(pin.length() >= 5 && myP == "") {
            //save it
            val editor = pref.edit()
            editor.putString("pin", pin.text.toString())
            editor.commit()
            startActivity(Intent(this, Principal::class.java))

        } else {
            //they are not the same, I show a message that the pin is wrong
            val toast = Toast.makeText(this, "PIN incorrecto!!", Toast.LENGTH_SHORT)
            toast.setGravity(0, 0, 450)
            toast.show()

        }
    }
}