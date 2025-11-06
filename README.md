# 🧑‍💻 Session Login Project (with Redis)

본 프로젝트는 Spring Boot와 Redis를 사용하여 구현한 세션 기반 로그인 시스템입니다. 이 아키텍처의 핵심 목표는 **서버가 여러 대로 증설되는 수평적 확장(Scale-out) 상황에서도 세션의 정합성을 보장**하고, 사용자의 로그인 상태를 안정적으로 유지하는 것입니다.

기존에 톰캣 등 WAS의 메모리에 세션을 저장하는 방식은, 로드 밸런서 뒤에서 여러 서버가 동작할 때 특정 서버에 세션이 종속되는 'Sticky Session' 문제가 발생할 수 있습니다. 본 프로젝트에서는 **세션 데이터를 외부의 중앙 집중화된 Redis에 저장**함으로써, 어떤 서버로 요청이 들어와도 동일한 세션 정보를 참조할 수 있어 **무상태(Stateless) 서버 아키텍처를 유지**하고 안정적인 서비스 운영이 가능하도록 설계했습니다.

---

## 🚀 주요 기능

- **회원 관리**
    - 회원가입 (Password-hashing)
- **인증 관리**
    - 세션 기반 로그인
    - 로그아웃
- **세션 관리**
    - Redis를 사용한 분산 세션 클러스터링
    - 동시 접속자 제어 (사용자 당 1개 세션)

---

## 🛠️ 기술 스택

- **Backend:** Java 17, Spring Boot 3.5.7
- **Database:** MySQL, Redis
- **ORM:** Spring Data JPA
- **Security:** Spring Security, Spring Session Data Redis
- **Build Tool:** Gradle

---

## 📝 API Endpoints

| Method | URI | 설명 | 인증 |
| --- | --- | --- | :---: |
| `POST` | `/user` | 회원가입 | |
| `POST` | `/session` | 로그인 (세션 생성) | |
| `DELETE` | `/session` | 로그아웃 (세션 삭제) | ✔️ |

---

## 📋 요구사항

프로젝트를 실행하기 위해 다음의 환경이 필요합니다.

- **Java 17**
- **Gradle 8.0 이상**
- **Docker** 및 **Docker Compose**

---

## ⚙️ 프로젝트 실행 방법

1.  **Repository Clone**
    ```bash
    git clone https://github.com/your-username/session-login-project.git
    cd session-login-project
    ```

2.  **데이터베이스 실행**
    프로젝트 루트 경로의 `docker-compose.yml` 파일을 사용하여 `MySQL`과 `Redis`를 실행합니다.
    ```bash
    docker-compose up -d
    ```
    *   `application.yml`에 설정된 값과 `docker-compose.yml`의 `environment` 값이 동일하므로 별도의 설정 변경이 필요 없습니다.

3.  **프로젝트 빌드 및 실행**
    ```bash
    ./gradlew build
    java -jar build/libs/session-0.0.1-SNAPSHOT.jar
    ```

---

## 🗄️ 데이터베이스 스키마

### User

| Column | Type | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | `PK`, `Auto Increment` | 사용자 고유 ID |
| `username` | `VARCHAR(255)` | `Unique`, `Not Null` | 사용자 이메일 |
| `password` | `VARCHAR(255)` | `Not Null` | 암호화된 비밀번호 |
| `role_type` | `VARCHAR(255)` | | 사용자 권한 |