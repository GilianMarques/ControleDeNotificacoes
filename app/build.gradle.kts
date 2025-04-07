plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")

    // Kotlin serialization plugin for type safe routes and navigation arguments
    kotlin("plugin.serialization") version "2.0.21"

    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "dev.gmarques.controledenotificacoes"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.gmarques.controledenotificacoes"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    //noinspection KaptUsageInsteadOfKsp
    kapt("androidx.room:room-compiler:2.6.1")
    // optional - Test helpers
    testImplementation("androidx.room:room-testing:2.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.activity:activity-ktx:1.10.1")

    //moshi pra uso com json
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0") // Para suporte a Kotlin

    //falsificar objetos
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.16.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    //testar corrotinas
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1") // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.1") // Necessário para rodar JUnit 4 com o 5
    testImplementation(kotlin("test"))

    //navigation
    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.8.9")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.9")

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // necessario para testes instrumentados pois ainda usam junit4 como padrao
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")


}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

tasks.withType<Test> {
    useJUnitPlatform() // Garante que JUnit 5 será usado nos testes unitarios
}