package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiService {
    private const val MODEL_NAME = "gemini-3.5-flash"

    suspend fun getChatResponse(prompt: String, apiKey: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "مرحباً بك! للاستفادة الكاملة من خدمة المساعد الذكي WAM AI، يرجى إعداد مفتاح API Key الخاص بـ Gemini في إعدادات التطبيق أو عبر Secrets panel."
        }

        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            urlConnection.doOutput = true
            urlConnection.connectTimeout = 15000
            urlConnection.readTimeout = 15000

            // System instructions mapping core attributes
            val systemInstruction = JSONObject().put("parts", JSONArray().put(JSONObject().put("text", """
                أنت المساعد الذكي المتطور (WAM AI Assistant) المدمج في تطبيق "الماهر موني" (Al-Maher Money) - محفظة إلكترونية رائدة من الجيل القادم مصممة خصيصاً للسوق اليمني والخليجي.
                اسم المحفظة الرسمي: الماهر موني (Al-Maher Money)
                الاسم الاختصاري: WAM
                المالك والمشرف العام والمصمم الوحيد للتطبيق: الأستاذ ماهر أحمد الوتاري.
                رقم الهاتف للتواصل والدعم الفني: 777644670 (وهو رقم الدعاية أيضاً).
                استخدم طابعاً مهنياً، ودوداً، وسريع الاستجابة باللغة العربية اليمنية المحببة والخليجية الفصحى.
                أبرز مميزات التطبيق التي يجب أن تسوقها وتجيب على أساسها:
                1. التحويلات الفورية P2P برقم الهاتف أو QR Code أو معرف WAM.
                2. سحب وإيداع نقدي فوري عبر الوكلاء المرخصين وشبكة مصارف WAM المعتمدة والآمنة.
                3. دفع فواتير الخدمات فورا (الكهرباء، المياه، الهاتف الثابت، الإنترنت) وشحن الرصيد المباشر لكافة الشبكات المحلية (يمن موبايل، يو YOU، سبأفون، واي).
                4. تقنية SoftPOS الثورية لتحويل الهاتف لنقاط بيع واستقبال البطاقات الائتمانية عبر NFC.
                5. ميزة "الأوعية الادخارية المبتكرة" (Saving Pots) لادخار ذكي بفائدة صفرية متوافقة مع الشريعة الإسلامية.
                6. ميزة القروض المصغرة الإسلامية بفائدة صفرية 100% (قروض التمويل الأصغر الذكية) بدون ضمانات كلاسيكية معتمدة على تحليل السلوك والذكاء الاصطناعي للمالك الأستاذ ماهر أحمد الوتاري.
                7. كاش باك ذكي فوري 1% على المعاملات.
                يرجى عدم ذكر "شركة الصيفي للصرافة" أو "شركة الصيفي" نهائياً، بل قل "شبكة مصارف ووكلاء WAM المعتمدين والمصنفين أمنياً".
                أجب على أسئلة العميل دائماً باختصار وبطريقة منظمة وجذابة، وروج للخدمات ولا تتحدث عن التفاصيل البرمجية الداخلية إلا بطلب مباشر.
            """.trimIndent())))

            val contents = JSONArray().put(
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            )

            val payload = JSONObject()
                .put("contents", contents)
                .put("systemInstruction", systemInstruction)

            OutputStreamWriter(urlConnection.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = urlConnection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.getJSONArray("candidates")
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                parts.getJSONObject(0).getString("text")
            } else {
                val errorStreamText = urlConnection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                "عذراً، حدث خطأ أثناء الاتصال بمحرك الذكاء الاصطناعي (رمز الخطأ: $responseCode). يرجى التأكد من صلاحية رمز الـ API Key الخاص بك."
            }
        } catch (e: Exception) {
            "حدث خطأ في الاتصال بالذكاء الاصطناعي: ${e.localizedMessage}. يرجى التحقق من اتصال الإنترنت واستخدام مفتاح API صالح لك."
        }
    }
}
