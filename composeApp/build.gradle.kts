import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.json:json:20240303")
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

                /* ---------- JavaFX (Strict Linux Only) ---------- */
                val javafxVersion = "17.0.10"
                val targetOs = "linux"

                implementation("org.openjfx:javafx-base:$javafxVersion:$targetOs")
                implementation("org.openjfx:javafx-graphics:$javafxVersion:$targetOs")
                implementation("org.openjfx:javafx-controls:$javafxVersion:$targetOs")
                implementation("org.openjfx:javafx-web:$javafxVersion:$targetOs")
                implementation("org.openjfx:javafx-swing:$javafxVersion:$targetOs")
                implementation("org.openjfx:javafx-media:$javafxVersion:$targetOs")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.echo.project.MainKt"

        jvmArgs += listOf(
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "-Dprism.order=sw"
        )

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm)

            packageName = "EchoChatBot"
            packageVersion = "1.0.0"

            linux {
                shortcut = true
                appCategory = "Utility"
                menuGroup = "EchoStudio"
                debMaintainer = "admin@echo.org"

                // FIXED: Only try to set the icon if the file actually exists
                val icon = project.file("src/jvmMain/resources/icon.png")
                if (icon.exists()) {
                    iconFile.set(icon)
                }
            }
        }
    }
}