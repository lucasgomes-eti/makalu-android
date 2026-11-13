import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "eti.lucasgomes.makalu.features.orders"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                test.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
            }
        }
    }
}

dependencies {
    // Project
    implementation(projects.shared)
    implementation(projects.components)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    // Realtime: MQTT 3.1.1 over secure WebSocket. The websocket platform adds the
    // optional Netty HTTP codec; without it the client hangs on connect.
    implementation(libs.hivemq.mqtt.client)
    implementation(platform(libs.hivemq.mqtt.client.websocket))

    // Image Loading
    implementation(libs.bundles.coil)

    // DI
    implementation(libs.koin.androidx.compose)

    // Tooling
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Test
    testImplementation(projects.testing)
    testImplementation(libs.bundles.android.testing)
}
