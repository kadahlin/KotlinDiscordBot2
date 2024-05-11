import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":server"))

    val cdkVersion = "2.137.0"
    implementation("software.amazon.awscdk:aws-cdk-lib:${cdkVersion}")
    implementation("io.arrow-kt:arrow-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-runtime-interface-client:2.5.0")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("io.strikt:strikt-arrow:0.34.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}

application.mainClass.set("com.kyledahlin.discord.stack.HonkbotAppKt")
