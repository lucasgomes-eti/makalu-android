import com.android.build.api.artifact.SingleArtifact
import java.io.StringReader
import java.util.Base64
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

val localProperties = Properties().apply {
    providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText
        .orNull
        ?.let { text -> StringReader(text).use { load(it) } }
}

fun localProperty(name: String): String? =
    localProperties.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }

val releaseKeystoreBase64 = localProperty("RELEASE_KEYSTORE_BASE64")
val releaseStorePassword = localProperty("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = localProperty("RELEASE_KEY_ALIAS")
val releaseKeyPassword = localProperty("RELEASE_KEY_PASSWORD")
val missingReleaseSigningProperties = mapOf(
    "RELEASE_KEYSTORE_BASE64" to releaseKeystoreBase64,
    "RELEASE_STORE_PASSWORD" to releaseStorePassword,
    "RELEASE_KEY_ALIAS" to releaseKeyAlias,
    "RELEASE_KEY_PASSWORD" to releaseKeyPassword
).filterValues { it == null }.keys
val releaseKeystore = layout.buildDirectory.file("signing/release.jks")

android {
    namespace = "eti.lucasgomes.makalu"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "eti.lucasgomes.makalu"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystore.get().asFile
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (missingReleaseSigningProperties.isEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
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

val prepareReleaseKeystore by tasks.registering {
    group = "build"
    description = "Decodes the release keystore from RELEASE_KEYSTORE_BASE64 in local.properties."
    val missing = missingReleaseSigningProperties.toList()
    val keystoreBase64 = releaseKeystoreBase64.orEmpty()
    val keystore = releaseKeystore
    doLast {
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Release signing requires ${missing.joinToString()} in local.properties"
            )
        }
        keystore.get().asFile.apply {
            parentFile.mkdirs()
            writeBytes(Base64.getMimeDecoder().decode(keystoreBase64))
        }
    }
}

tasks.matching {
    it.name in setOf("validateSigningRelease", "packageRelease", "packageReleaseBundle")
}.configureEach {
    dependsOn(prepareReleaseKeystore)
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val version = "${libs.versions.versionName.get()}-${libs.versions.versionCode.get()}"
        tasks.register<Sync>("buildRelease") {
            group = "build"
            description = "Builds the signed and minified release APK and AAB with the R8 mapping."
            from(variant.artifacts.get(SingleArtifact.APK)) {
                include("*.apk")
                rename { "makalu-$version.apk" }
            }
            from(variant.artifacts.get(SingleArtifact.BUNDLE)) {
                rename { "makalu-$version.aab" }
            }
            from(variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)) {
                rename { "makalu-$version-mapping.txt" }
            }
            into(layout.buildDirectory.dir("release"))
        }
    }
}

dependencies {
    // Project
    implementation(projects.shared)
    implementation(projects.components)
    implementation(projects.features.auth)
    implementation(projects.features.home)
    implementation(projects.features.orders)
    implementation(projects.features.profile)
    implementation(projects.features.address)
    implementation(projects.features.store)
    implementation(projects.features.menuItem)
    implementation(projects.features.cart)

    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.navigation)

    // DI
    implementation(libs.koin.androidx.compose)

    // Tooling
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Maps
    implementation(libs.play.services.location)

    // Test
    testImplementation(projects.testing)
    testImplementation(libs.bundles.android.testing)
}
