plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
