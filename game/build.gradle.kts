/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.3/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.rabbitmq:amqp-client:5.16.0")
    
    implementation("org.json:json:20220924")
    implementation("com.google.code.gson:gson:2.10")

    implementation("org.slf4j:slf4j-api:1.7.26")
    implementation("org.slf4j:slf4j-log4j12:1.7.26")
}

application {
    // Define the main class for the application.
    mainClass.set("App")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
