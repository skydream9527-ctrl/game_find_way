plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":core"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
