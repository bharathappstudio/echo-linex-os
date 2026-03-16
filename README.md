This is a Kotlin Multiplatform project targeting Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
Rebuild the JAR
  ```shell
  ./gradlew :composeApp:packageUberJar
  ```
  Locate jpackage and run it directly
  ```shell
  /usr/lib/jvm/java-17-openjdk-amd64/bin/jpackage \
  --input ~/Desktop/EchoBuild \
  --main-jar EchoChatBot-linux-x64-1.0.0.jar \
  --main-class org.echo.project.MainKt \
  --type deb \
  --name echochatbot \
  --app-version 1.0.0 \
  --vendor "EchoStudio" \
  --linux-shortcut \
  --linux-menu-group "Utility" \
  --dest ~/Desktop/EchoBuild/output
  ```
  on macOS/Linux
  ```shell
  sudo dpkg -i ~/Desktop/EchoBuild/output/echochatbot_1.0.0-1_amd64.deb
  ```



---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…