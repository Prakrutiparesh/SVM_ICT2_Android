    package com.example.ict2_project.api

    import okhttp3.OkHttpClient
    import okhttp3.logging.HttpLoggingInterceptor
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory

    object RetrofitClient {
        private const val BASE_URL = "http://192.168.1.70:5175/"

        // Create logging interceptora
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY   // Shows headers, body, etc.
        }

        // Build OkHttp client with the interceptor
        private val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val instance: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)                         // Attach the client
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        fun getBaseUrl(): String = BASE_URL

    }