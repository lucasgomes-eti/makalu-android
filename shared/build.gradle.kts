import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }

    val generateConfig by tasks.registering {
        val outputDir = layout.buildDirectory.dir("generated/source/config")

        outputs.dir(outputDir)

        doLast {
            val props = Properties().apply {
                load(rootProject.file("local.properties").inputStream())
            }

            val baseUrl = props.getProperty("BASE_URL") ?: ""

            val file = outputDir.get().file("MakaluConfig.kt").asFile
            file.parentFile.mkdirs()
            file.writeText(
                """
            object MakaluConfig {
                const val BASE_URL = "$baseUrl"
            }
            """.trimIndent()
            )
        }
    }

    sourceSets.main {
        kotlin.srcDir(generateConfig)
    }
}

dependencies {
    api(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
    // Ktor
    api(libs.bundles.ktor)
    // DataStore
    api(libs.datastore)
}