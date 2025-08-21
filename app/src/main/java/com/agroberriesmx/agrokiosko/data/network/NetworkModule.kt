package com.agroberriesmx.agrokiosko.data.network

import com.agroberriesmx.agrokiosko.BuildConfig.BASE_URL
import com.agroberriesmx.agrokiosko.data.RepositoryImpl
import com.agroberriesmx.agrokiosko.data.core.interceptors.AuthInterceptor
import com.agroberriesmx.agrokiosko.domain.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient
            .Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    fun provideAgrokioskoApiService(retrofit: Retrofit): AgrokioskoApiService {
        return retrofit.create(AgrokioskoApiService::class.java)
    }

    @Provides
    fun provideAgrokioskoRepository(apiService: AgrokioskoApiService):Repository{
        return RepositoryImpl(apiService)
    }
}