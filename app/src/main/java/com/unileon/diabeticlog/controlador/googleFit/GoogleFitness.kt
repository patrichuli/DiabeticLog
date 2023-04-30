package com.unileon.diabeticlog.controlador.googleFit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.SessionReadRequest
import com.unileon.diabeticlog.modelo.ConexionSQLHelper
import com.unileon.diabeticlog.modelo.ConexionSQLHelper.Companion.fechaRegistro
import com.unileon.diabeticlog.modelo.MetodosServidor
import com.unileon.diabeticlog.modelo.MetodosServidor.Companion.postUrlSteps
import com.unileon.diabeticlog.vista.UserProfile
import com.unileon.diabeticlog.vista.UserProfile.Companion.TAG
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit


@RequiresApi(Build.VERSION_CODES.O)
class GoogleFitness {

    private val servidor = MetodosServidor()

    fun accessGoogleFit(fitnessOptions: FitnessOptions, context: Context) {

        stepsData(fitnessOptions, context)
        sessionActivity(fitnessOptions, context)
       // historialData(fitnessOptions, context)

    }

    //function to count the daily steps
    fun stepsData(fitnessOptions: FitnessOptions, context: Context) {
        val end = LocalDateTime.now()
        val start = end.minusYears(1)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        Fitness.getHistoryClient(context, account)
            .readData(readRequest)
            .addOnSuccessListener({ response ->
                // Use response data here
                Log.i(UserProfile.TAG, "OnSuccess()")
                readStepCounter(fitnessOptions, context)
            })
            .addOnFailureListener({ e -> Log.d(UserProfile.TAG, "OnFailure()", e) })
    }

    fun readStepCounter(fitnessOptions: FitnessOptions, context: Context) {
        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result ->
                val totalSteps = result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
               //   Toast.makeText(context, "Total steps: " + totalSteps, Toast.LENGTH_SHORT).show()
                val mysql = ConexionSQLHelper(context)
                val datos = mysql.readSteps()
                if(datos.size != 0) {
                    mysql.updateSteps(totalSteps)
                    servidor.putRequestSteps(postUrlSteps, totalSteps)
                } else {
                    mysql.insertSteps(totalSteps)
                    servidor.postRequestSteps(postUrlSteps, totalSteps)
                }

            }
            .addOnFailureListener { e ->
                Log.i(TAG, "There was a problem getting steps.", e)
            }

    }

    fun historialData(fitnessOptions: FitnessOptions, context: Context) {
     //   val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
     //   val startTime = endTime.minusDays(1)

        val cal: Calendar = Calendar.getInstance()
        val now = Date()
        cal.setTime(now)
        val endTime: Long = cal.getTimeInMillis()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val startTime: Long = cal.getTimeInMillis()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_CALORIES_EXPENDED)
            .bucketByActivityType(1, TimeUnit.MINUTES)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // The aggregate query puts datasets into buckets, so flatten into a
                // single list of datasets
                for (dataSet in response.buckets.flatMap { it.dataSets }) {
                    dumpDataSet(dataSet)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG,"There was an error reading data from Google Fit", e)
            }
    }

    fun dumpDataSet(dataSet: DataSet): String {
        var calories: String = ""
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            Log.i(TAG,"Data point:")
            Log.i(TAG,"\tType: ${dp.dataType.name}")
            Log.i(TAG,"\tStart: ${dp.getStartTimeString()}")
            Log.i(TAG,"\tEnd: ${dp.getEndTimeString()}")

            for (field in dp.dataType.fields) {
                Log.i(TAG,"\tField: ${field.name} Value: ${dp.getValue(field)}")
                calories = dp.getValue(field).toString()
            }
        }

        return calories
    }

    //function to read activities from GoogleFit
    fun sessionActivity(fitnessOptions: FitnessOptions, context: Context) {
        // Use a start time of today and an end time of now.
        val cal: Calendar = Calendar.getInstance()
        val now = Date()
        cal.setTime(now)
        val endTime: Long = cal.getTimeInMillis()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
      //  cal.add(Calendar.WEEK_OF_YEAR, -1)
        val startTime: Long = cal.getTimeInMillis()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                1)
        }

        //variable to insert in database
        var startActivity = ""
        var endActivity = ""
        var nameActivity = ""
        var calories = ""
        val intensity = "0"

        //call to the database
        val mysql = ConexionSQLHelper(context)

        // Build a session read request
        val readRequest = SessionReadRequest.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .read(DataType.TYPE_WORKOUT_EXERCISE)
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .readSessionsFromAllApps()
            .build()

        Fitness.getSessionsClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
            .readSession(readRequest)
            .addOnSuccessListener { response ->
                // Get a list of the sessions that match the criteria to check the result.
                val sessions = response.sessions
                Log.i(TAG, "Number of returned sessions is: ${sessions.size}")
                for (session in sessions) {
                    // Process the session
                    dumpSession(session)


                    nameActivity = session.activity
                    var startActivity = DateFormat.getTimeInstance(DateFormat.SHORT).format(session.getStartTime(TimeUnit.MILLISECONDS))
                    var endActivity = DateFormat.getTimeInstance(DateFormat.SHORT).format(session.getEndTime(TimeUnit.MILLISECONDS))
                    if(startActivity.split(":")[0].length == 1){
                        startActivity = "0$startActivity"
                    }
                    if(endActivity.split(":")[0].length == 1){
                        endActivity = "0$endActivity"
                    }

                    // Process the data sets for this session
                    val dataSets = response.getDataSet(session)
                    for (dataSet in dataSets) {
                        val dev = dumpDataSet(dataSet)
                        if(dev != "") {
                            calories = dev
                        }

                    }

                    val datos = mysql.readAllSportTable()
                    var existe = false

                    if(datos.size == 0) {
                        mysql.insertSport(nameActivity, startActivity, endActivity, intensity,
                            calories.split(".")[0]
                        )
                    } else {
                        for(i in 0 until datos.size) {
                            if(nameActivity == datos[i].nombre && startActivity == datos[i].hora) {
                                existe = true
                                mysql.updateSport(fechaRegistro,
                                    datos.get(i).hora!!,
                                    datos.get(i).nombre!!, nameActivity, startActivity, endActivity, intensity, calories.split(".").get(0))
                            }
                        }

                        if(!existe) {
                            mysql.insertSport(nameActivity, startActivity, endActivity, intensity, calories.split(".").get(0))
                        }
                    }



                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG,"Failed to read session", e)
            }


    }

    fun DataPoint.getStartTimeString() = Instant.ofEpochSecond(this.getStartTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    fun DataPoint.getEndTimeString() = Instant.ofEpochSecond(this.getEndTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    fun Session.getStartTimeString() = Instant.ofEpochSecond(this.getStartTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    fun Session.getEndTimeString() = Instant.ofEpochSecond(this.getEndTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    fun dumpSession(session: Session) {

        Log.i(TAG, "Data returned for Session: " + session.name
                + "\n\tIdentifier: " + session.identifier
                + "\n\tDescription: " + session.description
                + "\n\tStart: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(session.getEndTime(TimeUnit.MILLISECONDS))
                + "\n\tActivity: " + session.activity)
    }


}