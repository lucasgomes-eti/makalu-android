import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.StringReader
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

    val baseUrl = providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .orElse("")
        .map { text ->
            val url = Properties()
                .apply { StringReader(text).use { load(it) } }
                .getProperty("BASE_URL")
                ?.trim()
                .orEmpty()
            if (url.isBlank()) {
                throw GradleException("BASE_URL is missing from local.properties")
            }
            if (!url.endsWith("/")) {
                throw GradleException("BASE_URL in local.properties must end with '/' (got \"$url\")")
            }
            url
        }

    val generateConfig by tasks.registering {
        val outputDir = layout.buildDirectory.dir("generated/source/config")

        inputs.property("baseUrl", baseUrl)
        outputs.dir(outputDir)

        doLast {
            val file = outputDir.get().file("MakaluConfig.kt").asFile
            file.parentFile.mkdirs()
            file.writeText(
                """
            object MakaluConfig {
                const val BASE_URL = "${baseUrl.get()}"
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

    // Test
    testImplementation(projects.testing)
}
