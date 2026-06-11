import com.android.build.gradle.internal.tasks.factory.dependsOn
import kotlin.io.path.Path
import kotlin.io.path.pathString

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("org.jetbrains.dokka") version "2.1.0"
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    // Настройка документации для Android модуля
    dokkaSourceSets {
        named("main") {
            // Android специфичные настройки
            sourceRoots.from("src/main/java")
            sourceRoots.from("src/main/kotlin")

            // Исключаем сгенерированный код
            sourceRoots.from("build/generated")

            // Настройка ссылок на Android документацию
            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
            }

            // Настройка для Compose
            perPackageOption {
                matchingRegex.set(".*\\.compose\\..*")
                suppress.set(true)  // Можно скрыть внутренние Compose компоненты
            }
        }
    }
}


tasks.register("dokkaModule") {
    dependsOn(tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>())
    group = "documentation"
    description = "Generate Dokka documentation for this module"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"  // для Kotlin 1.9.24
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.generativeai)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.hilt.common)

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
    implementation("com.geyifeng.immersionbar:immersionbar-ktx:3.2.2")
    implementation("io.github.boguszpawlowski.composecalendar:composecalendar:1.3.0")
    implementation("io.github.boguszpawlowski.composecalendar:kotlinx-datetime:1.3.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    implementation("androidx.work:work-runtime-ktx:2.9.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation(files("libs/CapsuleService.aar"))
    implementation(files("libs/devicedriver.aar"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.google.truth:truth:1.4.4")

    implementation(libs.kotlinx.serialization.json)
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