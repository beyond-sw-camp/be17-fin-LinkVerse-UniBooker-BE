test
test

adapter


역할
- 외부와 통신하는 계층
- 요청을 받거나 보낼 때 사용
- UseCase의 port와 실제 외부 시스템 연결을 담당



in
- 외부 요청을 받아서 UseCase 호출
- 예 : REST API, Kafka Consumer, WebSocket
- 예시 : 사용자가 리소스 생성 요청 -> Controller -> UseCase 호출

out 
- 외부 시스템 호출
- DB, Redis, Kafka, 다른 마이크로서비스 호출 담당
- 덕분에 UseCase가 직접 외부 시스템을 몰라도 됩니다.
- 예시 : UseCase가 리소스 저장 -> RepositoryAdapter가 DB에 저장