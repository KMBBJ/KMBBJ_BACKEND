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
        stage('Build') { // 두 번째 단계: 코드 빌드
            steps {
                // 빌드 단계 로그 메시지 출력
                echo 'Building...'
                sh 'echo $JAVA_HOME'
                sh 'java -version'
                sh 'chmod 755 backend/gradlew'
                sh 'cd backend && ./gradlew build'
                // 여기에 실제 빌드 작업을 추가
            }
        }

        stage('Test') { // 세 번째 단계: 테스트
            steps {
                // 테스트 단계 로그 메시지 출력
                echo 'Testing...'
                // 여기에 실제 테스트 작업을 추가
            }
        }

        stage('Docker Build and Deploy') {
            steps {
                echo 'Building Docker image...'
                sh '''
                docker build -t my-backend-app:latest backend/
                echo 'Stopping and removing old container if exists...'
                docker stop my-backend-app || true
                docker rm my-backend-app || true
                echo 'Running new Docker container...'
                docker run -d -p 8080:8080 --name my-backend-app my-backend-app:latest
                '''
            }
        }
    }

    post {
        always {
            echo 'Cleaning up...'
            sh 'docker system prune -f'
        }
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}