package com.example.ruyayorum


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// API istekleri için kullanılacak arayüz
interface DreamAnalysisApi {
    @POST("v1/chat/completions") // OpenAI API endpoint
    fun analyzeDream(@Body requestBody: RequestBody): Call<ResponseBody>
}

// API isteği için kullanılacak sınıflar
data class RequestBody(val messages: List<Message>)
data class Message(val role: String, val content: String)
data class Choice(val index: Int, val message: Message, val finish_reason: String)
data class ResponseBody(val choices: List<Choice>) // choices alanını güncelledik
data class Usage(val prompt_tokens: Int, val completion_tokens: Int, val total_tokens: Int)

// MainActivity sınıfı
class MainActivity : AppCompatActivity() {

    private lateinit var editTextDreamInput: EditText
    private lateinit var buttonAnalyze: Button
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Görsel bileşenleri tanımlama
        editTextDreamInput = findViewById(R.id.editTextDreamInput)
        buttonAnalyze = findViewById(R.id.buttonAnalyze)
        textViewResult = findViewById(R.id.textViewResult)

        // Butona tıklandığında rüyayı analiz et
        buttonAnalyze.setOnClickListener {
            val dreamText = editTextDreamInput.text.toString()

            if (dreamText.isNotEmpty()) {
                analyzeDreamWithAI(dreamText)
            } else {
                textViewResult.text = "Lütfen bir rüya metni giriniz."
            }
        }
    }

    private fun analyzeDreamWithAI(dream: String) {
        val message = Message(role = "user", content = dream)
        val requestBody = RequestBody(messages = listOf(message)) // Rüya metnini içeren istek gövdesi

        // API anahtarınızı buraya ekleyin
        val apiKey = "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

        // Retrofit istemcisi oluşturma
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $apiKey") // API anahtarınızı kullanın
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            }
            .build()

        val api = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DreamAnalysisApi::class.java)

        // API'ye istek yapma
        api.analyzeDream(requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // İlk choice'un message content'ini al ve sonucu güncelle
                    val resultText = response.body()?.choices?.get(0)?.message?.content ?: "Rüyanızda tanınan semboller bulunamadı."
                    textViewResult.text = resultText
                } else {
                    textViewResult.text = "Analiz başarısız oldu."
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                textViewResult.text = "Bir hata oluştu: ${t.message}"
            }
        })
    }
}
