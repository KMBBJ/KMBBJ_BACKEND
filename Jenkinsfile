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
                    // Jenkins에서 EC2로 파일 전송 및 Docker 빌드/컨테이너 실행
                    sshagent (credentials: ['ssh']) {
                        sh '''
                            scp -o StrictHostKeyChecking=no -r backend/ ubuntu@${EC2_IP}:/home/ubuntu/app/
                            ssh ubuntu@${EC2_IP} << EOF
                                cd /home/ubuntu/app/backend
                                docker build -t my-spring-app .
                                docker stop spring-app || true
                                docker rm spring-app || true
                                docker run -d --name spring-app -p 8080:8080 my-spring-app
                            EOF
                        '''
                    }
                }
            }
        }
    }
}