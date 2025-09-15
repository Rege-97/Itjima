<img width="500" alt="대지 3@3x" src="https://github.com/user-attachments/assets/f455d761-8aad-489f-b22a-20eaebc03580" />

> 📱 잊지마(Itjima) — 개인간 물품 대여 관리 앱

## 🚀 실행 방법 (Expo Go)

이 프로젝트는 **React Native + Expo** 기반으로 제작되었으며,  
아래 QR 코드를 스캔하면 바로 체험할 수 있습니다.

  <img width="300" height="300" alt="image" src="https://github.com/user-attachments/assets/c0888099-67c1-4a86-af20-2d5290d109ce" />

### 📲 실행 가이드
1. 스마트폰에 **Expo Go 앱** 설치 (iOS: App Store, Android: Play Store)  
2. 위 QR 코드 스캔 → 앱이 Expo Go에서 자동 실행  
3. 로그인/회원가입 후 기능 체험 가능  
   - 이메일 회원가입 시 실제 메일 인증 가능   

> ⚠️ **주의**: Expo Go는 네트워크 환경에 따라 로딩이 지연될 수 있습니다.  
> 서버는 AWS Elastic Beanstalk + RDS + S3에서 운영 중입니다.

## 📌 프로젝트 개요

**잊지마(Itjima)** 는 개인 간 **물품·금전 대여를 잊지 않고 관리**할 수 있도록 설계된 모바일 앱입니다.  
친구나 지인 사이에서 구두 약속으로만 이루어지는 대여가 종종 **기억에서 사라지거나 분쟁으로 이어지는 문제**를 해결하고자 했습니다.  

사용자는 앱을 통해 물품을 등록하고, 대여 계약을 체결하며, 상환 및 반납 과정을 **투명하게 기록**할 수 있습니다.  
직관적인 UI와 알림을 통해 현재 대여 현황을 쉽게 확인할 수 있으며, 자동 연체 처리로 잊힘 없이 관리할 수 있습니다.

### ✅ 주요 기능

- **[회원가입 및 인증]**  
  이메일 회원가입 + 인증 코드, 카카오 OAuth 로그인 지원,  
  JWT Access/Refresh 토큰 기반 무중단 로그인

- **[대여 계약 관리]**  
  대여 요청 → 승인/거절 → 대여 진행 → 상환/반납 완료까지 전 과정 앱 내 관리  
  역할(빌려준 사람/빌린 사람)에 따라 가능한 기능 분리  
  분할 상환 요청 및 잔액 자동 차감 기능

- **[대시보드]**  
  빌려준 건/빌린 건 통계 요약,  
  반납 임박·연체·승인 대기 건을 한눈에 확인

- **[내 물품 관리]**  
  등록한 물품 목록 및 상세 정보 관리  
  물품별 대여 이력 추적 및 이미지 업로드(S3 연동)

- **[상태 뱃지 및 알림]**  
  대여 상태(대기·진행·완료·연체)를 뱃지로 표시  
  반납일 경과 시 자동 연체 처리 및 알림 제공

- **[공통 인프라/기술]**  
  AOP 기반 활동 로그 기록  
  Spring Scheduler로 자동 연체 전환  
  Swagger 기반 API 문서 제공  
  Expo EAS Update로 앱 스토어 재심사 없이 배포



## 📌 미리보기

| 로그인 | 홈 화면 | 대여 목록 |
|--------|---------|-----------|
| <img width="300" alt="image" src="https://github.com/user-attachments/assets/dbef633e-bf3d-4563-9e4d-0c8f3902f683" /> | <img width="300" alt="image" src="https://github.com/user-attachments/assets/002a7915-9784-4b12-930d-853c97bc07a2" /> | <img width="300" alt="image" src="https://github.com/user-attachments/assets/056472cc-b5c3-4495-be84-9aa0524d9620" /> |

| 대여 상세 | 상환 기록 | 물품 관리 |
|-----------|-----------|-----------|
| <img width="300" alt="image" src="https://github.com/user-attachments/assets/890b3b93-2c0f-400c-b70c-f2df144c0be7" /> | <img width="300" alt="image" src="https://github.com/user-attachments/assets/0b3a4926-4e34-4e22-b3cd-0d1474edc932" /> | <img width="300" alt="image" src="https://github.com/user-attachments/assets/7dc3ffcf-556b-47ce-912b-aee529f02799" /> |

## 🛠 사용 기술 스택

| 분야 | 기술 |
|------|------|
| **언어** | ![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=java&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white) |
| **백엔드** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) ![MyBatis](https://img.shields.io/badge/MyBatis-0052CC?style=flat-square) ![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white) ![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=flat-square) ![Mockito](https://img.shields.io/badge/Mockito-7A1FA2?style=flat-square) |
| **프론트엔드(모바일)** | ![React Native](https://img.shields.io/badge/React_Native-61DAFB?style=flat-square&logo=react&logoColor=black) ![Expo](https://img.shields.io/badge/Expo-000020?style=flat-square&logo=expo&logoColor=white) ![React Navigation](https://img.shields.io/badge/React_Navigation-CA4245?style=flat-square) ![React Native Paper](https://img.shields.io/badge/React_Native_Paper-6200EE?style=flat-square) ![Axios](https://img.shields.io/badge/Axios-5A29E4?style=flat-square) |
| **데이터베이스** | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) |
| **서버 / 배포** | ![AWS Elastic Beanstalk](https://img.shields.io/badge/AWS_Elastic_Beanstalk-FF9900?style=flat-square&logo=awselasticbeanstalk&logoColor=white) ![AWS RDS](https://img.shields.io/badge/AWS_RDS-527FFF?style=flat-square&logo=amazonrds&logoColor=white) ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white) |
| **기타 도구** | ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=flat-square&logo=intellijidea&logoColor=white) ![Postman](https://img.shields.io/badge/Postman-FF6C37?style=flat-square&logo=postman&logoColor=white) ![DBeaver](https://img.shields.io/badge/DBeaver-372923?style=flat-square) |

## ✅ 기능 상세 설명

### 🔐 회원가입 및 로그인
- **이메일 회원가입**: 입력한 이메일로 인증 코드 발송 → 코드 검증 완료 시 계정 활성화.  
- **카카오 로그인**: 카카오 OAuth 기반 간편 로그인, 기존 계정과 충돌 시 연동 처리.  
- **JWT 인증**: Access/Refresh 토큰 발급 및 자동 재발급으로 무중단 로그인 유지.  
- **보안 강화**: 비밀번호는 BCrypt 암호화, 이메일/전화번호 중복 사전 차단.


### 🏠 홈 화면 대시보드
- **빌려준 건/빌린 건 통계** 요약 제공.  
- **반납 임박/연체 알림**: 기한 임박 건은 별도 표시, 반납일 초과 시 자동 연체 전환.  
- **승인 대기 요청 확인**: 상대방 응답 대기 중인 요청을 한눈에 확인 가능.


### 📋 대여 등록 및 관리
- **대여 등록**: 물품·금전을 조건과 함께 등록.  
- **상태 전이(State Machine)**:  
  - `PENDING` → `ACCEPTED` → `COMPLETE`  
  - 예외 상태: `REJECTED`, `CANCELLED`, `OVERDUE`  
- **분할 상환**: 채무자는 일부 금액만 상환 요청 가능, 서버는 잔여 금액 자동 차감.  
- **권한 분리**: 채권자/채무자에 따라 승인·거절·상환·완료 버튼이 다르게 노출.


### 📑 대여 상세 화면
- **계약 요약 정보**: 물품명·금액, 대여자/차용자 정보, 반납일 등 표시.  
- **활동 타임라인**: AOP 기반 로깅으로 승인, 상환, 연체 이벤트 추적 가능.  
- **버튼 액션**:  
  - 채무자: 상환 요청 가능  
  - 채권자: 승인/거절, 반납 완료 가능  
- **연체 자동 처리**: Scheduler가 상태 업데이트 → 사용자 알림 전달.


### 📦 내 물품 관리
- **목록 화면**: 내가 등록한 물품을 카드 UI로 나열.  
- **상세 화면**: 이미지, 설명, 등록일, 해당 물품의 대여 이력 조회.  
- **이미지 업로드**: AWS S3에 저장, 실패 시 DB 롤백 처리.  
- **수정/삭제**: 물품 정보 업데이트 및 삭제 가능.

### 🔔 상태 표시 및 알림
- **상태 뱃지**: PENDING, ACCEPTED, COMPLETE, OVERDUE 등 직관적 상태 표시.  
- **실시간 알림**: 승인/거절, 상환 요청, 연체 전환 시 알림 제공.  
- **자동 연체**: 매일 Scheduler로 연체 여부 점검 및 상태 업데이트.


### 🧩 공통 기능
- **공통 응답 구조**:  
  - 모든 API는 `ApiResponse<T>` 포맷(`code`, `message`, `data`)으로 반환.  
  - 목록 응답은 `PagedResultDTO<T>`로 커서 기반 페이지네이션 제공.  
- **전역 예외 처리**:  
  - `@RestControllerAdvice` 기반 글로벌 예외 핸들링.  
  - `NotFoundItemException`, `InvalidStateException`, `DuplicateUserFieldException` 등 커스텀 예외 정의.  
  - 모든 예외는 `ApiResponse<Error>` 형태로 일관되게 반환.  
- **트랜잭션 관리 & 조건부 업데이트**:  
  - 핵심 로직은 `@Transactional`로 묶어 무결성 보장.  
  - MyBatis 조건부 업데이트 쿼리로 동시성 문제 차단.  
- **파일 업로드 유틸**:  
  - `FileUtil` 클래스 설계로 업로드/삭제 공통 처리.  
  - AWS S3 연동, 실패 시 DB 롤백 + S3 정합화.  
  - 모든 도메인(물품, 프로필 등)에서 재사용 가능.  
- **AOP 활동 로그**:  
  - `ActivityLogAspect`로 대여/상환/연체 이벤트 자동 기록.  
  - 분쟁 발생 시 증거 데이터로 활용 가능.  
- **Swagger 기반 API 문서**:  
  - `springdoc-openapi`로 엔드포인트/DTO 자동 문서화.  
  - 개발자/시연 시 API 구조 즉시 확인 가능.  
- **테스트 코드**:  
  - JUnit5 + Mockito 기반 단위 테스트.  
  - MockMvc 기반 통합 테스트로 API 검증.  
  - 상태 전이, 예외, 페이지네이션 등 주요 기능을 테스트로 보장.


### ☁️ 배포 & 운영 환경
- **백엔드**: AWS Elastic Beanstalk — Spring Boot 앱 자동 배포 및 무중단 운영.  
- **DB**: AWS RDS (MySQL 8.4) — 안정적 데이터 관리.  
- **이미지 저장소**: AWS S3 — 확장성과 정합성 확보.  
- **모바일 앱**: Expo EAS Update — 앱스토어 재심사 없이 즉시 업데이트 배포.  
- **운영 최적화**: 로깅/모니터링으로 장애 추적, 프리티어 환경에서 Swap Memory 설정으로 안정성 확보.

## 📈 결과 & 회고

### 🚀 성과 요약

- AWS Elastic Beanstalk, RDS, S3, Expo EAS Update를 활용하여 **실제 사용자 서비스 가능한 수준으로 배포**  
- 물품/금전 대여의 전 과정(등록 → 승인/거절 → 상환/반납 → 완료/연체)을 앱 내에서 **단일 흐름으로 체계화**  
- 채권자/채무자 **역할 기반 권한 분리**와 상태 전이 규칙(State Machine)으로 **데이터 무결성과 신뢰성 확보**  
- **JWT + Spring Security 기반 인증/인가**로 사용자 세션 무중단 유지 및 보안 강화  
- **전역 예외 처리 + 공통 응답 포맷**으로 일관된 API 반환 구조 확보  
- **AOP 활동 로그**로 대여/상환/연체 이벤트 자동 기록 → 추적 가능성 강화  
- **커서 기반 페이지네이션**으로 대량 데이터에서도 성능 안정화 및 무한 스크롤 UX 구현  
- Swagger 기반 API 문서화로 **일관된 API 스펙 제공**  
- **JUnit5/Mockito/MockMvc 테스트 코드**로 상태 전이, 예외, 페이지네이션 등 핵심 기능 검증  
- Expo EAS Update 및 QR 시연으로 **모바일 앱 배포 실현**

### 🧠 기술적 성장

- **Spring Boot + MyBatis + Spring Security + JWT 보안 구조 실전 적용**  
  - Access/Refresh 토큰 발급/검증, SecurityContext 주입, 화이트리스트 처리까지 직접 구현  
- **전역 예외 처리 설계 경험**  
  - `@RestControllerAdvice` 기반 글로벌 핸들러 적용, 커스텀 예외 정의 및 일관된 에러 응답 반환  
- **AOP(관점 지향 프로그래밍) 활용**  
  - `ActivityLogAspect`로 대여/상환/연체 이벤트를 자동 기록하고 타임라인 UI와 연계  
- **React Native + Expo** 기반 모바일 클라이언트와 백엔드 API 연동 경험 축적  
- **AWS S3 업로드/삭제 유틸화** 및 DB 롤백 처리로 파일·데이터 정합성 문제 해결  
- **Spring Scheduler**로 자동 연체 전환 처리 → 실서비스 시나리오를 반영한 운영 로직 설계  
- **테스트 코드 기반 품질 관리**: 단위/통합 테스트 작성으로 안정성 확보, 잠재 오류 조기 차단  
- 로컬 개발을 넘어 **AWS 전체 배포/운영 직접 경험**, 단순 구현을 넘어 **실서비스 수준 프로젝트 완성**


