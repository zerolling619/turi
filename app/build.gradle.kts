    plugins {
    alias(libs.plugins.android.application)
        id("com.google.gms.google-services")
}

android {
    namespace = "robin.pe.turistea"
    compileSdk = 36

    defaultConfig {
        applicationId = "robin.pe.turistea"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
<<<<<<< HEAD
    // Google Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    
=======
>>>>>>> 4b04106b6799400d36a5b8aa209a5db1f51e27e9
    // Facebook Login
    implementation("com.facebook.android:facebook-login:16.1.3")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
}