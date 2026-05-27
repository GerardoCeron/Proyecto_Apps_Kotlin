package com.doctranslate.di

import android.content.Context
import com.doctranslate.data.remote.AndroidDownloader
import com.doctranslate.data.remote.Downloader
import com.doctranslate.data.remote.OfflineTranslationManager
import com.doctranslate.data.remote.api.ElevenLabsApi
import com.doctranslate.data.remote.api.MyMemoryApi
import com.doctranslate.data.remote.api.TranslationApi
import com.doctranslate.data.repository.AudioRepositoryImpl
import com.doctranslate.data.repository.TranslationRepositoryImpl
import com.doctranslate.domain.repository.AudioRepository
import com.doctranslate.domain.repository.TranslationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideDownloader(@ApplicationContext context: Context): Downloader {
        return AndroidDownloader(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "DocTranslate-Android/1.0")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(userAgentInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideTranslationApi(okHttpClient: OkHttpClient): TranslationApi {
        return Retrofit.Builder()
            .baseUrl("https://dummy.url/") // Base URL is required but overridden by @Url
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideElevenLabsApi(okHttpClient: OkHttpClient): ElevenLabsApi {
        return Retrofit.Builder()
            .baseUrl("https://api.elevenlabs.io/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ElevenLabsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMyMemoryApi(okHttpClient: OkHttpClient): MyMemoryApi {
        return Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyMemoryApi::class.java)
    }

    @Provides
    fun provideTranslationRepository(
        api: TranslationApi,
        myMemoryApi: MyMemoryApi,
        offlineManager: OfflineTranslationManager
    ): TranslationRepository {
        return TranslationRepositoryImpl(api, myMemoryApi, offlineManager)
    }

    @Provides
    fun provideAudioRepository(
        api: ElevenLabsApi
    ): AudioRepository {
        return AudioRepositoryImpl(api)
    }
}