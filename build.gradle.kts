plugins {
    kotlin("multiplatform") version "1.3.71"
}

group = "br.com.gamemods.koterite"
version = "1.0.0-SNAPSHOT"

repositories {
    //maven(url = "https://dl.bintray.com/kotlin/kotlinx")
    //maven(url = "https://dl.bintray.com/kotlin/ktor")
    //google()
    jcenter()
}

val ktor_version: String by project
val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")
println("!!!!!!!!!!!!!!!!!!!!!$mingwPath!!!!!!!!!!!!!!!!!!!!!!!!")

kotlin {
    jvm()

    // Determine host preset.
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")

    // Create target for the host platform.
    val hostTarget = when {
        //hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        //else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
        else -> null
    }

    hostTarget?.apply {
        binaries {
            sharedLib {
                baseName = "koterite-filesystem"
                //entryPoint = "sample.curl.main"
                if (isMingwX64) {
                    linkerOpts("-L${mingwPath.resolve("lib")}")
                }
            }
        }
    }
    /*mingwX64("windows") {
        binaries {
            sharedLib {
                baseName = "koterite-filesystem"
                linkerOpts("-L${mingwPath.resolve("lib")}")
            }
        }
    }
    linuxX64()*/
    // https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("br.com.gamemods.koterite:koterite-nbt:1.0.0-SNAPSHOT")
                //api("io.ktor:ktor-client-core:$ktor_version")
                api("io.ktor:ktor-client-core:$ktor_version")
                //api("io.ktor:ktor-utils:$ktor_version")
                //api("io.ktor:ktor-io:$ktor_version")
                //api("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api("io.ktor:ktor-client:$ktor_version")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        /*
        val windowsMain by getting
        val linuxX64Main by getting
        val nativeMain = listOf(windowsMain, linuxX64Main)

        configure(nativeMain) {
            dependencies {
                api("io.ktor:ktor-client-curl:$ktor_version")
            }
        }*/

        val nativeMain by getting {
            dependencies {
                api("io.ktor:ktor-client-curl:$ktor_version")
            }
        }
        val nativeTest by getting {
            dependencies {
                (dependsOn as MutableSet<*>).clear()
                kotlin.setSrcDirs(emptyList<Any>())
            }
        }
    }
}