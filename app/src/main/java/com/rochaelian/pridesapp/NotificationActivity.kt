package com.rochaelian.pridesapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.rochaelian.pridesapp.databinding.ActivityNotificationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    // VARIABLES DE LAS SOLICITUDES A BCCR
    val compra = 317
    val venta = 318
    val nombre = "Negocio"
    val subniveles = "n"
    val correo = "jabarca@prides.net"
    val token = "D2DTREJA8A"
    var fechaInicio = "12/08/2023"

    // VARIABLES DE OPEN WEATHER
    val apiKey = "45accf76a3c1bcb40bdda8c34d8c5455"
    val lat = 9.937671
    val lon = -84.096974

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val usuario = intent.getStringExtra("Usuario")
        binding.tvUsuario.setText(usuario)

        // Al hacer consultas a WS se utilizan corrutinas para agilizar el proceso de carga principal de la pantalla
        CoroutineScope(Dispatchers.IO).launch {
            solicitarIndicadores(compra)
            solicitarIndicadores(venta)
            ReadWeather()
        }

        binding.btnNotificar.setOnClickListener{
            Log.d("PRUEBANOMBRE", "USUARIO: $usuario")
            notoficar(usuario!!)
        }
    }


    // Crea la solicitud para enviar al WS del BCCR segun el indicador deseado
    private fun solicitarIndicadores(indicador:Int){

        // Armar el XML de la solicitud
        val soapRequestBody = """<?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
            <soap:Body>
            <ObtenerIndicadoresEconomicos xmlns="http://ws.sdde.bccr.fi.cr">
                <Indicador>${indicador}</Indicador>
                <FechaInicio>${fechaInicio}</FechaInicio>
                <FechaFinal>${fechaInicio}</FechaFinal>
                <Nombre>${nombre}</Nombre>
                <SubNiveles>${subniveles}</SubNiveles>
                <CorreoElectronico>${correo}</CorreoElectronico>
                <Token>${token}</Token>
            </ObtenerIndicadoresEconomicos>
            </soap:Body>
            </soap:Envelope>
        """
        val serviceUrl = "https://gee.bccr.fi.cr/Indicadores/Suscripciones/WS/wsindicadoreseconomicos.asmx"

        if (indicador == 317){
            SoapRequestTaskCompra().execute(serviceUrl, soapRequestBody)
        }
        if (indicador == 318){
            SoapRequestTaskVenta().execute(serviceUrl, soapRequestBody)
        }
    }


    // Manejan la solicitud al WS del BCCR
    private inner class SoapRequestTaskCompra() : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            val serviceUrl = params[0]
            val soapRequestBody = params[1]

            val url = URL(serviceUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8")
            connection.setRequestProperty("SOAPAction", "http://ws.sdde.bccr.fi.cr/ObtenerIndicadoresEconomicos")
            connection.doOutput = true

            try {
                val outputStream: OutputStream = connection.outputStream
                outputStream.write(soapRequestBody.toByteArray())
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    var resulFromXml = extractValueFromXml(response,"NUM_VALOR")

                    return resulFromXml.toString()
                } else {
                    Log.e("SOAPRequest", "Response Code: $responseCode")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                connection.disconnect()
            }

            return ""
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            // Una vez hecha la solicitud y recibida la respuessta, se setea el valor en la pantalla
            binding.tvCompra.setText(result.substring(0,6))
        }
    }

    private inner class SoapRequestTaskVenta() : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            val serviceUrl = params[0]
            val soapRequestBody = params[1]

            val url = URL(serviceUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8")
            connection.setRequestProperty("SOAPAction", "http://ws.sdde.bccr.fi.cr/ObtenerIndicadoresEconomicos")
            connection.doOutput = true

            try {
                val outputStream: OutputStream = connection.outputStream
                outputStream.write(soapRequestBody.toByteArray())
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    var resulFromXml = extractValueFromXml(response,"NUM_VALOR")

                    return resulFromXml.toString()
                } else {
                    Log.e("SOAPRequest", "Response Code: $responseCode")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                connection.disconnect()
            }

            return ""
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            // Una vez hecha la solicitud y recibida la respuessta, se setea el valor en la pantalla
            binding.tvVenta.setText(result.substring(0,6))
        }
    }



    // Extrae los valores del XML de despuesta del WS del BCCR
    private fun extractValueFromXml(xml: StringBuilder, value:String): String? {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml.toString()))

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == value) {
                        parser.next()
                        return parser.text
                    }
                }
            }
            eventType = parser.next()
        }
        return null
    }


    // Llama al WS de los datos del estado del tiempo
    private fun ReadWeather() {

        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${apiKey}"
        val postResquest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {

                    val gson = Gson()
                    val weatherResponse = gson.fromJson(response, WeatherResponse::class.java)

                    // Setar los parámetros de respuesta en los campos correspondientes de la pantalla
                    setParamsWeather(weatherResponse.main.temp, weatherResponse.sys.country, weatherResponse.sys.sunrise, weatherResponse.sys.sunset, weatherResponse.weather[0].description)
                    setIconWeather(weatherResponse.weather[0].icon)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        ) { error -> Log.e("Error", error.message!!) }
        Volley.newRequestQueue(this).add(postResquest)
    }

    // Setar los parámetros de respuesta en los campos correspondientes de la pantalla
    private fun setParamsWeather(temp:Double, region:String, sunrise:Long, sunset:Long, description:String){

        val tempCelcius = (temp - 273.15).toString()

        val sunriseDate = Date(sunrise * 1000)
        val sunsetDate = Date(sunset * 1000)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val formattedSunrise = dateFormat.format(sunriseDate)
        val formattedSunset = dateFormat.format(sunsetDate)

        binding.tvTemp.setText(tempCelcius.substring(0,2))
        binding.tvWeatherDescription.setText(description)
        binding.tvRegion.setText(region)
        binding.tvSunrise.setText(formattedSunrise)
        binding.tvSunset.setText(formattedSunset)
    }

    private fun setIconWeather(iconCode:String){

        if(iconCode == "01d" || iconCode == "01n"){
            binding.ivWeather.setImageResource(R.drawable.ic_sun_black)
        }

        if(iconCode == "02d" || iconCode == "02n"){
            binding.ivWeather.setImageResource(R.drawable.ic_cloud_black)
        }

        if(iconCode == "03d" || iconCode == "03n"){
            binding.ivWeather.setImageResource(R.drawable.ic_cloud_outline)
        }

        if(iconCode == "04d" || iconCode == "04n"){
            binding.ivWeather.setImageResource(R.drawable.ic_cloud_black)
        }

        if(iconCode == "09d" || iconCode == "09n" || iconCode == "10d" || iconCode == "10d"){
            binding.ivWeather.setImageResource(R.drawable.ic_water_black)
        }

        if(iconCode == "11d" || iconCode == "11n"){
            binding.ivWeather.setImageResource(R.drawable.ic_storm_black)
        }

        if(iconCode == "13d" || iconCode == "13n"){
            binding.ivWeather.setImageResource(R.drawable.ic_snow_black)
        }
    }




    // Crear la notificación
    private fun notoficar(usuario:String){

        var ventaDolar = binding.tvVenta.text
        var temperatura = binding.tvTemp.text

        showNotification(this, "¡Hola $usuario!", "La venta  del dólar para el día de hoy es de $ventaDolar y la temperatura actual es de $temperatura grados Celcius")
    }

    // Mostrar la notificación
    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "my_channel_id"
        val channelName = "My Channel Name"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_black)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationId = /* Assign a unique notification ID */
            notificationManager.notify(101, notificationBuilder.build())
    }
}