pipeline {
    agent any // 모든 사용 가능한 에이전트(또는 노드)에서 파이프라인을 실행

    // environment 블록에서 불필요한 자격 증명 설정 제거
    stages {
        stage('Checkout') { // 첫 번째 단계: 코드 체크아웃
            steps {
                // Git 리포지토리에서 코드 체크아웃
                // - branch: 'main' 브랜치를 체크아웃
                // - url: Git 리포지토리 URL
                // - credentialsId: 인증 정보 ID를 사용하여 리포지토리에 접근
                git branch: 'main', url: 'https://github.com/KMBBJ/KMBBJ_BACKEND.git', credentialsId: 'parkswon1'
            }
        }
        stage('Fetch and Create .env') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'env', variable: 'ENV_FILE')]){
                        sh 'mkdir -p backend/src/main/resources/properties'
                        def envContent = readFile(ENV_FILE)
                        echo "envContent: ${envContent}"
                        writeFile file: 'backend/src/main/resources/properties/.env', text: envContent
                    }
                }
            }
        }
        stage('Read .env') {
            steps {
                script {
                    sh 'cat backend/src/main/resources/properties/.env'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    sh 'chmod 755 backend/gradlew'
                    sh 'cd backend && ./gradlew build'
                }
            }
        }
        stage('Deploy to EC2') {
            steps {
                script {
                    // Jenkins에서 EC2 IP 주소를 가져옵니다.
                    withCredentials([string(credentialsId: 'EC2_IP', variable: 'EC2_IP')]) {
                        sshagent (credentials: ['ssh']) {
                            sh '''
                                ssh -o StrictHostKeyChecking=no ubuntu@$EC2_IP '
                                    cd /home/ubuntu/app/backend &&
                                    docker build -t my-spring-app . &&
                                    docker stop spring-app || true &&
                                    docker rm spring-app || true &&
                                    docker run -d --name spring-app -p 8080:8080 my-spring-app
                                '
                            '''
                        }
                    }
                }
            }
        }
    }
    post {
        success {
        echo "빌드 성공 후 알림 전송 시작"
            script {
                withCredentials([string(credentialsId: 'kmbbj_jenkins_build_alarm', variable: 'DISCORD')]) {
                    def changeLog = ""
                    for (changeSet in currentBuild.changeSets) {
                        for (entry in changeSet.items) {
                            def shortMsg = entry.msg.take(50)
                            changeLog += "* ${shortMsg} [${entry.author}]\n"
                        }
                    }
                    if (!changeLog) {
                        changeLog = "No changes in this build."
                    }
                    discordSend description: "${changeLog}",
                    footer: "내 코드가 돌아 간다고? 거짓말 하지마",
                    link: env.BUILD_URL, result: currentBuild.currentResult,
                    title: "KMBBJ_CI/CD \nSUCCESS",
                    webhookURL: "$DISCORD"
                }
            }
        }

        failure {
        echo "빌드 실패 후 알림 전송 시작" // 디버깅을 위한 메시지
            withCredentials([string(credentialsId: 'kmbbj_jenkins_build_alarm', variable: 'DISCORD')]) {
                discordSend description: """
                제목 : ${currentBuild.displayName}
                결과 : ${currentBuild.result}
                실행 시간 : ${currentBuild.duration / 1000}s
                 """,
                 link: env.BUILD_URL, result: currentBuild.currentResult,
                 title: "${env.JOB_NAME} : ${currentBuild.displayName} 실패",
                 webhookURL: "$DISCORD"
            }
        }
    }
}