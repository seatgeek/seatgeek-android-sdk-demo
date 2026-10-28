plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.seatgeek.android.sdk.sample"
    // SeatGeek SDK 5.3.0 publishes AARs with minCompileSdkVersion = 37.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.seatgeek.android.sdk.sample"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The SDK runs OIDC through AppAuth, which it bundles; AppAuth's RedirectUriReceiverActivity
        // catches the browser redirect for this scheme. Must match the scheme of the redirect URI set
        // in SampleApplication.
        manifestPlaceholders["appAuthRedirectScheme"] = "yourAppScheme"
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

kotlin {
    // The android { kotlinOptions {} } block is a compile error under AGP 9's Kotlin DSL;
    // configure the Kotlin compiler through the kotlin {} extension instead.
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {

    implementation(libs.seatgeek.sdk) {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    coreLibraryDesugaring(libs.android.tools.desugar)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}