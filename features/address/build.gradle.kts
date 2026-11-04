import com.android.build.api.variant.BuildConfigField
import java.io.StringReader
import java.util.Properties
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
    namespace = "eti.lucasgomes.makalu.features.adress"
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val mapsApiKey: Provider<String> = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .orElse("")
    .map { text ->
        Properties()
            .apply { StringReader(text).use { load(it) } }
            .getProperty("MAPS_API_KEY")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw GradleException("MAPS_API_KEY is missing from local.properties")
    }

androidComponents {
    onVariants { variant ->
        variant.manifestPlaceholders.put("MAPS_API_KEY", mapsApiKey)
        variant.buildConfigFields?.put(
            "MAPS_API_KEY",
            mapsApiKey.map { BuildConfigField("String", "\"$it\"", null) }
        )
    }
}

dependencies {
    // Project
    implementation(projects.shared)
    implementation(projects.components)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // DI
    implementation(libs.koin.androidx.compose)

    // Tooling
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Test
    testImplementation(projects.testing)
    testImplementation(libs.bundles.android.testing)
}
