import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import java.io.StringReader
import java.util.Properties
import org.sonarqube.gradle.SonarExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.sonarqube)
    jacoco
}

val jacocoVersion = libs.versions.jacoco.get()

val coverageExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/ComposableSingletons*",
    "**/*\$\$serializer.class",
    "MakaluConfig.class",
    "**/ui/theme/**",
    "**/components/ColorKt*",
    "**/*ModuleKt.class",
    "**/*ModuleKt$*.class"
)

val sonarCoverageExclusions = listOf(
    "**/ui/theme/**",
    "**/components/Color.kt",
    "**/*Module.kt"
)

val localProperties = Properties().apply {
    providers.fileContents(layout.projectDirectory.file("local.properties")).asText.orNull
        ?.let { text -> StringReader(text).use { load(it) } }
}

fun localProperty(name: String): String? =
    localProperties.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }

val sonarHostUrl = localProperty("SONAR_HOST_URL")
val sonarToken = localProperty("SONAR_TOKEN")
val sonarProjectKey = localProperty("SONAR_PROJECT_KEY")
val sonarProjectName = localProperty("SONAR_PROJECT_NAME")

val aggregatedCoverageXml = layout.buildDirectory.file("reports/jacoco/aggregated/jacoco.xml")

jacoco {
    toolVersion = jacocoVersion
}

val jacocoAggregatedReport = tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates a JaCoCo coverage report merging the unit tests of every module."
    reports {
        xml.required = true
        html.required = true
        xml.outputLocation = aggregatedCoverageXml
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco/aggregated/html")
    }
}

val ktlint = configurations.create("ktlint")

dependencies {
    ktlint(libs.ktlint.cli) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

val ktlintSources = listOf("**/src/**/*.kt", "**/*.kts", "!**/build/**")
val ktlintReport = layout.buildDirectory.file("reports/ktlint/ktlint.xml")

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Checks the Kotlin code style of every module with ktlint."
    classpath = ktlint
    mainClass = "com.pinterest.ktlint.Main"
    inputs.files(
        fileTree(layout.projectDirectory) {
            include("**/*.kt", "**/*.kts", ".editorconfig")
            exclude("**/build/**", ".gradle/**", ".kotlin/**")
        }
    )
    outputs.file(ktlintReport)
    args(
        "--reporter=plain",
        "--reporter=checkstyle,output=${ktlintReport.get().asFile.absolutePath}"
    )
    args(ktlintSources)
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fixes the Kotlin code style violations of every module that ktlint can fix."
    classpath = ktlint
    mainClass = "com.pinterest.ktlint.Main"
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    args("-F")
    args(ktlintSources)
}

sonar {
    properties {
        property("sonar.sources", existing("build.gradle.kts", "settings.gradle.kts"))
        property("sonar.kotlin.ktlint.reportPaths", ktlintReport.get().asFile.absolutePath)
        sonarProjectKey?.let { property("sonar.projectKey", it) }
        sonarProjectName?.let { property("sonar.projectName", it) }
        sonarHostUrl?.let { property("sonar.host.url", it) }
        sonarToken?.let { property("sonar.token", it) }
    }
}

tasks.named("sonar") {
    val isConfigured = sonarHostUrl != null && sonarToken != null && sonarProjectKey != null
    dependsOn(jacocoAggregatedReport)
    doFirst {
        if (!isConfigured) {
            throw GradleException(
                "SONAR_HOST_URL, SONAR_TOKEN and SONAR_PROJECT_KEY must be set in " +
                    "local.properties"
            )
        }
    }
}

subprojects {
    if (path == ":testing") {
        extensions.configure<SonarExtension> { isSkipProject = true }
        return@subprojects
    }

    extensions.configure<SonarExtension> {
        properties {
            property(
                "sonar.sources",
                existing(
                    "src/main/java",
                    "src/main/kotlin",
                    "src/main/AndroidManifest.xml",
                    "build.gradle.kts"
                )
            )
            property("sonar.tests", existing("src/test/java", "src/test/kotlin"))
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                aggregatedCoverageXml.get().asFile.absolutePath
            )
            property("sonar.coverage.exclusions", sonarCoverageExclusions)
        }
    }

    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            buildTypes.getByName("debug").enableUnitTestCoverage = true
            testCoverage.jacocoVersion = jacocoVersion
        }
        configureAndroidSonar()
        configureCoverage(
            testTaskName = "testDebugUnitTest",
            classesDir = "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
        )
    }
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            buildTypes.getByName("debug").enableUnitTestCoverage = true
            testCoverage.jacocoVersion = jacocoVersion
        }
        configureAndroidSonar()
        configureCoverage(
            testTaskName = "testDebugUnitTest",
            classesDir = "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
        )
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configureCoverage(testTaskName = "test", classesDir = "classes/kotlin/main")
        tasks.named<JacocoReport>("jacocoTestReport") {
            dependsOn(tasks.named("test"))
            reports.xml.required = true
            classDirectories.setFrom(
                fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
                    exclude(
                        coverageExclusions
                    )
                }
            )
        }
    }
}

fun Project.existing(vararg paths: String) = paths.map(::file).filter { it.exists() }

fun Project.configureCoverage(testTaskName: String, classesDir: String) {
    apply(plugin = "jacoco")
    extensions.configure<JacocoPluginExtension> { toolVersion = jacocoVersion }

    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    val testTasks = tasks.withType<Test>().matching { it.name == testTaskName }
    val classes = fileTree(layout.buildDirectory.dir(classesDir)) { exclude(coverageExclusions) }
    val sources = files("src/main/java", "src/main/kotlin")

    jacocoAggregatedReport.configure {
        dependsOn(testTasks)
        executionData(testTasks)
        classDirectories.from(classes)
        sourceDirectories.from(sources)
    }
}

fun Project.configureAndroidSonar() {
    val intermediates = layout.buildDirectory.dir("intermediates").get().asFile

    extensions.configure<SonarExtension> {
        setAndroidVariant("debug")
        properties {
            property(
                "sonar.java.binaries",
                listOf(
                    intermediates.resolve("built_in_kotlinc/debug/compileDebugKotlin/classes"),
                    intermediates.resolve("javac/debug/compileDebugJavaWithJavac/classes")
                )
            )
            property(
                "sonar.java.test.binaries",
                listOf(
                    intermediates.resolve(
                        "built_in_kotlinc/debugUnitTest/compileDebugUnitTestKotlin/classes"
                    )
                )
            )
        }
    }
}
