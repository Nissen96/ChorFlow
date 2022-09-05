import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    antlr("org.antlr:antlr4:4.10.1")
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
