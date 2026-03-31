plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.agroberriesmx.agrokiosko"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.agroberriesmx.agrokiosko"
        minSdk = 21
        targetSdk = 35
        versionCode = 8 //Ultima version 20/10/2025
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            resValue("string", "AgroberriesMX", "Agrokiosko")
            buildConfigField("String", "BASE_URL", "\"http://54.165.41.23:5053/api/Agrokiosko/\"")
            //buildConfigField("String", "BASE_URL", "\"http://192.168.50.120:5011/api/Agrokiosko/\"")
        }

        getByName("debug") {
            isDebuggable = true
            resValue("string", "AgroberriesMX", "[DEBUG]Agrokiosko")
            buildConfigField("String", "BASE_URL", "\"http://54.165.41.23:5053/api/Agrokiosko/\"")
            //buildConfigField("String", "BASE_URL", "\"http://192.168.50.120:5011/api/Agrokiosko/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    val navVersion = "2.8.5"
    val daggerHiltVersion = "2.48"
    val retrofitVersion = "2.9.0"
    val okHttpVersion = "4.9.1"
    val zxingVersion = "4.3.0"
    val mailerVersion = "1.6.2"
    val coroutinesVersion = "1.7.3"

    //NavComponent
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    //DaggerHilt
    implementation("com.google.dagger:hilt-android:$daggerHiltVersion")
    kapt("com.google.dagger:hilt-compiler:$daggerHiltVersion")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    //SQLite
    //implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    //ZXing
    implementation("com.journeyapps:zxing-android-embedded:$zxingVersion")

    //Mailer
    implementation("com.sun.mail:android-mail:$mailerVersion")
    implementation("com.sun.mail:android-activation:$mailerVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")  // Para Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    //PDF
    //implementation ("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1" )
    //implementation ("com.github.barteksc:android-pdf-viewer:2.8.2")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}