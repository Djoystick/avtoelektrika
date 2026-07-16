package com.example.autoelectricai.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.autoelectricai.data.ai.GeminiApiService
import com.example.autoelectricai.data.ai.OpenAiApiService
import com.example.autoelectricai.data.db.AppDatabase
import com.example.autoelectricai.data.db.DiagnosisDao
import com.example.autoelectricai.data.db.DtcDao
import com.example.autoelectricai.data.db.OfflineCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "autoelectric.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val mockDtcs = listOf(
                        "('P0300', 'Powertrain', 'Обнаружены случайные/множественные пропуски зажигания', 'Random/Multiple Cylinder Misfire Detected', 'Система зажигания', 'critical', '[\"Свечи зажигания\", \"Катушки зажигания\", \"Топливные форсунки\"]', '[\"Замена свечей\", \"Проверка катушек\"]', '', '*', 1)",
                        "('P0171', 'Powertrain', 'Слишком бедная смесь (Bank 1)', 'System Too Lean (Bank 1)', 'Топливная система', 'warning', '[\"Подсос воздуха\", \"Грязный MAF-сенсор\", \"Слабое давление топлива\"]', '[\"Очистка MAF\", \"Поиск утечек вакуума\"]', '', '*', 1)",
                        "('P0420', 'Powertrain', 'Эффективность системы катализатора ниже пороговой (Bank 1)', 'Catalyst System Efficiency Below Threshold (Bank 1)', 'Выхлопная система', 'warning', '[\"Неисправный катализатор\", \"Датчик кислорода\"]', '[\"Замена катализатора\", \"Замена лямбда-зонда\"]', '', '*', 1)",
                        "('U0100', 'Network', 'Потеря связи с модулем управления двигателем (ECM/PCM)', 'Lost Communication With ECM/PCM', 'CAN шина', 'critical', '[\"Обрыв проводки\", \"Окисление контактов\", \"Сгорел предохранитель\"]', '[\"Проверка разъемов ECM\", \"Проверка питания\"]', '', '*', 1)",
                        "('C0040', 'Chassis', 'Неисправность цепи датчика скорости правого переднего колеса', 'Right Front Wheel Speed Sensor Circuit', 'Тормозная система', 'critical', '[\"Датчик ABS\", \"Ступичный подшипник\", \"Проводка\"]', '[\"Замена датчика ABS\", \"Восстановление проводки\"]', '', '*', 1)",
                        "('B0020', 'Body', 'Неисправность цепи подушки безопасности пассажира', 'Passenger Airbag Circuit Malfunction', 'Система безопасности (SRS)', 'critical', '[\"Разъем подушки\", \"Шлейф руля\", \"Блок SRS\"]', '[\"Проверка разъема под сиденьем\", \"Замена подушки\"]', '', '*', 1)"
                    )
                    mockDtcs.forEach {
                        db.execSQL("INSERT INTO dtc_catalog (code, category, description_ru, description_en, system, severity, common_causes, common_fixes, related_codes, affected_brands, is_generic) VALUES $it")
                    }
                }
            })
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7)
            .build()

    @Provides
    fun provideDiagnosisDao(db: AppDatabase): DiagnosisDao = db.diagnosisDao()

    @Provides
    fun provideDtcDao(db: AppDatabase): DtcDao = db.dtcDao()

    @Provides
    fun provideOfflineCacheDao(db: AppDatabase): OfflineCacheDao = db.offlineCacheDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("openai")
    fun provideOpenAiRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.dellmar.xyz/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("pollinations")
    fun providePollinationsRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://text.pollinations.ai/openai/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideGeminiService(@Named("gemini") retrofit: Retrofit): GeminiApiService =
        retrofit.create(GeminiApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenAiService(@Named("openai") retrofit: Retrofit): OpenAiApiService =
        retrofit.create(OpenAiApiService::class.java)

    @Provides
    @Singleton
    @Named("pollinations")
    fun providePollinationsService(@Named("pollinations") retrofit: Retrofit): OpenAiApiService =
        retrofit.create(OpenAiApiService::class.java)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage
}
