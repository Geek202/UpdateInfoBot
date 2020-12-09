import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}

group = "me.geek.tom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        name = "Kord"
        url = uri("https://dl.bintray.com/kordlib/Kord")
    }
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-snapshots/")
    }
    maven {
        name = "Mojang"
        url = uri("https://libraries.minecraft.net/")
    }
    maven {
        name = "TomTheGeek"
        url = uri("https://maven.tomthegeek.ml/")
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // Kord Extensions framework
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.3-SNAPSHOT")

    // Config framework
    implementation("com.uchuhimo:konf:0.23.0")
    implementation("com.uchuhimo:konf-toml:0.23.0")

    // Guava
    implementation("com.google.guava:guava:29.0-jre")

    // Logging
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.1")
    implementation("org.apache.logging.log4j:log4j-iostreams:2.13.1")

    // WebRequests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0") {
        exclude(module = "okhttp")
    }

    // Data serialisation
    implementation("com.mojang:datafixerupper:4.0.26")

    // Analysis
    implementation("me.geek.tom:MCAutoCodeDiff:1.1-SNAPSHOT")
    implementation("net.fabricmc:stitch:0.5.1+build.77")
    implementation("org.cadixdev:lorenz-io-proguard:0.5.4")

    implementation("org.ow2.asm:asm:9.0")
    implementation("org.ow2.asm:asm-analysis:9.0")
    implementation("org.ow2.asm:asm-commons:9.0")
    implementation("org.ow2.asm:asm-tree:9.0")
    implementation("org.ow2.asm:asm-util:9.0")
}

application {
    mainClassName = "me.geek.tom.mcupdateinfo.MainKt"
}

tasks.withType(KotlinCompile::class).forEach {
    it.kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf(
            "-XXLanguage:+NewInference",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("build.properties") {
        expand("version" to project.version)
        filter { line ->
            line.replace("development", "production")
        }
    }
}
