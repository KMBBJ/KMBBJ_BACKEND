# 1. 베이스 이미지 선택 (Alpine Linux 기반)
FROM alpine:3.18

# 2. 필수 패키지 설치
RUN apk update && apk add --no-cache curl tar bash

# 3. Oracle JDK 21 다운로드 및 설치
ENV JDK_URL=https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz
RUN curl -L ${JDK_URL} -o /tmp/jdk.tar.gz && \
    mkdir /opt/jdk && \
    tar -xzf /tmp/jdk.tar.gz -C /opt/jdk --strip-components=1 && \
    rm /tmp/jdk.tar.gz

# 4. JAVA_HOME 환경 변수 설정
ENV JAVA_HOME=/opt/jdk
ENV PATH="$JAVA_HOME/bin:$PATH"

# 5. 작업 디렉토리 설정
ENV APP_HOME=/app
WORKDIR $APP_HOME

# 6. 빌드된 JAR 파일을 Docker 이미지에 복사
COPY build/libs/myapp.jar $APP_HOME/myapp.jar

# 7. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "myapp.jar"]