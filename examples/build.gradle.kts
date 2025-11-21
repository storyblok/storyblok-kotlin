//import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.android.kotlin.multiplatform.library)
}
kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }

//    androidLibrary {
//        namespace = "com.storyblok.ktor-client-storyblok"
//        compileSdk = libs.versions.android.compileSdk.get().toInt()
//        minSdk = libs.versions.android.minSdk.get().toInt()
//
//        withJava() // enable java compilation support
//        withHostTestBuilder {}.configure {}
//        withDeviceTestBuilder {
//            sourceSetTreeName = "test"
//        }
//
//        compilations.configureEach {
//            compilerOptions.configure {
//                jvmTarget.set(
//                    JvmTarget.JVM_11
//                )
//            }
//        }
//    }
//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//    linuxX64()

    sourceSets {

        commonTest.dependencies {
            implementation(project("::ktor-plugin"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
        }

        jvmTest.dependencies {
            implementation(libs.logback.classic)
        }
    }
}
