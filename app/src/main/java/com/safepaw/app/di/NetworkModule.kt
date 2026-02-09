package com.safepaw.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://eiicceqdkngrynzvohyo.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpaWNjZXFka25ncnluenZvaHlvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA2MjkxODEsImV4cCI6MjA4NjIwNTE4MX0.7kNwLkGo9M_W40Wnc6FdSjp-KShDbE7OehaxTKKgb38"
        ) {
            install(Postgrest)
            install(GoTrue)
            install(Storage)
        }
    }
}
