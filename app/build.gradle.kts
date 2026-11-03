import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Store keys live in local.properties (not committed) or gradle.properties / -P flags.
// See the "Billing and ads configuration" section of the PR / README for each value.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(name: String): String =
    (localProps.getProperty(name) ?: providers.gradleProperty(name).orNull ?: "").trim()
fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.barati.zehnsaz"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.barati.zehnsaz"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "TAPSELL_APP_KEY", quoted(secret("TAPSELL_APP_KEY")))
        buildConfigField("String", "TAPSELL_REWARDED_ZONE_ID", quoted(secret("TAPSELL_REWARDED_ZONE_ID")))
        buildConfigField("String", "BAZAAR_RSA_PUBLIC_KEY", quoted(secret("BAZAAR_RSA_PUBLIC_KEY")))
        buildConfigField("String", "BAZAAR_SUB_MONTHLY", quoted(secret("BAZAAR_SUB_MONTHLY")))
        buildConfigField("String", "BAZAAR_SUB_QUARTERLY", quoted(secret("BAZAAR_SUB_QUARTERLY")))
        buildConfigField("String", "BAZAAR_SUB_YEARLY", quoted(secret("BAZAAR_SUB_YEARLY")))
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
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.maxHeapSize = "2g"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Tapsell Plus SDK (rewarded video ads)
    implementation(libs.tapsell.plus)

    // Cafe Bazaar in-app billing (subscriptions)
    implementation(libs.poolakey)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}