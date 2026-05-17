import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    androidTarget()

    jvmToolchain(17)

    jvm()


    sourceSets {

        commonMain.dependencies {

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta03")

            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

            implementation("io.ktor:ktor-client-core:3.1.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")

            implementation("app.cash.sqldelight:runtime:2.0.2")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
        }

        androidMain.dependencies {

            implementation("io.ktor:ktor-client-okhttp:3.1.3")

            implementation("app.cash.sqldelight:android-driver:2.0.2")
        }

        jvmMain.dependencies {

            implementation(compose.desktop.currentOs)

            implementation("io.ktor:ktor-client-cio:3.1.3")

            implementation("app.cash.sqldelight:sqlite-driver:2.0.2")


            implementation("org.xerial:sqlite-jdbc:3.46.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
        }


    }
}


android {

    namespace = "com.example.sicedroidmultiplatform"

    compileSdk =
        libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {

        applicationId =
            "com.example.sicedroidmultiplatform"

        minSdk =
            libs.versions.android.minSdk.get().toInt()

        targetSdk =
            libs.versions.android.targetSdk.get().toInt()

        versionCode = 1

        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {

        getByName("release") {

            isMinifyEnabled = false
        }
    }

    compileOptions {
         sourceCompatibility = JavaVersion.VERSION_17
         targetCompatibility = JavaVersion.VERSION_17
    }

}

sqldelight {
    databases {
        create("SicenetDatabase") {
            packageName.set("com.example.sicedroidmultiplatform.database")
        }
    }
}

dependencies {

    debugImplementation(
        libs.compose.uiTooling
    )
}

compose.desktop {

    application {

        mainClass =
            "com.example.sicedroidmultiplatform.MainKt"

        nativeDistributions {

            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb
            )

            packageName =
                "com.example.sicedroidmultiplatform"

            packageVersion = "1.0.0"
        }
    }
}