FROM mcr.microsoft.com/devcontainers/java:21-bookworm

# 1. Install dependencies esensial untuk Android SDK & Kotlin
RUN apt-get update && apt-get install -y wget unzip usbutils && rm -rf /var/lib/apt/lists/*

# 2. Setup direktori Android SDK & Kotlin dengan kepemilikan user 'vscode'
RUN mkdir -p /opt/android-sdk /opt/kotlin && chown -R vscode:vscode /opt/android-sdk /opt/kotlin

USER vscode
ENV ANDROID_HOME=/opt/android-sdk
ENV KOTLIN_HOME=/opt/kotlin
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${KOTLIN_HOME}/bin

# 3. Download dan ekstrak Android Command Line Tools (Stable version)
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-extracted \
    && mv /tmp/cmdline-tools-extracted/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools-extracted

# 4. Terima semua lisensi Android secara otomatis & install build tools (Target API 34)
RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" \
                  "platforms;android-34" \
                  "build-tools;34.0.0"

# 5. Download dan install Kotlin Compiler secara manual (Gak pake SDKMAN! biar anti-gagal)
RUN wget -q https://github.com/JetBrains/kotlin/releases/download/v2.1.0/kotlin-compiler-2.1.0.zip -O /tmp/kotlin.zip \
    && unzip -q /tmp/kotlin.zip -d /tmp/kotlin-extracted \
    && mv /tmp/kotlin-extracted/kotlinc/* /opt/kotlin/ \
    && rm -rf /tmp/kotlin.zip /tmp/kotlin-extracted
