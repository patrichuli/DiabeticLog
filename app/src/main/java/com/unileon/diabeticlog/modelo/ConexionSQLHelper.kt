package com.unileon.diabeticlog.modelo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.BaseColumns
import android.util.Base64
import androidx.annotation.RequiresApi
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.data.DatosRegistrados
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ConexionSQLHelper(var context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 5
        const val DATABASE_NAME = "DataBasesDiabeticLog.db"

        //actual date and hour
        val dateFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = dateFormat.format(Calendar.getInstance().getTime())

        //only actual date
        val format = SimpleDateFormat("dd/MM/yyyy")
        val fechaRegistro = format.format(Calendar.getInstance().getTime())

        //date of the weather
        var weatherDate = ""
    }

    //variables in the table of measures
    object Reader {
        object Medida : BaseColumns{
            const val TABLE = "medida"
            const val COLUMN_ALTURA = "altura"
            const val COLUMN_PESO = "peso"

        }
    }


    private val SQL_CREATE_MEDIDA =
            "CREATE TABLE ${Reader.Medida.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Reader.Medida.COLUMN_ALTURA} INTEGER," +
                    "${Reader.Medida.COLUMN_PESO} DOUBLE)"


    private val SQL_DELETE_MEDIDA = "DROP TABLE IF EXISTS ${Reader.Medida.TABLE}"

    //variables in the table of weather
    object Tiempo : BaseColumns{
        const val TABLE = "tabla_tiempo"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_TIEMPO = "tiempo"
    }


    private val SQL_CREATE_TIEMPO =
            "CREATE TABLE ${Tiempo.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Tiempo.COLUMN_FECHA} TEXT," +
                    "${Tiempo.COLUMN_TIEMPO} TEXT)"


    private val SQL_DELETE_TIEMPO = "DROP TABLE IF EXISTS ${Tiempo.TABLE}"

    //variables in the table of steps
    object PasosDiarios : BaseColumns{
        const val TABLE = "tabla_pasos"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_PASOS = "pasos"
    }


    private val SQL_CREATE_PASOS =
        "CREATE TABLE ${PasosDiarios.TABLE} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${PasosDiarios.COLUMN_FECHA} TEXT," +
                "${PasosDiarios.COLUMN_PASOS} INTEGER)"


    private val SQL_DELETE_PASOS = "DROP TABLE IF EXISTS ${PasosDiarios.TABLE}"

    //variables in the table of emotion
    object Emocion : BaseColumns{
        const val TABLE = "tabla_emocion"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_EMOCION = "emocion"
        const val COLUMN_INTENSIDAD = "intensidad"
        const val COLUMN_MOTIVO = "motivo"
    }


    private val SQL_CREATE_EMOCION =
            "CREATE TABLE ${Emocion.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Emocion.COLUMN_FECHA} TEXT," +
                    "${Emocion.COLUMN_EMOCION} TEXT," +
                    "${Emocion.COLUMN_INTENSIDAD} TEXT," +
                    "${Emocion.COLUMN_MOTIVO} TEXT)"


    //  private val SQL_DELETE_EMOCION = "DELETE FROM ${Emocion.TABLE} WHERE ${date} < date('now', '-1 day')"
    private val SQL_DELETE_EMOCION = "DROP TABLE IF EXISTS ${Emocion.TABLE}"

    //variables in the table of sport
    object Deporte : BaseColumns{
        const val TABLE = "tabla_deporte"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_INICIO = "inicio"
        const val COLUMN_FIN = "fin"
        const val COLUMN_INTENSIDAD = "intensidad"
        const val COLUMN_CALORIAS = "calorias"
    }


    private val SQL_CREATE_DEPORTE =
            "CREATE TABLE ${Deporte.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Deporte.COLUMN_FECHA} TEXT," +
                    "${Deporte.COLUMN_TIPO} TEXT," +
                    "${Deporte.COLUMN_INICIO} TEXT," +
                    "${Deporte.COLUMN_FIN} TEXT," +
                    "${Deporte.COLUMN_INTENSIDAD} INTEGER," +
                    "${Deporte.COLUMN_CALORIAS} INTEGER)"


    private val SQL_DELETE_DEPORTE = "DROP TABLE IF EXISTS ${Deporte.TABLE}"

    //variables in the table of insulin
    object Insulina : BaseColumns{
        const val TABLE = "tabla_insulina"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_HORA_REGISTRO = "hora_registro"
        const val COLUMN_TIPO_INSULINA = "tipo_insulina"
        const val COLUMN_UNIDADES = "unidades"
        const val COLUMN_TIPO_APLICACION = "tipo_aplicacion"
    }


    private val SQL_CREATE_INSULINA =
            "CREATE TABLE ${Insulina.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Insulina.COLUMN_FECHA} TEXT," +
                    "${Insulina.COLUMN_HORA_REGISTRO} TEXT," +
                    "${Insulina.COLUMN_TIPO_INSULINA} TEXT," +
                    "${Insulina.COLUMN_UNIDADES} INTEGER," +
                    "${Insulina.COLUMN_TIPO_APLICACION} TEXT)"


    private val SQL_DELETE_INSULINA = "DROP TABLE IF EXISTS ${Insulina.TABLE}"


    //variables in the table of feeding
    object Alimentacion : BaseColumns{
        const val TABLE = "tabla_alimentacion"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_HORA_REGISTRO = "hora_registro"
        const val COLUMN_ALIMENTO = "alimento"
        const val COLUMN_FOTO = "foto"
        const val COLUMN_RACIONES = "raciones"
    }


    private val SQL_CREATE_ALIMENTACION =
            "CREATE TABLE ${Alimentacion.TABLE} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Alimentacion.COLUMN_FECHA} TEXT," +
                    "${Alimentacion.COLUMN_HORA_REGISTRO} TEXT," +
                    "${Alimentacion.COLUMN_ALIMENTO} TEXT," +
                    "${Alimentacion.COLUMN_FOTO} BLOB," +
                    "${Alimentacion.COLUMN_RACIONES} INTEGER)"


    private val SQL_DELETE_ALIMENTACION = "DROP TABLE IF EXISTS ${Alimentacion.TABLE}"


    //variables in the table of glucose
    object Glucosa : BaseColumns{
        const val TABLE = "tabla_glucosa"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_HORA_REGISTRO = "hora_registro"
        const val COLUMN_NIVELES_GLUCOSA = "niveles_glucosa"
        const val COLUMN_CICLO_MENSTRUAL = "ciclo_menstrual"
        const val COLUMN_INICIO_CICLO = "inicio_ciclo"
        const val COLUMN_FIN_CICLO = "fin_ciclo"
    }


    private val SQL_CREATE_GLUCOSA =
        "CREATE TABLE ${Glucosa.TABLE} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${Glucosa.COLUMN_FECHA} TEXT," +
                "${Glucosa.COLUMN_HORA_REGISTRO} TEXT," +
                "${Glucosa.COLUMN_NIVELES_GLUCOSA} INTEGER," +
                "${Glucosa.COLUMN_CICLO_MENSTRUAL} TEXT," +
                "${Glucosa.COLUMN_INICIO_CICLO} TEXT," +
                "${Glucosa.COLUMN_FIN_CICLO} TEXT)"


    private val SQL_DELETE_GLUCOSA = "DROP TABLE IF EXISTS ${Glucosa.TABLE}"

    //to create all the tables
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_MEDIDA)
        db.execSQL(SQL_CREATE_TIEMPO)
        db.execSQL(SQL_CREATE_EMOCION)
        db.execSQL(SQL_CREATE_DEPORTE)
        db.execSQL(SQL_CREATE_INSULINA)
        db.execSQL(SQL_CREATE_ALIMENTACION)
        db.execSQL(SQL_CREATE_GLUCOSA)
        db.execSQL(SQL_CREATE_PASOS)
    }

    //the previous version of the database is deleted
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_MEDIDA)
        db.execSQL(SQL_DELETE_TIEMPO)
        db.execSQL(SQL_DELETE_EMOCION)
        db.execSQL(SQL_DELETE_DEPORTE)
        db.execSQL(SQL_DELETE_INSULINA)
        db.execSQL(SQL_DELETE_ALIMENTACION)
        db.execSQL(SQL_DELETE_GLUCOSA)
        db.execSQL(SQL_DELETE_PASOS)
        onCreate(db)

    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    //close the database
    fun onDestroy() {
        this.close()

    }

    //METHODS OF MEASUREMENT TABLE

    //insert the height and weight in the database
    fun insertMeasures(altura: String, peso: String){
        val db = this.writableDatabase

        val a = altura.toInt()
        val p = peso.toDouble()

        val values = ContentValues()
        values.put("altura", a)
        values.put("peso", p)

        val newRowId = db.insert("medida", null, values)
        /*  if (newRowId == (0).toLong()) {
              Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
          }
          else {
              Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
          }*/

        //   onDestroy()

    }

    //read the data from the database
    fun readMeasures(): MutableList<String> {

        val list: MutableList<String> = ArrayList()
        val db = this.readableDatabase

        val query = "SELECT * FROM ${Reader.Medida.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val altura = result.getInt(result.getColumnIndex("altura"))
                val peso = result.getDouble(result.getColumnIndex("peso"))
                list.add(altura.toString())
                list.add(peso.toString())
            }
            while (result.moveToNext())
        }

        //    onDestroy()

        return list


    }

    //update the values
    fun updateMeasures(altura: String, peso: String){

        val db = this.writableDatabase

        val a = altura.toInt()
        val p = peso.toDouble()
        //new value for a column
        val values = ContentValues().apply {
            put("altura", a)
            put("peso", p)
        }

        //que fila actualizar WHERE x LIKE ?
        // val selection = "${ReaderUsuario.Usuario.COLUMN_NAME_TITLE} LIKE ?"
        // val selectionArgs = arrayOf("MyOldTitle")
        db.update(
                Reader.Medida.TABLE,
                values,
                null,
                null)

        //    onDestroy()

    }

    //METHODS OF WEATHER TABLE

    //insert the weather in the database
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun insertWeather(opciones: MutableList<String>){
        val db = this.writableDatabase

        val json = JSONObject()
        json.put("opciones", JSONArray(opciones))
        val arrayList = json.toString()

        val values = ContentValues()
        values.put("fecha", fechaRegistro)
        values.put("tiempo", arrayList)

        db.insert("tabla_tiempo", null, values)

        //   onDestroy()

    }

    //read the data from the database
    fun readWeather(): JSONArray {

        var list = JSONArray()
        val db = this.readableDatabase

        val strSeparator = ", "

        val query = "SELECT * FROM ${Tiempo.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val fecha = result.getString(result.getColumnIndex("fecha"))
                val tiempo = result.getString(result.getColumnIndex("tiempo"))
                val json = JSONObject(tiempo)
                weatherDate = fecha
                list = json.optJSONArray("opciones")

            }
            while (result.moveToNext())
        }


        //   onDestroy()

        return list


    }

    //update the values
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun updateWeather(opciones: MutableList<String>){

        val db = this.writableDatabase

        val json = JSONObject()
        json.put("opciones", JSONArray(opciones))
        val arrayList = json.toString()

        // nuevo valor para una columna
        val values = ContentValues().apply {
            put("fecha", fechaRegistro)
            put("tiempo",  arrayList)
        }

        db.update(
                Tiempo.TABLE,
                values,
                null,
                null)

        //    onDestroy()

    }


    //METHODS OF STEPS TABLE


    //insert the values in the database
    fun insertSteps(pasos: Int){

        val db = this.writableDatabase

        val values = ContentValues()
        values.put("fecha", fechaRegistro)
        values.put("pasos", pasos)

        db.insert("tabla_pasos", null, values)

        //    onDestroy()

    }

    //read the data from the database
    fun readSteps(): MutableList<String> {

        val list: MutableList<String> = ArrayList()
        val db = this.readableDatabase

        val query = "SELECT * FROM ${PasosDiarios.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val fecha = result.getString(result.getColumnIndex("fecha"))
                val pasos = result.getInt(result.getColumnIndex("pasos"))
                list.add(fecha)
                list.add(pasos.toString())
            }
            while (result.moveToNext())
        }

        //   onDestroy()

        return list


    }

    //update the values
    fun updateSteps(pasos: Int){

        val db = this.writableDatabase

        // nuevo valor para una columna
        val values = ContentValues().apply {
            put("fecha", fechaRegistro)
            put("pasos", pasos)
        }


        db.update(
            PasosDiarios.TABLE,
            values,
            null,
            null)

        //    onDestroy()

    }


    //METHODS OF EMOTION TABLE


    //insert the values in the database
    fun insertEmotion(emocion: String, intensidad: String, motivo: String){

        val db = this.writableDatabase

        val values = ContentValues()
        values.put("fecha", fechaRegistro)
        values.put("emocion", emocion)
        values.put("intensidad", intensidad)
        values.put("motivo", motivo)

        db.insert("tabla_emocion", null, values)

        //    onDestroy()

    }

    //read the data from the database
    fun readEmotion(): MutableList<String> {

        val list: MutableList<String> = ArrayList()
        val db = this.readableDatabase

        val query = "SELECT * FROM ${Emocion.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val fecha = result.getString(result.getColumnIndex("fecha"))
                val emocion = result.getString(result.getColumnIndex("emocion"))
                val intensidad = result.getString(result.getColumnIndex("intensidad"))
                val motivo = result.getString(result.getColumnIndex("motivo"))
                list.add(fecha)
                list.add(emocion)
                list.add(intensidad)
                list.add(motivo)
            }
            while (result.moveToNext())
        }

        //   onDestroy()

        return list


    }

    //update the values
    fun updateEmotion(emocion: String, intensidad: String, motivo: String){

        val db = this.writableDatabase

        // nuevo valor para una columna
        val values = ContentValues().apply {
            put("fecha", fechaRegistro)
            put("emocion", emocion)
            put("intensidad", intensidad)
            put("motivo", motivo)
        }


        db.update(
                Emocion.TABLE,
                values,
                null,
                null)

        //    onDestroy()

    }


    //METHODS OF SPORT TABLE

    //insert the values in the database
    fun insertSport(tipo: String, inicio: String, fin: String, intensidad: String, calorias: String){

        val db = this.writableDatabase

        val values = ContentValues()
        values.put("fecha", fechaRegistro)
        values.put("tipo", tipo)
        values.put("inicio", inicio)
        values.put("fin", fin)
        values.put("intensidad", intensidad.toInt())
        values.put("calorias", calorias.toInt())

        db.insert("tabla_deporte", null, values)

        //    onDestroy()

    }

    //read the data from the database
    fun readSport(fecha: String, inicio: String, tipo: String): MutableList<String> {

        val list: MutableList<String> = ArrayList()
        val db = this.readableDatabase

        val query = "SELECT * FROM ${Deporte.TABLE} WHERE ${Deporte.COLUMN_TIPO} = ? AND ${Deporte.COLUMN_FECHA} = ? AND ${Deporte.COLUMN_INICIO} = ?"
        val selectionArgs = arrayOf(tipo, fecha, inicio)
        val result = db.rawQuery(query, selectionArgs)
        if(result.moveToFirst()){
            do {

                val fecha = result.getString(result.getColumnIndex("fecha"))
                val tipo = result.getString(result.getColumnIndex("tipo"))
                val inicio = result.getString(result.getColumnIndex("inicio"))
                val fin = result.getString(result.getColumnIndex("fin"))
                val intensidad = result.getInt(result.getColumnIndex("intensidad"))
                val calorias = result.getInt(result.getColumnIndex("calorias"))
                list.add(fecha)
                list.add(tipo)
                list.add(inicio)
                list.add(fin)
                list.add(intensidad.toString())
                list.add(calorias.toString())
            }
            while (result.moveToNext())
        }

        //    onDestroy()

        return list


    }

    //update the values in the indicated row
    fun updateSport(fecha: String, horaAnterior: String, nombre: String, tipo: String, inicio: String, fin: String, intensidad: String, calorias: String){

        val db = this.writableDatabase

        // nuevo valor para una columna
        val values = ContentValues().apply {
            put("tipo", tipo)
            put("inicio", inicio)
            put("fin", fin)
            put("intensidad", intensidad.toInt())
            put("calorias", calorias.toInt())
        }

        //que fila actualizar WHERE x LIKE ?
        val selection = "${Deporte.COLUMN_TIPO} LIKE ? AND ${Deporte.COLUMN_FECHA} LIKE ? AND ${Deporte.COLUMN_INICIO} LIKE ?"
        val selectionArgs = arrayOf(nombre, fecha, horaAnterior)
        db.update(
                Deporte.TABLE,
                values,
                selection,
                selectionArgs)

        //  onDestroy()

    }

    //delete the indicated row in the database
    fun deleteSport(fecha: String, inicio: String, tipo: String) {

        val db = this.writableDatabase
        // Define 'where' part of query.
        val selection = "${Deporte.COLUMN_TIPO} LIKE ? AND ${Deporte.COLUMN_FECHA} LIKE ? AND ${Deporte.COLUMN_INICIO} LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(tipo, fecha, inicio)
        // Issue SQL statement.
        db.delete(Deporte.TABLE, selection, selectionArgs)

        //   onDestroy()
    }


    //METHODS OF INSULIN TABLE

    //insert all the values in the database
    fun insertInsulin(hora: String, tipoInsulina: String, unidades: String, tipoAplicacion: String){

        val db = this.writableDatabase

        val values = ContentValues()
        values.put("fecha", fechaRegistro)
        values.put("hora_registro", hora)
        values.put("tipo_insulina", tipoInsulina)
        values.put("unidades", unidades.toInt())
        values.put("tipo_aplicacion", tipoAplicacion)

        db.insert("tabla_insulina", null, values)

        //    onDestroy()

    }

    //read the data from the database
    fun readInsulin(fecha: String, hora: String, tipoInsulina: String): MutableList<String> {

        val list: MutableList<String> = ArrayList()
        val db = this.readableDatabase

        val query = "SELECT * FROM ${Insulina.TABLE} WHERE ${Insulina.COLUMN_TIPO_INSULINA} = ? AND ${Insulina.COLUMN_FECHA} = ? AND ${Insulina.COLUMN_HORA_REGISTRO} = ?"
        val selectionArgs = arrayOf(tipoInsulina, fecha, hora)
        val result = db.rawQuery(query, selectionArgs)
        if(result.moveToFirst()){
            do {

                val fecha = result.getString(result.getColumnIndex("fecha"))
                val hora = result.getString(result.getColumnIndex("hora_registro"))
                val tipoInsulina = result.getString(result.getColumnIndex("tipo_insulina"))
                val unidades = result.getInt(result.getColumnIndex("unidades"))
                val tipoAplicacion = result.getString(result.getColumnIndex("tipo_aplicacion"))
                list.add(fecha)
                list.add(hora)
                list.add(tipoInsulina)
                list.add(unidades.toString())
                list.add(tipoAplicacion)
            }
            while (result.moveToNext())
        }

        //   onDestroy()


        return list


    }

    //update the values in the indicated row
    fun updateInsulin(fecha: String, horaAnterior: String, hora: String, nombre: String, tipoInsulina: String, unidades: String, tipoAplicacion: String){

        val db = this.writableDatabase

        // nuevo valor para una columna
        val values = ContentValues().apply {
            put("hora_registro", hora)
            put("tipo_insulina", tipoInsulina)
            put("unidades", unidades.toInt())
            put("tipo_aplicacion", tipoAplicacion)
        }

        //que fila actualizar WHERE x LIKE ?
        val selection = "${Insulina.COLUMN_TIPO_INSULINA} LIKE ? AND ${Insulina.COLUMN_FECHA} LIKE ? AND ${Insulina.COLUMN_HORA_REGISTRO} LIKE ?"
        val selectionArgs = arrayOf(nombre, fecha, horaAnterior)
        db.update(
                Insulina.TABLE,
                values,
                selection,
                selectionArgs)

        //    onDestroy()

    }

    //delete the indicated row in the database
    fun deleteInsulin(fecha: String, hora: String, tipoInsulina: String) {

        val db = this.writableDatabase
        // Define 'where' part of query.
        val selection = "${Insulina.COLUMN_TIPO_INSULINA} LIKE ? AND ${Insulina.COLUMN_FECHA} LIKE ? AND ${Insulina.COLUMN_HORA_REGISTRO} LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(tipoInsulina, fecha, hora)
        // Issue SQL statement.
        db.delete(Insulina.TABLE, selection, selectionArgs)

        //    onDestroy()
    }


    //METHODS OF FEEDING TABLE

    //insert the data in the database
    fun insertFeeding(hora: String, alimento: String, foto: ByteArray, raciones: String){

        val db = this.writableDatabase

        val values = ContentValues()
        values.put("fecha", fechaRegistro)
        values.put("hora_registro", hora)
        values.put("alimento", alimento)
        values.put("foto", foto)
        values.put("raciones", raciones.toInt())

        db.insert("tabla_alimentacion", null, values)

        //   onDestroy()

    }

    //read the data from the database
    fun readFeeding(fecha: String, hora: String, tipoAlimento: String): MutableList<String> {

        val list: MutableList<String> = ArrayList()
        val db = this.readableDatabase

        val query = "SELECT * FROM ${Alimentacion.TABLE} WHERE ${Alimentacion.COLUMN_ALIMENTO} = ? AND ${Alimentacion.COLUMN_FECHA} = ? AND ${Alimentacion.COLUMN_HORA_REGISTRO} = ?"
        val selectionArgs = arrayOf(tipoAlimento, fecha, hora)
        val result = db.rawQuery(query, selectionArgs)
        if(result.moveToFirst()){
            do {

                val fecha = result.getString(result.getColumnIndex("fecha"))
                val hora = result.getString(result.getColumnIndex("hora_registro"))
                val alimento = result.getString(result.getColumnIndex("alimento"))
                val foto = result.getBlob(result.getColumnIndex("foto"))
                val raciones = result.getInt(result.getColumnIndex("raciones"))
                list.add(fecha)
                list.add(hora)
                list.add(alimento)
                list.add(Base64.encodeToString(foto, Base64.NO_WRAP))
                list.add(raciones.toString())
            }
            while (result.moveToNext())
        }

        //   onDestroy()

        return list


    }


    //update the values in the indicated row
    fun updateFeeding(fecha: String, horaAnterior: String, hora: String, nombre: String, alimento: String, foto: ByteArray, raciones: String){

        val db = this.writableDatabase

        // nuevo valor para una columna
        val values = ContentValues().apply {
            put("hora_registro", hora)
            put("alimento", alimento)
            put("foto", foto)
            put("raciones", raciones.toInt())
        }

        //que fila actualizar WHERE x LIKE ?
        val selection = "${Alimentacion.COLUMN_ALIMENTO} LIKE ? AND ${Alimentacion.COLUMN_FECHA} LIKE ? AND ${Alimentacion.COLUMN_HORA_REGISTRO} LIKE ?"
        val selectionArgs = arrayOf(nombre, fecha, horaAnterior)
        db.update(
                Alimentacion.TABLE,
                values,
                selection,
                selectionArgs)

        //     onDestroy()

    }

    //delete the indicated row in the database
    fun deleteFeeding(fecha: String, hora: String, tipoAlimento: String) {

        val db = this.writableDatabase
        // Define 'where' part of query.
        val selection = "${Alimentacion.COLUMN_ALIMENTO} LIKE ? AND ${Alimentacion.COLUMN_FECHA} LIKE ? AND ${Alimentacion.COLUMN_HORA_REGISTRO} LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(tipoAlimento, fecha, hora)
        // Issue SQL statement.
        db.delete(Alimentacion.TABLE, selection, selectionArgs)

        //    onDestroy()
    }

    //METHODS OF GLUCOSE TABLE

    //insert all the values in the database
    fun insertGlucose(hora: String, nivelesGlucosa: String, cicloMenstrual: String, inicioCiclo: String, finCiclo: String){

        val db = this.writableDatabase

        val values = ContentValues()
        values.put("fecha", fechaRegistro)
        values.put("hora_registro", hora)
        values.put("niveles_glucosa", nivelesGlucosa.toInt())
        values.put("ciclo_menstrual", cicloMenstrual)
        values.put("inicio_ciclo", inicioCiclo)
        values.put("fin_ciclo", finCiclo)

        db.insert("tabla_glucosa", null, values)

        //    onDestroy()

    }

    //read the data from the database
    fun readGlucose(fecha: String, hora: String, nivelesGlucosa: String): MutableList<String> {

        val list: MutableList<String> = ArrayList()
        val db = this.readableDatabase

        val query = "SELECT * FROM ${Glucosa.TABLE} WHERE ${Glucosa.COLUMN_NIVELES_GLUCOSA} = ? AND ${Glucosa.COLUMN_FECHA} = ? AND ${Glucosa.COLUMN_HORA_REGISTRO} = ?"
        val selectionArgs = arrayOf(nivelesGlucosa, fecha, hora)
        val result = db.rawQuery(query, selectionArgs)
        if(result.moveToFirst()){
            do {

                val fecha = result.getString(result.getColumnIndex("fecha"))
                val horaGlucosa = result.getString(result.getColumnIndex("hora_registro"))
                val nivelesGlucosa = result.getInt(result.getColumnIndex("niveles_glucosa"))
                val cicloMenstrual = result.getString(result.getColumnIndex("ciclo_menstrual"))
                val inicioCiclo = result.getString(result.getColumnIndex("inicio_ciclo"))
                val finCiclo = result.getString(result.getColumnIndex("fin_ciclo"))
                list.add(fecha)
                list.add(horaGlucosa)
                list.add(nivelesGlucosa.toString())
                list.add(cicloMenstrual)
                list.add(inicioCiclo)
                list.add(finCiclo)
            }
            while (result.moveToNext())
        }

        //   onDestroy()


        return list


    }

    //update the values in the indicated row
    fun updateGlucose(fecha: String, horaAnterior: String, hora: String, nivelesAnteriores: String, nivelesGlucosa: String, cicloMenstrual: String, inicioCiclo: String, finCiclo: String){

        val db = this.writableDatabase

        // nuevo valor para una columna
        val values = ContentValues().apply {
            put("hora_registro", hora)
            put("niveles_glucosa", nivelesGlucosa.toInt())
            put("ciclo_menstrual", cicloMenstrual)
            put("inicio_ciclo", inicioCiclo)
            put("fin_ciclo", finCiclo)
        }

        //que fila actualizar WHERE x LIKE ?
        val selection = "${Glucosa.COLUMN_NIVELES_GLUCOSA} LIKE ? AND ${Glucosa.COLUMN_FECHA} LIKE ? AND ${Glucosa.COLUMN_HORA_REGISTRO} LIKE ?"
        val selectionArgs = arrayOf(nivelesAnteriores, fecha, horaAnterior)
        db.update(
            Glucosa.TABLE,
            values,
            selection,
            selectionArgs)

        //    onDestroy()

    }

    //delete the indicated row in the database
    fun deleteGlucose(fecha: String, hora: String, nivelesGlucosa: String) {

        val db = this.writableDatabase
        // Define 'where' part of query.
        val selection = "${Glucosa.COLUMN_NIVELES_GLUCOSA} LIKE ? AND ${Glucosa.COLUMN_FECHA} LIKE ? AND ${Glucosa.COLUMN_HORA_REGISTRO} LIKE ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(nivelesGlucosa, fecha, hora)
        // Issue SQL statement.
        db.delete(Glucosa.TABLE, selection, selectionArgs)

        //    onDestroy()
    }


    //read all the data from the all tables in the database
    fun readAllDataBase(): ArrayList<DatosRegistrados> {

        val devolver: ArrayList<DatosRegistrados> = ArrayList()
        val prueba: ArrayList<Date> = ArrayList()

        //lists with the data
        val listInsulin = readAllInsulinTable()
        val listaSport = readAllSportTable()
        val listFeeding = readAllFeedingTable()
        val listGlucose = readAllGlucoseTable()


        for(i in 0 until listInsulin.size){
            val datos = listInsulin[i]
            devolver.add(datos)
        }

        for(i in 0 until listaSport.size){
            val datos = listaSport[i]
            devolver.add(datos)
        }

        for(i in 0 until listFeeding.size){
            val datos = listFeeding[i]
            devolver.add(datos)
        }

        for(i in 0 until listGlucose.size){
            val datos = listGlucose[i]
            devolver.add(datos)
        }

        devolver.sortByDescending { it.hora }

        //     onDestroy()

        return devolver

    }

    //read all data in the feeding table
    fun readAllFeedingTable(): ArrayList<DatosRegistrados> {

        val list: ArrayList<DatosRegistrados> = ArrayList()
        var datos = DatosRegistrados()

        val db = this.readableDatabase
        val query = "SELECT * FROM ${Alimentacion.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val id = result.getInt(result.getColumnIndex("_id"))
                val fecha = result.getString(result.getColumnIndex("fecha"))
                val horaRegistro = result.getString(result.getColumnIndex("hora_registro"))
                val alimento = result.getString(result.getColumnIndex("alimento"))
                val raciones = result.getInt(result.getColumnIndex("raciones"))
                val foto = result.getBlob(result.getColumnIndex("foto"))

                if(foto.size == 0){
                    datos = DatosRegistrados(id, "Alimentacion", alimento, raciones.toString() + " raciones", fecha, horaRegistro, R.drawable.un_pan)
                } else {
                    val arrayInputStream = ByteArrayInputStream(foto)
                    val bitmap = BitmapFactory.decodeStream(arrayInputStream)
                    datos = DatosRegistrados(id, "Alimentacion", alimento, raciones.toString() + " raciones", fecha, horaRegistro, bitmap)
                }

                if(fecha == fechaRegistro){
                    list.add(datos)
                }

            }
            while (result.moveToNext())
        }

        return list

    }

    //read all data in the insulin table
    fun readAllInsulinTable(): ArrayList<DatosRegistrados> {

        val list: ArrayList<DatosRegistrados> = ArrayList()
        var datos = DatosRegistrados()

        val db = this.readableDatabase
        val query = "SELECT * FROM ${Insulina.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val id = result.getInt(result.getColumnIndex("_id"))
                val fecha = result.getString(result.getColumnIndex("fecha"))
                val horaRegistro = result.getString(result.getColumnIndex("hora_registro"))
                val tipoInsulina = result.getString(result.getColumnIndex("tipo_insulina"))
                val unidades = result.getInt(result.getColumnIndex("unidades"))

                datos = DatosRegistrados(id, "Insulina", tipoInsulina, unidades.toString() + " uds", fecha, horaRegistro, R.drawable.inyeccion)

                if(fecha == fechaRegistro){
                    list.add(datos)

                }
            }
            while (result.moveToNext())
        }

        return list

    }

    //read all data in the sport table
    fun readAllSportTable(): ArrayList<DatosRegistrados> {

        val list: ArrayList<DatosRegistrados> = ArrayList()
        var datos = DatosRegistrados()

        val db = this.readableDatabase
        val query = "SELECT * FROM ${Deporte.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val id = result.getInt(result.getColumnIndex("_id"))
                val fecha = result.getString(result.getColumnIndex("fecha"))
                val tipo = result.getString(result.getColumnIndex("tipo"))
                val inicio = result.getString(result.getColumnIndex("inicio"))
                val fin = result.getString(result.getColumnIndex("fin"))

                //calculate the total duration
                val time1 = SimpleDateFormat("HH:mm").parse(inicio)
                val time2 = SimpleDateFormat("HH:mm").parse(fin)
                val difference = time2.time - time1.time
                val days = (difference / (1000*60*60*24))
                val hours = ((difference - (1000*60*60*24*days)) / (1000*60*60))
                val min = (difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60)
                var durationTotal: String
                if(hours.toString() == "0" && min.toString() != "0") {
                    durationTotal = "$min min"
                } else if(hours.toString() != "0" && min.toString() == "0"){
                    durationTotal = "$hours h"
                } else {
                    durationTotal = "$hours h $min min"
                }

                //duration in minutes
                var h = 0
                var m: Int

                if (hours.toString() !== "") {
                    h = hours.toInt() * 60
                }
                m = if (min.toString() == "") {
                    h
                } else {
                    min.toInt() + h
                }
                val durationMinutes = m.toString() + " min"

                datos = DatosRegistrados(id, "Actividad fisica", tipo, durationTotal, durationMinutes, fecha, inicio, R.drawable.ejercicio)

                if(fecha == fechaRegistro){
                    list.add(datos)
                }



            }
            while (result.moveToNext())
        }

        return list

    }

    //read all data in the glucose table
    fun readAllGlucoseTable(): ArrayList<DatosRegistrados> {

        val list: ArrayList<DatosRegistrados> = ArrayList()
        var datos = DatosRegistrados()

        val db = this.readableDatabase
        val query = "SELECT * FROM ${Glucosa.TABLE}"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()){
            do {

                val id = result.getInt(result.getColumnIndex("_id"))
                val fecha = result.getString(result.getColumnIndex("fecha"))
                val horaRegistro = result.getString(result.getColumnIndex("hora_registro"))
                val nivelesGlucosa = result.getInt(result.getColumnIndex("niveles_glucosa"))

                datos = DatosRegistrados(id, "Glucosa", "Glucosa", nivelesGlucosa.toString() + " mg/L", fecha, horaRegistro, R.drawable.medidor_de_glucosa)

                if(fecha == fechaRegistro){
                    list.add(datos)
                }
            }
            while (result.moveToNext())
        }

        return list

    }


}