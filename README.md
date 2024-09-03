# KMBBJ_BACKEND

KMBBJ 팀의 백엔드 리포지토리입니다. 이 프로젝트는 [Spring Boot](https://spring.io/projects/spring-boot)를 기반으로 하며, RESTful API 및 데이터베이스 연동을 포함한 다양한 백엔드 기능을 구현하고 있습니다.

## 프로젝트 개요

이 리포지토리는 KMBBJ 팀의 백엔드 애플리케이션을 관리합니다. 애플리케이션은 다음과 같은 기능을 포함합니다:

- 사용자 인증 및 권한 관리
- 제품 정보 관리 및 검색 API
- 주문 처리 및 결제 API
- 관리자 대시보드 기능

## 주요 기술 스택

- **Java/open21**: 애플리케이션의 주요 프로그래밍 언어
- **Spring Boot**: 백엔드 애플리케이션 프레임워크
- **PostgreSQL**: 데이터베이스 관리 시스템
- **REDIS**: 사용자 토큰 관리 시스템
- **Cassandra**: 사용자 거래 관리 시스템
- **JPA**: 데이터베이스 ORM(Object-Relational Mapping)
- **Docker**: 컨테이너화 및 배포 자동화
- **Jenkins**: CI/CD 파이프라인 구축 및 자동 배포
- **AWS**: 백엔드 배포 및 CICD
- **GCP**: 프론트엔드 CICD 및 DB 배포

## 프로젝트 구조

```plaintext
KMBBJ_BACKEND/
├── src/                 # 소스 코드 디렉토리
│   ├── main/
│   │   ├── java/        # Java 소스 파일
│   │   └── resources/   # 설정 파일 및 리소스
│   └── test/            # 테스트 코드
├── build/               # 빌드 아티팩트
├── Dockerfile           # Docker 이미지 빌드 설정 파일
├── Jenkinsfile          # Jenkins 파이프라인 설정 파일
└── README.md            # 리포지토리 개요 (현재 파일)

![시스템 아키텍처](https://github.com/user-attachments/assets/d84be2b4-a545-4c36-b989-dd44d8f50515)
