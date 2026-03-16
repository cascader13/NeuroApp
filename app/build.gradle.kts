import com.android.build.gradle.internal.tasks.factory.dependsOn
import kotlin.io.path.Path
import kotlin.io.path.pathString

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin
}

android {
    namespace = "com.neuroproject.neuro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.neuroproject.neuro"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        externalNativeBuild {
            cmake {
                cppFlags.add("")
            }
        }
        ndk{
            abiFilters.add("arm64-v8a")
        }
    }

    externalNativeBuild {
        cmake {
            path = File("src/main/cpp/CMakeLists.txt")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)

    implementation("androidx.datastore:datastore-preferences:1.1.1")


    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")


    kapt("androidx.room:room-compiler:$roomVersion") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-metadata-jvm")
    }


    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")

    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
    implementation("com.geyifeng.immersionbar:immersionbar-ktx:3.2.2")

    implementation("io.github.boguszpawlowski.composecalendar:composecalendar:1.3.0")
    implementation("io.github.boguszpawlowski.composecalendar:kotlinx-datetime:1.3.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.dagger:hilt-android:2.49")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation(files("libs/CapsuleService.aar"))
    implementation(files("libs/devicedriver.aar"))


    kapt("com.google.dagger:hilt-android-compiler:2.49")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


kapt {
    correctErrorTypes = true
    useBuildCache = false
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}

fun Task.findCapsuleSharedLib(aarPath: String): List<File> {
    return project.zipTree(aarPath).filter { it.name.endsWith(".so") }.toList()
}

data class AarSharedLibFileInfo(val name:String,val path:String,val architecture: String)

val capsuleSharedUnpackTaskName ="capsule_shared_unpack"
tasks.register<Copy>(capsuleSharedUnpackTaskName){
    val capsuleSharedLibs = findCapsuleSharedLib("libs/CapsuleService.aar")
    val libs = capsuleSharedLibs.map{
        val name = it.name
        val architecture = it.parentFile.name
        val path = it.path
        return@map AarSharedLibFileInfo(name,path,architecture)
    }
    val destination = Path(layout.buildDirectory.get().toString(),"capsule_shared")
    libs.forEach {
        from(it.path).into(Path(destination.pathString,it.architecture))
    }
}

tasks.named("preBuild").dependsOn(capsuleSharedUnpackTaskName)