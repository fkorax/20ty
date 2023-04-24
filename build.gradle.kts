/*
 * Copyright (c) 2022  Franchesko Korako
 *
 * This file is part of 20ty.
 *
 * 20ty is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 20ty is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 20ty.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    java
    application
    idea
}

group = "io.github.fkorax"
version = "1.0-SNAPSHOT"

val jvmTarget = "11"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:16.0.2")
    implementation("com.formdev:flatlaf:3.0")
    implementation("com.dorkbox:SystemTray:4.2.1")
    // Has a lower version for compatibility with SystemTray:
    implementation("com.dorkbox:Utilities:1.39")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("io.github.fkorax.twenty.TwentyApp")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.withType<JavaCompile> {
    sourceCompatibility = jvmTarget
    targetCompatibility = jvmTarget
}

task<Jar>("fatJar") {
    group = "build"

    // TODO Move all licenses into a separate folder
    //  in META-INF. They should never have been in the root
    //  folder to begin with.
    duplicatesStrategy = DuplicatesStrategy.WARN

    manifest {
        attributes(
            "Implementation-Title" to "20ty",
            "Implementation-Version" to project.version,
            "Main-Class" to application.mainClass.get()
        )
    }
    archiveBaseName.set("${project.name}-all")
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    with(tasks.jar.get() as CopySpec)
}
