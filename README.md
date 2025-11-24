<img width="1758" height="303" alt="Group 144" src="https://github.com/user-attachments/assets/bb4b0055-a3d4-415c-8855-b1114ed4276b" />

<br />

# 🧩 UniBooker Back-end

<br>

## 📘 프로젝트 소개

**UniBooker**는 대규모 예약 신청을 안정적으로 처리하기 위해 설계된 **B2B 클라우드 예약 관리 서비스**입니다. <br/>
기업이나 기관은 별도의 개발 과정 없이 **가입만으로 바로 도입**할 수 있으며,
시설·공간·이벤트 등 다양한 리소스를 손쉽게 등록하고 운영할 수 있습니다.

교육, 헬스케어, 공공기관, 기업 행사 등 여러 산업에서 바로 적용할 수 있도록 설계되었으며,
각 기업의 정책과 운영 방식에 맞춰 **유연하게 규칙을 설정하고 확장 가능한 구조**를 제공하여
기존처럼 각 기업이 개별 시스템을 구축·운영해야 하는 부담을 크게 줄여줍니다.
<br><br><br>

## 👥 팀원 소개
<div align="center">

| <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRKQRnAMmRWI273hiziEH3SDGEIPshU-cRblQ&s" width="100" height="100"/> | <img src="https://i.pinimg.com/236x/c8/5f/d7/c85fd7277915c240c352e0f7b8495d8c.jpg" width="100" height="100"/> | <img src="https://image.zeta-ai.io/profile-image/8f2165b4-df11-41d4-babb-aa27e1f1a420/d470174d-7e96-44c5-adcc-3463975a7146.jpeg?w=3840&q=90&f=webp" width="100" height="100"/> | <img src="https://blog.kakaocdn.net/dna/ddapbS/btsH5PuRPbZ/AAAAAAAAAAAAAAAAAAAAACDpoGlbGghqNR4aWLq9C2NaIXAf5bF6blKSGob_YDBP/img.png?credential=yqXZFxpELC7KVnFOS48ylbz2pIh7yKj8&expires=1764514799&allow_ip=&allow_referer=&signature=i6S%2F%2B8MiAs%2FNXDXkxvCBb8U%2BFco%3D" width="100" height="100"/> | <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCFhW2il5BFeGVFQH1X6llq9_zOstgaADyuQ&s" width="100" height="100"/> |
| :-------------------------------------------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------------: | :-----------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------: |
|                              🍀 **김아영**<br/>[@thay123028](https://github.com/thay123028)                              |                  🍀 **유현경**<br/>[@gaangstar](https://github.com/gaangstar)                 |                    🍀 **윤소민**<br/>[@somminn](https://github.com/somminn)                    |                               🍀 **허정우**<br/>[@JohnHeo81](https://github.com/JohnHeo81)                               |                               🍀 **홍서연**<br/>[@seoyeon22](https://github.com/seoyeon22)                               |

</div>
<br><br>

## 📘 프로젝트 개요
### 🔍 배경

기업·기관의 예약 시스템은 특정 시점에 이용자가 급증하는 구조를 가지고 있습니다.<br/>
수강신청, 티켓팅, 병원 예약, 행정 서비스 등에서는 수천 명이 동시에 접속하는 상황이 반복되지만 이러한 트래픽을 감당하지 못해 아래와 같은 문제가 발생하고 있습니다.

* 동시 접속 폭주로 인한 응답 지연, 서버 다운, 중복 예약

* 잔여석·취소·성공 여부를 실시간으로 확인하기 어려움

* 기업별로 시스템을 따로 구축해야 하므로 비용·운영 인력 부담 증가

* 특정 산업에 맞춘 폐쇄적 구조로 범용성이 부족

이는 운영 효율 저하, 공정성 문제, 민원 증가 등으로 이어질 수 있습니다.

<br/>

### 🧩 솔루션

저희는 위 문제를 해결하기 위해
대규모 예약 신청을 안정적으로 처리할 수 있는 B2B 클라우드 예약 관리 서비스를 목표로 하였습니다.

#### 1) 대규모 트래픽 대응

* 대기열 시스템으로 폭주 트래픽 제어

* Redis 분산 락으로 중복 예약 방지

* MSA 구조로 특정 기능 장애가 전체 서비스에 영향을 주지 않도록 설계

#### 2) 실시간 운영 대시보드 제공

* 잔여석, 성과, 예약수, 취소수 현황을 실시간으로 모니터링

* 운영자가 즉각적으로 문제를 파악하고 대응 가능

####  3) 다양한 산업에서 즉시 사용 가능

* 시설·공간·이벤트 등 다양한 리소스를 기업이 직접 등록

* 정원·시간대·예약 규칙을 관리자가 직접 설정할 수 있는 유연한 구조

#### 4) 시스템 구축/운영 부담 감소

* 클라우드 SaaS 기반으로 별도 개발·서버 구축 필요 없음

* 유지보수 비용 절감 및 운영 효율 향상

<br/>


## 🌟 주요 기능
### 🍀 리소스(예약 서비스) 관리

* 시설 · 공간 · 장비 등 다양한 자원을 그룹별로 구조화하여 관리
* 리소스별 운영 시간, 예약 가능 조건, 가격 정책 설정
* 예약 가능한 리소스(시설·공간·이벤트 등)의 등록, 수정, 삭제
* 리소스 그룹 단위 카테고리 관리 및 필터링
* 리소스 그룹 및 리소스 통계 제공
* 특정 날짜/시간에 예약을 막는 예외 시간대 설정
* 반복 예외, 일회성 예외, 공휴일 적용 등 고급 설정 지원

### 🍀 예약 관리

* 사용자 예약 생성 · 취소 기능
* 관리자가 예약 현황을 한눈에 보는 관리자 대시보드 제공
* 시간 단위/기간 단위 예약 지원
* 예약 겹침·동시성 제어로 안정적인 예약 처리

### 🍀 대기열 관리

* 대규모 트래픽 상황에서도 안정적인 예약 요청 처리
* 대기열 진입·종료 현황 및 우선순위 관리

### 🍀 통계 및 리포트

* 누적 예약/취소 건수
* 리소스 그룹별 예약 수 통계
* 시간대별 이용량 조회
  <br><br>

## 🌐 접속 주소

### [플랫폼 관리자 바로가기](https://www.unibooker.kro.kr/super/login)

- ID : super@unibooker.com
- PW : super1234

### [기업 관리자 바로가기](https://www.unibooker.kro.kr/admin/home)

- ID : admin@unibooker.com
- PW : Lqwer1234!

### [고객 바로가기](https://www.unibooker.kro.kr/c/hanwha-systems)

- ID : test111@test.com
- PW : test1234!


<br><br>

## 🧰 기술 스택

<div>
  <img src="https://img.shields.io/badge/vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white">
  <img src="https://img.shields.io/badge/Pinia-FFE801?style=for-the-badge">
  <img src="https://img.shields.io/badge/tailwindcss-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white">
  <img src="https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">
</div>
<div>
  <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white">
  <img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white">
</div>
<br><br>

## 🏗️ 시스템 아키텍처 [🔗](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/3.-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98)
![3. 시스템아키텍처](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/blob/develop/docs/3.%20%EC%8B%9C%EC%8A%A4%ED%85%9C%20%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98.png)

<br><br>

## 🛢️ ERD [🔗](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/5.-ERD)
![5. ERD](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/blob/develop/docs/5.%20ERD.png)

<br><br>

## 🖥 화면 설계서
> [Figma 보러가기](https://www.figma.com/design/7xSlI165fAoEO6CYpWqIqZ/UniBooker-%ED%99%94%EB%A9%B4-%EC%84%A4%EA%B3%84%EC%84%9C?node-id=1125-3384&m=dev&t=u8xcRCOC6QbjigES-1)

<br><br>

## 📺 UI/UX 기능 테스트
> [UI/UX 기능 테스트 보러가기](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/8.-%ED%94%84%EB%A1%A0%ED%8A%B8-UI-UX-%EA%B8%B0%EB%8A%A5-%ED%85%8C%EC%8A%A4%ED%8A%B8)

<br><br>

## 📄 프로젝트 상세 문서
#### 📌 프로젝트 기획서
> [프로젝트 기획서 보러가기](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/1.-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EA%B8%B0%ED%9A%8D%EC%84%9C)
#### 📌 요구사항 정의서
> [요구사항 정의서 보러가기](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/2.-%EC%9A%94%EA%B5%AC%EC%82%AC%ED%95%AD-%EC%A0%95%EC%9D%98%EC%84%9C)
#### 📌 WBS
> [WBS 보러가기](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/4.-WBS)
#### 📌 CI/CD 계획서
> [CI/CD 계획서 보러가기](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/9.-CI-CD-%EA%B3%84%ED%9A%8D%EC%84%9C)
#### 📌 통합 테스트 결과서
>[통합 테스트 결과서 보러가기](https://github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-FE/wiki/%ED%86%B5%ED%95%A9-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EA%B2%B0%EA%B3%BC%EC%84%9C)