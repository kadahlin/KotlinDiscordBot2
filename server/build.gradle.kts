import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("kapt")
    id("org.jetbrains.kotlinx.kover")
    id("com.github.johnrengelman.shadow")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("com.google.firebase:firebase-admin:9.0.0")
    implementation("com.google.cloud:google-cloud-firestore:3.20.0")
    implementation("io.arrow-kt:arrow-core:1.2.1")
    implementation("ch.qos.logback:logback-classic:1.4.12")

    implementation("com.apple.itunes.storekit:app-store-server-library:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.slf4j:slf4j-simple:2.1.0-alpha1")

    // Dagger2 dependencies
    implementation("com.google.dagger:dagger:2.51.1")
    kapt("com.google.dagger:dagger-compiler:2.51.1")

    implementation("dev.kord:kord-core:0.13.1")
    implementation(project(":models"))

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("io.strikt:strikt-arrow:0.34.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.0.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}

koverReport {
    defaults {
        filters {
            excludes {
                annotatedBy("*CoverageExclude*")
                annotatedBy("*Serializable*")
                classes(
                    "*CoreModule*",
                    "*CoreComponent*",
                    "*_Factory*"
                )
            }
        }
//        verify {
//            rule("Minimum coverage") {
//                minBound(80)
//            }
//        }
    }
}

application.mainClass.set("com.kyledahlin.discord.server.ServerKt")
