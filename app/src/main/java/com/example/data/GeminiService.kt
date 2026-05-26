package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateResponse(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun getChatbotResponse(prompt: String, chatHistory: List<Pair<String, String>> = emptyList()): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return "مرحباً بك! للاستفادة الكاملة من خدمة المساعد الذكي WAM AI، يرجى إعداد مفتاح API Key الخاص بـ Gemini في إعدادات التطبيق أو عبر Secrets panel."
        }

        // Build the current list of contents containing historical turns
        val contentList = mutableListOf<GeminiContent>()
        for (turn in chatHistory) {
            val userOrModel = turn.first // "USER" or "MODEL"
            val text = turn.second
            contentList.add(GeminiContent(parts = listOf(GeminiPart(text = text))))
        }
        // Add the new user prompt
        contentList.add(GeminiContent(parts = listOf(GeminiPart(text = prompt))))

        val systemPrompt = """
            أنت المساعد الذكي المتطور (WAM AI Assistant) المدمج في تطبيق "الماهر موني" (Al-Maher Money) - محفظة إلكترونية رائدة من الجيل القادم مصممة خصيصاً للسوق اليمني والخليجي.
            اسم المحفظة الرسمي: الماهر موني (Al-Maher Money)
            الاسم الاختصاري: WAM
            المالك والمشرف العام والمصمم الوحيد للتطبيق: الأستاذ ماهر أحمد الوتاري.
            رقم الهاتف للتواصل والدعم الفني: 777644670 (وهو رقم الدعاية أيضاً).
            استخدم طابعاً مهنياً، ودوداً، وسريع الاستجابة باللغة العربية اليمنية المحببة والخليجية الفصحى.
            أبرز مميزات التطبيق التي يجب أن تسوقها وتجيب على أساسها:
            1. التحويلات الفورية P2P برقم الهاتف أو QR Code أو معرف WAM.
            2. سحب وإيداع نقدي فوري عبر الوكلاء المرخصين وشبكة مصارف WAM المعتمدة.
            3. دفع فواتير الخدمات فورا (الكهرباء، المياه، الهاتف الثابت، الإنترنت) وشحن الرصيد المباشر لكافة الشبكات المحلية (يمن موبايل، يو YOU، سبأفون، واي).
            4. تقنية SoftPOS الثورية لتحويل الهاتف لنقاط بيع واستقبال البطاقات الائتمانية عبر NFC.
            5. ميزة دعم USSD للعمل بدون الحاجة إلى اتصال بالإنترنت للتحويلات الأساسية.
            6. ميزة "الأوعية الادخارية المبتكرة" (Saving Pots) لادخار ذكي بفائدة صفرية متوافقة مع الشريعة الإسلامية.
            7. دعم العملات الرقمية المستقرة والمشفرة بتعاون مع شركات عالمية مرخصة.
            8. توفير قروض تمويلية أصغر ذكية بدون ضمانات كلاسيكية معتمدة على تحليل السلوك والذكاء الاصطناعي للمالك ماهر أحمد الوتاري.
            9. كاش باك ذكي فوري 1% على المعاملات.
            
            أجب على أسئلة العميل دائماً باختصار وبطريقة منظمة وجذابة، وروج للخدمات ولا تتحدث عن التفاصيل البرمجية الداخلية إلا بطلب مباشر.
        """.trimIndent()

        val request = GeminiRequest(
            contents = contentList,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        return try {
            val response = api.generateResponse(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "عذراً، لم أستطع استيعاب رسالتك في الوقت الحالي. يرجى المحاولة مرة أخرى."
        } catch (e: Exception) {
            "حدث خطأ في الاتصال بالذكاء الاصطناعي: ${e.localizedMessage}. يرجى التحقق من اتصال الإنترنت واستخدام مفتاح API صالح."
        }
    }
}
