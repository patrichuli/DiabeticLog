package com.unileon.diabeticlog.modelo

import android.content.ContentValues
import android.os.AsyncTask
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.modelo.ConexionSQLHelper.Companion.fechaRegistro
import com.unileon.diabeticlog.vista.UserProfile
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


class MetodosServidor :  AsyncTask<String, Void, String>() {

    private var client = OkHttpClient()

    override fun doInBackground(vararg params: String?): String? {
        val builder = Request.Builder()
        builder.url(params[0])
        val request = builder.build()
        try {
            val response = client.newCall(request).execute()
            return response.body()!!.string()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //get the data
    @Throws(IOException::class)
    fun getRequestMeasures(view: View) {

        val mostrarAltura: TextView = view.findViewById(R.id.muestraAltura)
        val mostrarPeso: TextView = view.findViewById(R.id.muestraPeso)

        val client = OkHttpClient()

        val request: Request = Request.Builder()
                .url("http://${getIP()}:8080/medidas")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }


            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                try {
                    val json = JSONArray(myResponse)
                    if(json.length() != 0){
                        mostrarAltura.setText("${json.getJSONObject(0).getInt("altura")}".trimIndent())
                        mostrarPeso.setText("${json.getJSONObject(0).getDouble("peso")}".trimIndent())

                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })




    }

    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestMeasures(postUrl: String, altura: Int, peso: Double) {

        val postdata = JSONObject()
        try {
            postdata.put("altura", altura)
            postdata.put("peso", peso)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
                .url(postUrl)
                .post(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestMeasures(postUrl: String, altura: Int, peso: Double) {

        val putdata = JSONObject()
        try {
            putdata.put("id", 1)
            putdata.put("altura", altura)
            putdata.put("peso", peso)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
                .url(postUrl)
                .put(body)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    //get the data
    @Throws(IOException::class)
    fun getRequestWeather(view: View) {

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(postUrlWeather)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                try {
                    val json = JSONArray(myResponse)
                    if(json.length() != 0){
                        for (i in 0 until UserProfile.itemsWeather.size) { // Iterate elements of the first ArrayList
                            for (j in 0 until json.length()) { // Iterate elements of the second ArrayList
                                if (UserProfile.itemsWeather[i].equals("")) { // Compare if the values are equal.
                                    UserProfile.marcados[i] = true
                                }
                            }
                        }
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })


    }

    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestWeather(postUrl: String, list: MutableList<String>) {

        val json = JSONObject()
        json.put("opciones", JSONArray(list))
        val arrayList = json.toString()

        val postdata = JSONObject()
        try {
            postdata.put("opciones", arrayList)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestWeather(postUrl: String, list: MutableList<String>) {

        val json = JSONObject()
        json.put("opciones", JSONArray(list))
        val arrayList = json.toString()

        val putdata = JSONObject()
        try {
            putdata.put("id", 1)
            putdata.put("opciones", arrayList)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .put(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    //get the data
    @Throws(IOException::class)
    fun getRequestEmotion(view: View) {

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(postUrlEmotionalState)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                try {
                    val json = JSONArray(myResponse)
                    if(json.length() != 0){

                        //    mostrarAltura.setText("${json.getJSONObject(0).getInt("altura")}".trimIndent())
                        //     mostrarPeso.setText("${json.getJSONObject(0).getDouble("peso")}".trimIndent())
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })


    }

    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestEmotion(postUrl: String, emocion: String, intensidad: String, motivo: String) {

        val postdata = JSONObject()
        try {
            postdata.put("emocion", emocion)
            postdata.put("intensidad", intensidad)
            postdata.put("motivo", motivo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestEmotion(postUrl: String, emocion: String, intensidad: String, motivo: String) {

        val putdata = JSONObject()
        try {
            putdata.put("id", 1)
            putdata.put("emocion", emocion)
            putdata.put("intensidad", intensidad)
            putdata.put("motivo", motivo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .put(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    //get the data
    @Throws(IOException::class)
    fun getRequestInsulin(view: View) {

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(postUrlInsulin)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                try {
                    val json = JSONArray(myResponse)
                    if(json.length() != 0){

                        //    mostrarAltura.setText("${json.getJSONObject(0).getInt("altura")}".trimIndent())
                        //     mostrarPeso.setText("${json.getJSONObject(0).getDouble("peso")}".trimIndent())
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })


    }

    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestInsulin(postUrl: String, hora: String, tipoInsulina: String, unidades: String, tipoAplicacion: String) {

        val postdata = JSONObject()
        try {
            postdata.put("fecha", fechaRegistro.toString())
            postdata.put("hora_registro", hora)
            postdata.put("tipo_insulina", tipoInsulina)
            postdata.put("unidades", unidades.toInt())
            postdata.put("tipo_aplicacion", tipoAplicacion)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestInsulin(postUrl: String, id: Int, hora: String, tipoInsulina: String, unidades: String, tipoAplicacion: String) {

        val putdata = JSONObject()
        try {
            putdata.put("id", id)
            putdata.put("hora_registro", hora)
            putdata.put("tipo_insulina", tipoInsulina)
            putdata.put("unidades", unidades.toInt())
            putdata.put("tipo_aplicacion", tipoAplicacion)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .put(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // delete the data
    @Throws(IOException::class)
    fun deleteRequestInsulin(postUrl: String, id: Int) {

        val deleteUrl = postUrl + id

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }


    //get the data
    @Throws(IOException::class)
    fun getRequestSport(view: View) {

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(postUrlSport)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                try {
                    val json = JSONArray(myResponse)
                    if(json.length() != 0){
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })


    }

    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestSport(postUrl: String, tipo: String, inicio: String, fin: String, intensidad: String, calorias: String) {

        val postdata = JSONObject()
        try {
            postdata.put("fecha", fechaRegistro.toString())
            postdata.put("tipo", tipo)
            postdata.put("inicio", inicio)
            postdata.put("fin", fin)
            postdata.put("intensidad", intensidad.toInt())
            postdata.put("calorias", calorias.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestSport(postUrl: String, id: Int, tipo: String, inicio: String, fin: String, intensidad: String, calorias: String) {

        val putdata = JSONObject()
        try {
            putdata.put("id", id)
            putdata.put("tipo", tipo)
            putdata.put("inicio", inicio)
            putdata.put("fin", fin)
            putdata.put("intensidad", intensidad.toInt())
            putdata.put("calorias", calorias.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .put(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // delete the data
    @Throws(IOException::class)
    fun deleteRequestSport(postUrl: String, id: Int) {

        val deleteUrl = postUrl + id

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }


    //get the data
    @Throws(IOException::class)
    fun getRequestGlucose(view: View) {

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(postUrlGlucose)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                try {
                    val json = JSONArray(myResponse)
                    if(json.length() != 0){

                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })


    }

    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestGlucose(postUrl: String, hora: String, nivelesGlucosa: String, cicloMenstrual: String, inicioCiclo: String, finCiclo: String) {

        val postdata = JSONObject()
        try {
            postdata.put("fecha", fechaRegistro.toString())
            postdata.put("hora_registro", hora)
            postdata.put("niveles_glucosa", nivelesGlucosa.toInt())
            postdata.put("ciclo_menstrual", cicloMenstrual)
            postdata.put("inicio_ciclo", inicioCiclo)
            postdata.put("fin_ciclo", finCiclo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestGlucose(postUrl: String, id: Int, hora: String, nivelesGlucosa: String, cicloMenstrual: String, inicioCiclo: String, finCiclo: String) {

        val putdata = JSONObject()
        try {
            putdata.put("id", id)
            putdata.put("hora_registro", hora)
            putdata.put("niveles_glucosa", nivelesGlucosa.toInt())
            putdata.put("ciclo_menstrual", cicloMenstrual)
            putdata.put("inicio_ciclo", inicioCiclo)
            putdata.put("fin_ciclo", finCiclo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .put(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // delete the data
    @Throws(IOException::class)
    fun deleteRequestGlucose(postUrl: String, id: Int) {

        val deleteUrl = postUrl + id

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    //get the data
    @Throws(IOException::class)
    fun getRequestFeeding(view: View) {

        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(postUrlFeeding)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                try {
                    val json = JSONArray(myResponse)
                    if(json.length() != 0){

                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })


    }

    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestFeeding(postUrl: String, hora: String, alimento: String, foto: ByteArray, raciones: String) {

        val postdata = JSONObject()
        try {
            postdata.put("fecha", fechaRegistro.toString())
            postdata.put("hora_registro", hora)
            postdata.put("alimento", alimento)
            postdata.put("foto", Base64.encodeToString(foto, Base64.NO_WRAP))
            postdata.put("raciones", raciones.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestFeeding(postUrl: String, id: Int, hora: String, alimento: String, foto: ByteArray, raciones: String) {

        val putdata = JSONObject()
        try {
            putdata.put("id", id)
            putdata.put("hora_registro", hora)
            putdata.put("alimento", alimento)
            putdata.put("foto", Base64.encodeToString(foto, Base64.NO_WRAP))
            putdata.put("raciones", raciones.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .put(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // delete the data
    @Throws(IOException::class)
    fun deleteRequestFeeding(postUrl: String, id: Int) {

        val deleteUrl = postUrl + id

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }


    // send data to a server to create a resource.
    @Throws(IOException::class)
    fun postRequestSteps(postUrl: String, steps: Int) {

        val postdata = JSONObject()
        try {
            postdata.put("fecha", fechaRegistro.toString())
            postdata.put("pasosTotales", steps)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, postdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    // send data to a server to update a resource.
    @Throws(IOException::class)
    fun putRequestSteps(postUrl: String, steps: Int) {

        val putdata = JSONObject()
        try {
            putdata.put("id", 1)
            putdata.put("pasosTotales", steps)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val client = OkHttpClient()
        val body: RequestBody = RequestBody.create(JSON, putdata.toString())
        val request: Request = Request.Builder()
            .url(postUrl)
            .put(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d("TAG", response.body().toString())
            }

        })
    }

    fun getIP(): String {

        var addrs: List<InetAddress>
        var ip = ""
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                addrs = Collections.list(intf.getInetAddresses())
                for (addr in addrs) {

                    if (addr is Inet4Address) {
                        ip = addr.hostAddress.toUpperCase(Locale("es", "MX"))
                        System.out.println("DIRECCIONES O ALGO NO LO SE: " + addr)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(ContentValues.TAG, "Ex getting IP value " + e.message)
        }
        System.out.println("LA FINAL: " + ip)
        return ip

    }

    companion object {
        //to be able to access the server
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val postUrlSteps: String = "http://192.168.1.135:8080/pasos"
        val postUrlMeasures: String = "http://192.168.1.135:8080/medidas"
        val postUrlWeather: String = "http://192.168.1.135:8080/tiempo"
        val postUrlEmotionalState: String = "http://192.168.1.135:8080/emocion"
        val postUrlInsulin: String = "http://192.168.1.135:8080/insulina"
        val postUrlGlucose: String = "http://192.168.1.135:8080/glucosa"
        val postUrlFeeding: String = "http://192.168.1.135:8080/alimentacion"
        val postUrlSport: String = "http://192.168.1.135:8080/deporte"
        val deleteUrlInsulin: String = "http://192.168.1.135:8080/insulina?id="
        val deleteUrlGlucose: String = "http://192.168.1.135:8080/glucosa?id="
        val deleteUrlFeeding: String = "http://192.168.1.135:8080/alimentacion?id="
        val deleteUrlSport: String = "http://192.168.1.135:8080/deporte?id="
    }


}