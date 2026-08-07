FROM mcr.microsoft.com/devcontainers/java:21-bookworm

# 1. 安装基础依赖 (wget, unzip, usbutils, curl, git)
RUN apt-get update && apt-get install -y --no-install-recommends \
        wget \
        unzip \
        usbutils \
        curl \
        git \
    && rm -rf /var/lib/apt/lists/*

# 2. 安装 GitHub CLI (通过官方包仓库)
RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \
    && chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
    && apt-get update \
    && apt-get install -y --no-install-recommends gh \
    && rm -rf /var/lib/apt/lists/*

# 3. 允许 vscode 用户无密码使用 sudo (解决权限问题)
RUN echo "vscode ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# 4. 创建 Android SDK 与 Kotlin 目录并设置所有权
RUN mkdir -p /opt/android-sdk /opt/kotlin && chown -R vscode:vscode /opt/android-sdk /opt/kotlin

USER vscode

# 5. 设置环境变量
ENV ANDROID_HOME=/opt/android-sdk
ENV KOTLIN_HOME=/opt/kotlin
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${KOTLIN_HOME}/bin

# 6. 下载并安装 Android SDK Command Line Tools (版本: 11076708)
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-extracted \
    && mv /tmp/cmdline-tools-extracted/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools-extracted

# 7. 自动接受 Android 许可证并安装必要组件 (API 34)
RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" \
                  "platforms;android-34" \
                  "build-tools;34.0.0"

# 8. 下载并安装 Kotlin 编译器 (版本: 2.1.0)
RUN wget -q https://github.com/JetBrains/kotlin/releases/download/v2.1.0/kotlin-compiler-2.1.0.zip -O /tmp/kotlin.zip \
    && unzip -q /tmp/kotlin.zip -d /tmp/kotlin-extracted \
    && mv /tmp/kotlin-extracted/kotlinc/* /opt/kotlin/ \
    && rm -rf /tmp/kotlin.zip /tmp/kotlin-extracted

# 9. 安装 Antigravity CLI (使用官方安装脚本)
RUN curl -fsSL https://antigravity.google/cli/install.sh | bash

# 10. 配置 Antigravity CLI 以实现自动批准 (auto-accept)
RUN mkdir -p ~/.gemini/antigravity-cli \
    && echo '{ "toolPermission": "always-proceed" }' > ~/.gemini/antigravity-cli/settings.json
