FROM node:20 AS ui-build

WORKDIR /build
COPY ui .
RUN set -x \
    && mkdir -p /app/src/main/assets/webview \
    && yarn \
    && yarn build

FROM alpine AS sdkmanager

WORKDIR /sdkhome

RUN set -x \
    && wget -O /tmp/sdk-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip \
    && unzip /tmp/sdk-tools.zip -d . \
    && mkdir latest \
    && mv cmdline-tools/* latest \
    && mv latest cmdline-tools

FROM eclipse-temurin:17
WORKDIR /build

# Install Android SDK
ENV ANDROID_HOME=/sdkhome
ENV PATH=/sdkhome/cmdline-tools/latest/bin:$PATH
COPY --from=sdkmanager /sdkhome /sdkhome
RUN set -x \
    && yes | sdkmanager --licenses \
    && sdkmanager \
        "cmdline-tools;latest" \
        "build-tools;34.0.0" \
        "platform-tools" \
        "platforms;android-34"

COPY --from=ui-build /app /ui/app
COPY build_entrypoint.sh /
# COPY . .

ENTRYPOINT ["/build_entrypoint.sh"]