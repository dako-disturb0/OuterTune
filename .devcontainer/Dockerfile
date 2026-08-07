FROM mcr.microsoft.com/devcontainers/java:21-bookworm

# 1. Install dependencies esensial + Github CLI + Akses Root tanpa password (NOPASSWD)
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget unzip usbutils curl git gpg sudo \
    && mkdir -p -m 755 /etc/apt/keyrings \
    && curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | tee /etc/apt/keyrings/githubcli-archive-keyring.gpg > /dev/null \
    && chmod 644 /etc/apt/keyrings/githubcli-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
    && apt-get update && apt-get install -y gh \
    && echo "vscode ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/vscode \
    && chmod 0440 /etc/sudoers.d/vscode \
    && rm -rf /var/lib/apt/lists/*

# 2. Setup direktori Android SDK, Kotlin, dan Antigravity CLI untuk user 'vscode'
RUN mkdir -p /opt/android-sdk /opt/kotlin /home/vscode/.gemini/antigravity_cli /home/vscode/.gemini/antigravity-cli \
    && chown -R vscode:vscode /opt/android-sdk /opt/kotlin /home/vscode/.gemini

# 3. Konfigurasi Antigravity CLI (Auto Accept & Tanpa Konfirmasi)
RUN echo '{ \
  "auto_accept": true, \
  "auto_approve": true, \
  "dangerously_skip_permissions": true, \
  "permissions": { \
    "mode": "auto_accept", \
    "allow_all": true, \
    "allow": ["*"] \
  }, \
  "security": { \
    "require_confirmation": false \
  } \
}' > /home/vscode/.gemini/antigravity_cli/settings.json \
    && cp /home/vscode/.gemini/antigravity_cli/settings.json /home/vscode/.gemini/antigravity-cli/settings.json \
    && chown -R vscode:vscode /home/vscode/.gemini

USER vscode
ENV ANDROID_HOME=/opt/android-sdk
ENV KOTLIN_HOME=/opt/kotlin
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${KOTLIN_HOME}/bin

# 4. Optimasi Paralelisasi Download Android Tools & Kotlin Compiler
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && (wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip &) \
    && (wget -q https://github.com/JetBrains/kotlin/releases/download/v2.1.0/kotlin-compiler-2.1.0.zip -O /tmp/kotlin.zip &) \
    && wait \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-extracted \
    && mv /tmp/cmdline-tools-extracted/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && unzip -q /tmp/kotlin.zip -d /tmp/kotlin-extracted \
    && mv /tmp/kotlin-extracted/kotlinc/* /opt/kotlin/ \
    && rm -rf /tmp/*.zip /tmp/*-extracted

# 5. Terima lisensi Android & install platform-tools
RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" \
                  "platforms;android-34" \
                  "build-tools;34.0.0"
