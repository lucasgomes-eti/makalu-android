import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.secrets) apply false
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
    "**/*ModuleKt$*.class",
)

jacoco {
    toolVersion = jacocoVersion
}

val jacocoAggregatedReport = tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates a JaCoCo coverage report merging the unit tests of every module."
    reports {
        xml.required = true
        html.required = true
        xml.outputLocation = layout.buildDirectory.file("reports/jacoco/aggregated/jacoco.xml")
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco/aggregated/html")
    }
}

subprojects {
    if (path == ":testing") return@subprojects

    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            buildTypes.getByName("debug").enableUnitTestCoverage = true
            testCoverage.jacocoVersion = jacocoVersion
        }
        configureCoverage(
            testTaskName = "testDebugUnitTest",
            classesDir = "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
        )
    }
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            buildTypes.getByName("debug").enableUnitTestCoverage = true
            testCoverage.jacocoVersion = jacocoVersion
        }
        configureCoverage(
            testTaskName = "testDebugUnitTest",
            classesDir = "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
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
