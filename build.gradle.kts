import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

// Detect os to allow building with the correct bindings
val os : OperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()

logger.lifecycle("*** Building ChorFlow for ${os.name} ***")

plugins {
    kotlin("jvm") version "1.7.10"
    antlr
    application
}

group = "me.alexander"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // ANTLR: Parser generator, generates recognizer for a language based on a grammar
    antlr("org.antlr:antlr4:4.11.1")

    // Builtin graphviz support to visualize flow graphs
    implementation("guru.nidi:graphviz-kotlin:0.18.1")

    // Logging - dependency for graphviz-kotlin
    implementation("org.apache.logging.log4j:log4j:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")

    // Bindings for V8, used to execute graphviz on the Javascript V8 engine - OS-dependent
    if (os.isWindows)
        implementation("com.eclipsesource.j2v8:j2v8_win32_x86_64:4.6.0")
    else if (os.isMacOsX)
        implementation("com.eclipsesource.j2v8:j2v8_macosx_x86_64:4.6.0")
    else
        // Assume Linux bindings work for remaining
        implementation("com.eclipsesource.j2v8:j2v8_linux_x86_64:4.8.0")

    // Arg parsing library
    implementation("com.github.ajalt.clikt:clikt:3.5.0")
}

tasks.generateGrammarSource {
    outputDirectory = File("src/main/kotlin/chorflow/grammar")
    arguments = arguments + listOf("-no-listener", "-visitor", "-package", "chorflow.grammar")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    dependsOn(tasks.generateGrammarSource)
}

configure<SourceSetContainer> {
    main {
        java.srcDir("src/main/kotlin/chorflow/grammar")
    }
}

application {
    mainClass.set("chorflow.MainKt")
}
