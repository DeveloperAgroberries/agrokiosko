package com.agroberriesmx.agrokiosko.data.network

import android.content.Context
import com.agroberriesmx.agrokiosko.data.logger.LogUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LogUtilModule {

    @Provides
    @Singleton
    fun provideLogUtil(@ApplicationContext context: Context): LogUtil {
        return LogUtil(context)
    }
}