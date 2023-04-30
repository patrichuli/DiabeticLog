package com.unileon.diabeticlog.vista

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.googleFit.GoogleFitness
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.ConexionSQLHelper.Companion.fechaRegistro
import com.unileon.diabeticlog.modelo.ConexionSQLHelper.Companion.weatherDate
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlMeasures
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlWeather
import com.unileon.diabeticlog.vista.EmotionalState.Companion.itemsEmotion


/**
 * A simple [Fragment] subclass.
 * Use the [UserProfile.newInstance] factory method to
 * create an instance of this fragment.
 *
 * This class allows the user to record additional data and
 * view this data or recorded data in the record window
 */
class UserProfile : Fragment() {

    val servidor = MetodosServidor()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val vista = inflater.inflate(R.layout.fragment_user_profile, container, false)

        //to show the menu
        setHasOptionsMenu(true)

        //variables
        val anadirAltura: ImageButton = vista.findViewById(R.id.anadirAltura)
        val anadirPeso: ImageButton = vista.findViewById(R.id.anadirPeso)
        val anadirTiempo: FloatingActionButton = vista.findViewById(R.id.fabTiempo)
        val verEstadoEmocional: ImageButton = vista.findViewById(R.id.botonVerEmocion)
        val mostrarAltura: TextView = vista.findViewById(R.id.muestraAltura)
        val mostrarPeso : TextView = vista.findViewById(R.id.muestraPeso)
        val mostrarPasos: TextView = vista.findViewById(R.id.muestraPasosDiarios)

        //call to the database and read the data
        val mysql: ConexionSQLHelper = ConexionSQLHelper(context)
        val datos = mysql.readMeasures()

        if(datos.size != 0){
            mostrarAltura.setText(datos[0])
            mostrarPeso.setText(datos[1])
        }
        //read the weather data from the database
        val t = mysql.readWeather()

        if(t.length() != 0){
            if(weatherDate == fechaRegistro) {
                for (i in 0 until itemsWeather.size) { // Iterate elements of the first ArrayList
                    for (j in 0 until t.length()) { // Iterate elements of the second ArrayList
                        if (itemsWeather[i].equals(t[j])) { // Compare if the values are equal.
                            marcados[i] = true
                        }
                    }
                }
            }

        }

        //read the steps data from the database
        val steps = mysql.readSteps()

        if(steps.size != 0) {
            if(steps[0] == fechaRegistro) {
                mostrarPasos.setText(steps[1])
            } else {
                mostrarPasos.setText("0")
            }
        }

        // with the buttons the different functions are called
        anadirAltura.setOnClickListener {
            recordHeight(vista)
        }

        anadirPeso.setOnClickListener{
            recordWeight(vista)
        }

        anadirTiempo.setOnClickListener{
            recordWeather(vista)
        }

        verEstadoEmocional.setOnClickListener{
            showEmotionalState(vista)
        }

        // Check if user is signed in (non-null) and update UI accordingly.
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if(account?.email != null) {
            onlyIfHasPermissions()
        }



        return vista

    }

    // create a menu with the option to change the PIN
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_perfil, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    // this function allows the user to change their PIN to a new one
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //variables
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Diabeticlog_preferences_key", Context.MODE_PRIVATE)
        val editor = preferences.edit()

        val view = LayoutInflater.from(context).inflate(R.layout.change_pin, null, false)

        val nuevoPin : EditText = view.findViewById(R.id.nuevo)
        val nuevoPinRepetido : EditText = view.findViewById(R.id.nuevox2)


        //create and show the dialog
        val dialog: AlertDialog = AlertDialog.Builder(context)
            .setView(view)
            .setTitle("Cambiar PIN")
            .setMessage("Introduzca el nuevo PIN. \nMínimo 5 números.")
            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener {
            val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if(nuevoPin.length() >= 5 && nuevoPinRepetido.length() >= 5 && nuevoPin.text.toString() == nuevoPinRepetido.text.toString()){
                        editor.putString("pin", nuevoPin.text.toString())
                        editor.commit()
                        dialog.dismiss()
                    } else if(nuevoPin.text.toString() != nuevoPinRepetido.text.toString()) {
                        val t = Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT)
                        t.setGravity(0, 0, 450)
                        t.show()
                    } else if(nuevoPin.length() < 5 || nuevoPinRepetido.length() < 5 ){
                        val toast = Toast.makeText(context, "Mínimo 5 números", Toast.LENGTH_SHORT)
                        toast.setGravity(0, 0, 300)
                        toast.show()
                    }
                }
            })
        }


        return when (item.itemId) {
            R.id.settings -> {
                dialog.show()
                true
            }
            R.id.bluet -> {
                startActivity(Intent(context, BluetoothScanAcitvity::class.java))
                true
            }
            R.id.fit -> {
                dialogGoogleFit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //this function allows the user height to be entered
    fun recordHeight(view: View) {

        //variables
        val mostrarAltura: TextView  = view.findViewById(R.id.muestraAltura)
        val mostrarPeso : TextView = view.findViewById(R.id.muestraPeso)
        val LL = LinearLayout(context)
        val builder = AlertDialog.Builder(context)

        //we create a numberpicker for the natural number
        val pAltura = NumberPicker(context)
        pAltura.maxValue = 280
        pAltura.minValue = 50
        val f = Integer.parseInt(mostrarAltura.text.toString())
        if(f != 0){
            pAltura.value = f
        }

        //create a view
        val params = LinearLayout.LayoutParams(50, 50)
        params.gravity = Gravity.CENTER

        val numPicerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        numPicerParams.weight = 1f

        // we add the number picker to a view to be able to show it later
        LL.layoutParams = params
        LL.addView(pAltura, numPicerParams)

        //call to the database and read the data
        val mysql: ConexionSQLHelper = ConexionSQLHelper(context)
        val datos = mysql.readMeasures()

        servidor.getIP()

        // a dialog opens to be able to select the height value
        with(builder)
        {

            setTitle("Altura")
            setView(LL)
            setCancelable(false)
            setPositiveButton("Aceptar",  DialogInterface.OnClickListener { dialog, id ->
                val v: String = "" + pAltura.value
                mostrarAltura.setText(v)

                if(datos.size == 0){
                    // if there is no data entered, then POST method
                    mysql.insertMeasures(v, mostrarPeso.text.toString())
                    servidor.postRequestMeasures(postUrlMeasures, pAltura.value, mostrarPeso.text.toString().toDouble())
                } else {
                    // if there is data, then we update it with the PUT method
                    mysql.updateMeasures(v, mostrarPeso.text.toString())
                    servidor.putRequestMeasures(postUrlMeasures, pAltura.value, mostrarPeso.text.toString().toDouble())
                }




            })
            setNegativeButton("Cancelar", null)
            show()

        }



    }

    //this function allows the user weight to be entered
    fun recordWeight(view: View){

        //variables
        val mostrarPeso : TextView = view.findViewById(R.id.muestraPeso)
        val mostrarAltura : TextView = view.findViewById(R.id.muestraAltura)
        val LL = LinearLayout(context)
        val builder = AlertDialog.Builder(context)

        //show weight in decimal form
        val numeros: Array<String> = mostrarPeso.text.split(".").toTypedArray()
        val ints = IntArray(2)
        for (i in 0 until 2) {
            ints[i] = numeros[i].toInt()
        }


        // we create two numberpickers, one for the natural number and one for the decimal number
        val pPeso = NumberPicker(context)
        pPeso.maxValue = 250
        pPeso.minValue = 10
        if(ints[0] != 0){
            pPeso.value = ints[0]
        }


        val pDecim = NumberPicker(context)
        pDecim.maxValue = 9
        pDecim.minValue = 0
        pDecim.value = ints[1]

        //create a view
        val params = LinearLayout.LayoutParams(50, 50)
        params.gravity = Gravity.CENTER

        val numPicerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        numPicerParams.weight = 1f

        val qPicerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        qPicerParams.weight = 1f

        // we add both number pickers to a view view to be able to display it later
        LL.layoutParams = params
        LL.addView(pPeso, numPicerParams)
        LL.addView(pDecim, qPicerParams)

        //call to the database and read the data
        val mysql: ConexionSQLHelper = ConexionSQLHelper(context)
        val datos = mysql.readMeasures()

        servidor.getIP()

        // a dialog opens to be able to select the height value
        with(builder)
        {

            setTitle("Peso")
            setView(LL)
            setCancelable(false)
            setPositiveButton("Aceptar", DialogInterface.OnClickListener{ dialog, id ->
                val s: String = "" + pPeso.value + "." + pDecim.value
                mostrarPeso.setText(s)

                if(datos.size == 0){
                    mysql.insertMeasures(mostrarAltura.text.toString(), s)
                    // if there is no data entered, then POST method
                    servidor.postRequestMeasures(postUrlMeasures, Integer.parseInt(mostrarAltura.text.toString()), s.toDouble())
                } else {
                    mysql.updateMeasures(mostrarAltura.text.toString(), s)
                    // if there is data, then we update it with the PUT method
                    servidor.putRequestMeasures(postUrlMeasures, Integer.parseInt(mostrarAltura.text.toString()), s.toDouble())
                }


            })
            setNegativeButton("Cancelar", null)
            show()

        }


    }

    //this function allows you to record the current time
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun recordWeather(view: View) {

        //variables
        val tiempo = AlertDialog.Builder(context)
        val mysql = ConexionSQLHelper(context)
        val datos = mysql.readWeather()

        var j = 0

        //create and show a dialog to choose the time of the current day
        with(tiempo) {
            setTitle("¿Qué tiempo hace hoy?")
            setCancelable(false)
            setMultiChoiceItems(
                itemsWeather,
                marcados, DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
                    marcados[which] = isChecked
                })
            setPositiveButton("Guardar", { dialog, which ->
                list.clear()
                for (i in 0 until itemsWeather.size) {
                    val checked = marcados[i]
                    if (checked) {
                        list.add(itemsWeather[i])
                        j++
                    }
                }
                if(datos.length() == 0){
                    mysql.insertWeather(list)
                    // if there is no data entered, then POST method
                    servidor.postRequestWeather(postUrlWeather, list)
                } else {
                    mysql.updateWeather(list)
                    // if there is data, then we update it with the PUT method
                    servidor.putRequestWeather(postUrlWeather, list)
                }


            })
            setNegativeButton("Cancelar",  {dialog, which ->
                if(list.size == 0) {
                    for (i in 0 until itemsWeather.size) {
                        val checked = marcados[i]
                        if (checked) {
                            marcados[i] = false
                        }
                    }
                }

            })
            show()
        }



    }

    //this function allows you to see how the user feels
    fun showEmotionalState(view: View) {

        //call to the database and read the data of the emotional state that is registered in the database
        val mysql: ConexionSQLHelper = ConexionSQLHelper(context)
        val emocionDatos = mysql.readEmotion()

        //variable
        var text: String = ""

        if(emocionDatos.size != 0) {
            if(emocionDatos[0] == fechaRegistro) {
                for(i in 0..itemsEmotion.size-1) {
                    if(emocionDatos[1] == itemsEmotion[i]) {
                        if(i < 10) {
                            text = "Enhorabuena!! lo estás haciendo muy bien, sigue así."
                        } else if(i >= 10 && i < 20) {
                            text = "No te desanimes, eres capaz de conseguir todo lo que te propongas. Ánimo!!."
                        } else if(i >= 20) {
                            text = "Sé que a veces es duro, pero puedes salir adelante, mañana será otro día. Si lo necesitas, no olvides pedir ayuda."
                        }
                    }
                }


                if(emocionDatos[3] == "") {
                    emocionDatos[3] = "Ninguno"
                }
                //dialog to show advices
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Tu estado emocional")
                    .setMessage(
                        Html.fromHtml("<br><b>" + emocionDatos[2] + " " + emocionDatos[1] + "</b><br><br>" +
                                "<b>Motivo: </b>" + emocionDatos[3] + "<br><br>" + text + "<br>" ))
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .create()

                dialog.show()

            } else {
                //dialog to show advices
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Tu estado emocional")
                    .setMessage(
                        Html.fromHtml("<br>No hay registro<br><br>"))
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .create()

                dialog.show()
            }
        } else {

            //dialog to show advices
            val dialog: AlertDialog = AlertDialog.Builder(context)
                .setTitle("Tu estado emocional")
                .setMessage(
                    Html.fromHtml("<br>No hay registro<br><br>"))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()

            dialog.show()
        }



    }

    //create a client of fitness
    @RequiresApi(Build.VERSION_CODES.O)
    fun dialogGoogleFit() {
     //   super.onStart()
       fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
            .accessActivitySessions(FitnessOptions.ACCESS_READ)
            .build()


        // Check if user is signed in (non-null) and update UI accordingly.
        val acc = GoogleSignIn.getLastSignedInAccount(context)
        if(!acc?.email.equals(null)) {
            val account = context?.let { GoogleSignIn.getAccountForExtension(it, fitnessOptions) }
            if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Ya tienes permisos de Google")
                    .setMessage("¿Quieres cerrar sesión en tu cuenta de Google?")
                    .setPositiveButton("Aceptar",  DialogInterface.OnClickListener { dialog, id ->
                        val client = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build()
                        context?.let { GoogleSignIn.getClient(it, client).signOut() }

                        Toast.makeText(context, "La sesión ha sido cerrada correctamente", Toast.LENGTH_SHORT).show()

                    })
                    .setNegativeButton("Cancelar", null)
                    .setCancelable(false)
                    .create()

                dialog.show()

            } else {
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("No tienes permisos de Google")
                    .setMessage("¿Quieres iniciar sesión con tu cuenta de Google?")
                    .setPositiveButton("Aceptar",  DialogInterface.OnClickListener { dialog, id ->
                        GoogleSignIn.requestPermissions(
                            this, // your activity
                            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                            account,
                            fitnessOptions)
                    })
                    .setNegativeButton("Cancelar", null)
                    .setCancelable(false)
                    .create()

                dialog.show()
            }
        } else {
            val account = context?.let { GoogleSignIn.getAccountForExtension(it, fitnessOptions) }
            val dialog: AlertDialog = AlertDialog.Builder(context)
                .setTitle("No tienes permisos de Google")
                .setMessage("¿Quieres iniciar sesión con tu cuenta de Google?")
                .setPositiveButton("Aceptar",  DialogInterface.OnClickListener { dialog, id ->
                    GoogleSignIn.requestPermissions(
                        this, // your activity
                        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                        account,
                        fitnessOptions)
                })
                .setNegativeButton("Cancelar", null)
                .setCancelable(false)
                .create()

            dialog.show()
        }


    }

    //access to data of the client of fitness
    @RequiresApi(Build.VERSION_CODES.O)
    fun onlyIfHasPermissions() {
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
            .accessActivitySessions(FitnessOptions.ACCESS_READ)
            .build()

        val fit = GoogleFitness()
        context?.let { fit.accessGoogleFit(fitnessOptions, it) }
    }

    //create a client of fitness
 /*   @RequiresApi(Build.VERSION_CODES.O)
    fun createApiFitness() {
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
            .accessActivitySessions(FitnessOptions.ACCESS_READ)
            .build()

        val account = context?.let { GoogleSignIn.getAccountForExtension(it, fitnessOptions) }

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                account,
                fitnessOptions)
        } else {
            val fit = GoogleFitness()
            context?.let { fit.accessGoogleFit(fitnessOptions, it) }
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fit = GoogleFitness()
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> context?.let {
                    fit.accessGoogleFit(fitnessOptions,
                        it
                    )
                }
                else -> {
                    // Result wasn't from Google Fit
                    Log.i(TAG, "Result wasn't from Google Fit")
                }
            }
            else -> {
                // Permission not granted
                Log.i(TAG, "Permission not granted")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserProfile()

        //list with with various weather possibilities
        val itemsWeather: Array<String> = arrayOf("Soleado", "Nublado", "Lluvia", "Nieve", "Viento", "Frío", "Calor", "Tormenta")
        //variables
        val marcados = BooleanArray(itemsWeather.size)
        val list: MutableList<String> = ArrayList()
        lateinit var fitnessOptions: FitnessOptions
        val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
        val TAG = "GoogleFit"

    }







}