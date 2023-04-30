package com.unileon.diabeticlog.vista

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.unileon.diabeticlog.R

/**
 *
 * This class is taken to the different windows to record the data
 */
class DataRecord : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val vista = inflater.inflate(R.layout.fragment_data_record, container, false)

        //variables
        val registrarDeporte: CardView = vista.findViewById(R.id.cardDeporte)
        val registrarInsulina: CardView = vista.findViewById(R.id.cardInsulina)
        val registrarAlimentacion: CardView = vista.findViewById(R.id.cardAlimentacion)
        val registrarEstado: CardView = vista.findViewById(R.id.cardEstado)
        val registrarGlucosa: CardView = vista.findViewById(R.id.cardGlucosa)
        val registrarMas: CardView = vista.findViewById(R.id.cardMas)

        // Clicking on each of the cards will take you to a window to record that data
        registrarDeporte.setOnClickListener {
            startActivity(Intent(context, SportActivity::class.java))
        }

        registrarInsulina.setOnClickListener {
            startActivity(Intent(context, Insulin::class.java))
        }

        registrarAlimentacion.setOnClickListener {
            startActivity(Intent(context, Feeding::class.java))
        }

        registrarEstado.setOnClickListener {
            startActivity(Intent(context, EmotionalState::class.java))
        }

        registrarGlucosa.setOnClickListener {
            startActivity(Intent(context, Glucose::class.java))
        }

        registrarMas.setOnClickListener {
            recordOtherData()
        }



        return vista
    }

    fun recordOtherData() {

        //variable with the different data
        val array = arrayOf("Ritmo cardíaco", "Actividad del sueño")

        //dialog to choose which data to register
        val dialog: AlertDialog = AlertDialog.Builder(context)
                .setTitle("Registre más datos\n")
                .setItems(array,
                        DialogInterface.OnClickListener { dialog, which ->
                            // The 'which' argument contains the index position
                            // of the selected item
                            if(array[which] == "Ritmo cardíaco") {
                                startActivity(Intent(context, HeartRate::class.java))
                            } else {
                                startActivity(Intent(context, SleepActivity::class.java))
                            }
                        })
                .create()

        dialog.show()
    }

    companion object {
        fun newInstance() = DataRecord()
    }
}