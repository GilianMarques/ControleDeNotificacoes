import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "dev.gmarques.controledenotificacoes"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.gmarques.controledenotificacoes"
        minSdk = 24
        targetSdk = 35
        versionCode = 25
        versionName = "0.0.${versionCode}-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {

        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            signingConfig = signingConfigs.getByName("release")
            resValue("string", "app_name", "Controle (Staging)")
            resValue("color", "app_icon_bg_color", "#9A0098")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }


    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {

    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1") // TODO: remover
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.navigation:navigation-common-ktx:2.9.1") // Permite usar NavDeepLinkBuilder para abrir fragmentos especificos via notificação com argumentos
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.9.1") // Feature module support for Fragments
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.1") // Views/Fragments integration
    implementation("androidx.navigation:navigation-ui-ktx:2.9.1")
    implementation("androidx.room:room-ktx:2.7.2")
    implementation("androidx.room:room-runtime:2.7.2")

    implementation("com.airbnb.android:lottie:6.6.7")
    implementation("com.firebaseui:firebase-ui-auth:9.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.zawadz88:MaterialPopupMenu:4.1.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("com.google.firebase:firebase-auth:23.2.1")
    implementation("com.google.firebase:firebase-config:22.1.2")
    implementation("com.google.firebase:firebase-crashlytics:19.4.4")

    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2") // Para suporte a Kotlin
    implementation("net.danlew:android.joda:2.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    //noinspection KaptUsageInsteadOfKsp
    kapt("androidx.room:room-compiler:2.7.2")
    //noinspection KaptUsageInsteadOfKsp
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:2.7.2")
    testImplementation("io.mockk:mockk:1.14.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.3") // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.3")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0") // imitar classes
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.3")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.13.3") // Necessário para rodar JUnit 4 com o 5
    testImplementation(kotlin("test"))

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:2.9.1")
    // necessario para testes instrumentados pois ainda usam junit4 como padrao
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

tasks.withType<Test> {
    useJUnitPlatform() // Garante que JUnit 5 será usado nos testes unitarios
}
