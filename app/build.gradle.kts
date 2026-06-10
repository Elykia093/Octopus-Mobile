import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}

fun releaseSigningValue(name: String): String? =
    providers.gradleProperty(name).orNull?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }

val releaseSigningInputs = mapOf(
    "OCTOPUS_RELEASE_STORE_FILE" to releaseSigningValue("OCTOPUS_RELEASE_STORE_FILE"),
    "OCTOPUS_RELEASE_STORE_PASSWORD" to releaseSigningValue("OCTOPUS_RELEASE_STORE_PASSWORD"),
    "OCTOPUS_RELEASE_KEY_ALIAS" to releaseSigningValue("OCTOPUS_RELEASE_KEY_ALIAS"),
    "OCTOPUS_RELEASE_KEY_PASSWORD" to releaseSigningValue("OCTOPUS_RELEASE_KEY_PASSWORD"),
)
val hasPartialReleaseSigning = releaseSigningInputs.values.any { it != null } &&
    releaseSigningInputs.values.any { it == null }
val hasReleaseSigning = releaseSigningInputs.values.all { it != null }

if (hasPartialReleaseSigning) {
    throw GradleException(
        "Release signing is partially configured. Provide all of: " +
            releaseSigningInputs.keys.joinToString(", "),
    )
}

android {
    namespace = "com.elykia.octopus"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.elykia.octopus"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "0.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(releaseSigningInputs.getValue("OCTOPUS_RELEASE_STORE_FILE")))
                storePassword = requireNotNull(releaseSigningInputs.getValue("OCTOPUS_RELEASE_STORE_PASSWORD"))
                keyAlias = requireNotNull(releaseSigningInputs.getValue("OCTOPUS_RELEASE_KEY_ALIAS"))
                keyPassword = requireNotNull(releaseSigningInputs.getValue("OCTOPUS_RELEASE_KEY_PASSWORD"))
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    testBuildType = "release"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

val releaseArtifactFlavor = if (hasReleaseSigning) "signed" else "unsigned"
val releaseArtifactName = "Octopus-Mobile-v${android.defaultConfig.versionName}-$releaseArtifactFlavor.apk"
val copyVersionedReleaseApk = tasks.register("copyVersionedReleaseApk") {
    dependsOn("packageRelease")
    doLast {
        copy {
            from(layout.buildDirectory.dir("outputs/apk/release")) {
                include("*.apk")
                exclude("Octopus-Mobile-v*.apk")
                rename { releaseArtifactName }
            }
            into(layout.buildDirectory.dir("outputs/apk/release"))
        }
    }
}

tasks.configureEach {
    if (name == "assembleRelease") {
        finalizedBy(copyVersionedReleaseApk)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.hilt.android)
    kapt(libs.kotlin.metadata.jvm)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)

    implementation(libs.miuix.ui)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.runner)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("dagger.fastInit", "enabled")
    }
}
