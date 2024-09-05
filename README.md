# KMBBJ_BACKEND

KMBBJ 팀의 백엔드 리포지토리입니다. 이 프로젝트는 [Spring Boot](https://spring.io/projects/spring-boot)를 기반으로 하며, RESTful API 및 데이터베이스 연동을 포함한 다양한 백엔드 기능을 구현하고 있습니다.

## 프로젝트 개요

**아이디어 계기**

암호화폐와 블록체인의 인기가 높아지면서 많은 사람들이 코인 거래에 관심을 갖게 되었습니다. 그러나 실제 거래는 위험이 따르며, 초보자들은 쉽게 접근하기 어렵습니다. 사용자들이 게임을 통해 안전하게 거래를 시뮬레이션하고, 거래 기술을 익힐 수 있는 재미있는 플랫폼을 제공합니다.

**기대 효과**

실제 돈이 아닌 가상의 자산을 사용하여 코인을 사고팔고, 시장의 변동성을 체험할 수 있습니다. 이를 통해 사용자는 실제 거래의 복잡성과 위험을 이해하고, 전략을 개발하며, 친구들과 경쟁할 수 있습니다.


## 주요 기능
- 사용자 인증 및 권한 관리
- 제품 정보 관리 및 검색 API
- 주문 처리 및 결제 API
- 관리자 대시보드 기능
- 랜덤 매칭

## 역할
- 김강현 : 레이팅, 게임
- 문수혁 : 관리자, 알림, 공통응답처리
- 정재영 : 매칭 시스템
- 박석원 : OAuth 인증 & 관리, 거래
- 배민서 : 차트

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
│   │ ├── java/        # Java 소스 파일
│ │ │  └── com/ 
│ │ │   └── kmbbj/ 
│ │ │    └── backend/ 
│ │ │         ├── admin/ //관리자 기능
│ │ │         ├── auth/ //사용자 인증, 회원가입, 사용자관리
│ │ │         ├── balance/ //사용자 총 잔액
│ │ │         ├── charts/  //코인, 코인 차트
│ │ │         ├── Transaction/ //거래 기능
│ │ │         ├── games/ //게임 기능
│ │ │         ├── global/ //전역으로 사용하는 설정
│ │ │         ├── matching/ //게임 방생성, 매칭
│ │ │         └── notifications/  //알림
│   │── resources/   # 설정 파일 및 리소스
│   │  └── cql/ //cassandra cql 문
│   │  └── properties/ //각종 환경 변수 및 DB 설정 파일 및 선행작업
│   │  └── sql/ //sql 파일
│   └── test/            # 테스트 코드
├── build/               # 빌드 아티팩트
├── Dockerfile           # Docker 이미지 빌드 설정 파일
├── Jenkinsfile          # Jenkins 파이프라인 설정 파일
└── README.md            # 리포지토리 개요 (현재 파일)
```
![시스템 아키텍처](https://github.com/user-attachments/assets/d84be2b4-a545-4c36-b989-dd44d8f50515)

## wiki
https://github.com/KMBBJ/KMBBJ_BACKEND/wiki
