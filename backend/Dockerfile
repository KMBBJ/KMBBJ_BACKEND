# 1. 베이스 이미지 선택 (Ubuntu 기반)
FROM ubuntu:22.04

# 2. 루트 사용자로 전환하여 필요한 패키지 설치
USER root

# 3. 필수 패키지 설치
RUN apt-get update && \
    apt-get install -y \
    curl \
    wget \
    zip

# 4. Oracle JDK 21 다운로드 URL 설정
ENV JDK_URL=https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz

# 5. Orcle JDK 21 설치
RUN wget $JDK_URL -O jdk-21_linux-x64_bin.tar.gz && \
    tar -xvf jdk-21_linux-x64_bin.tar.gz && \
    JDK_DIR=$(tar -tf jdk-21_linux-x64_bin.tar.gz | head -n 1 | cut -f1 -d"/") && \
    mv $JDK_DIR /usr/local/jdk-21 && \
    ln -sf /usr/local/jdk-21/bin/java /usr/bin/java && \
    ln -sf /usr/local/jdk-21/bin/javac /usr/bin/javac && \
    rm jdk-21_linux-x64_bin.tar.gz

# 4. JAVA_HOME 환경 변수 설정
ENV JAVA_HOME=/usr/local/jdk-21
ENV PATH=$JAVA_HOME/bin:$PATH

# 5. 작업 디렉토리 설정
ENV APP_HOME=/app
WORKDIR $APP_HOME

# 6. 빌드된 JAR 파일을 Docker 이미지에 복사
COPY build/libs/*.jar /app/myapp.jar

# 7. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "myapp.jar"]