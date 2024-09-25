package ufc.smd.restclient

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PaisActivity : ComponentActivity() {
    lateinit var tvInfos: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pais)

        tvInfos = findViewById(R.id.tvInfos)

    }

    fun onPaisClick(v: View) = runBlocking{

        lifecycleScope.launch (Dispatchers.IO){
            val retorno = mLoad("https://ipapi.co/json")
            if (retorno != null){
                val jsonResponse = JSONObject(retorno.readText())
                Log.v("PDM", "Olha o ip: " + jsonResponse.getString("country_name").toString())

                val retorno2 = mLoad("https://restcountries.com/v3.1/name/" + jsonResponse.getString("country_name"))
                if (retorno2 != null){
                    val jsonResponse2 = JSONArray(retorno2.readText())
                    Log.v("PDM", "Olha as infos do país do IP: " + jsonResponse2.toString())

                    var mensagem:String = ""

                    //mensagem = jsonResponse2.getJSONObject(0).getJSONObject("nativeName").getJSONObject("por").getString("official")
                    mensagem = jsonResponse2.getJSONObject(0).getJSONObject("name").getJSONObject("nativeName").getJSONObject("por").getString("official").toString() + '\n'
                    mensagem += "Seu IP: " + jsonResponse.getString("ip")
                    Log.v("PDM", "mensagem " + mensagem)

                    withContext(Dispatchers.Main) {
                        tvInfos.text = mensagem
                    }

                }

            }

        }

    }



    suspend fun mLoad(string: String): BufferedReader? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpsURLConnection?
        try {
            connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpsURLConnection
            connection.requestMethod= "GET"
            connection.connectTimeout= 20000
            // Fingir que é um browser pedindo
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            withContext(Dispatchers.IO) {
                connection.connect()
            }

            Log.v("PDM", "Response Code: "+connection.responseCode)
            Log.v("PDM", "Response: "+connection.responseMessage)


            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return bufferedInputStream.bufferedReader(Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.v("PDM", "Erro de comunicação: "+e.message)


        }
        return null
    }

    // Function to convert string to URL
    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Log.v("PDM", "Erro de formatação da URL: "+e.message)
        }
        return null
    }
}


