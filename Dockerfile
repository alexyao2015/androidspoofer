FROM node:20 AS ui-build

WORKDIR /build
COPY ui/package.json ui/yarn.lock .
RUN set -x \
    && yarn

COPY ui .
RUN set -x \
    && mkdir -p /app/src/main/assets/webview \
    && yarn build

FROM alpine AS sdkmanager

WORKDIR /sdk

RUN set -x \
    && wget -O /tmp/sdk-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip \
    && unzip /tmp/sdk-tools.zip -d . \
    && mkdir latest \
    && mv cmdline-tools/* latest \
    && mv latest cmdline-tools

FROM eclipse-temurin:17
WORKDIR /build

# Configure a tmp home for unknown users
ENV HOME=/tmp/builduser
ENV CACHE_DIR=/build/cache
ENV GRADLE_USER_HOME=${CACHE_DIR}/gradle
ENV ANDROID_HOME=${CACHE_DIR}/sdk

# Install Android SDK
ENV PATH=/sdk/cmdline-tools/latest/bin:$PATH
# Mount a temporary volume to install the latest sdkmanager
RUN --mount=type=bind,target=/tmp/sdk,source=/sdk,from=sdkmanager,rw \
    set -x \
    && mkdir /sdk \
    && yes | /tmp/sdk/cmdline-tools/latest/bin/sdkmanager --licenses --sdk_root=/sdk \
    && /tmp/sdk/cmdline-tools/latest/bin/sdkmanager \
        --sdk_root=/sdk \
          "cmdline-tools;latest"

COPY --from=ui-build /app /ui/app
COPY build_entrypoint.sh /
# COPY . .
# RUN chmod -R 777 /build

ENTRYPOINT ["/build_entrypoint.sh"]