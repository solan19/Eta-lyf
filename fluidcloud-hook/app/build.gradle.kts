plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.mangi.hook"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.mangi.hook"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
