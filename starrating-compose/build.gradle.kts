import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.library")
    `maven-publish`
}

val kuiklyVersion: String by project

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    js(IR) {
        browser()
    }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    sourceSets {
        commonMain.dependencies {
            api("com.tencent.kuikly-open:core:$kuiklyVersion")
            api("com.tencent.kuikly-open:compose:$kuiklyVersion")
            api(project(":starrating"))
        }
    }
}

android {
    namespace = "com.kuikly.starrating.compose"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("starrating-compose")
            description.set("Kuikly star rating — Compose DSL.")
            url.set("https://github.com/FooWrites/KuiklyStarRating")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("FooWrites")
                    name.set("FooWrites")
                }
            }
            scm {
                url.set("https://github.com/FooWrites/KuiklyStarRating")
                connection.set("scm:git:https://github.com/FooWrites/KuiklyStarRating.git")
                developerConnection.set("scm:git:ssh://git@github.com/FooWrites/KuiklyStarRating.git")
            }
        }
    }
}
