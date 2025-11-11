usecase
- 유스케이스 계층
- "이 기능을 어떻게 수행할지" 정의
- Controller -> UseCase -> Port -> Adapter -> DB/Redis/Kafka 호출



port
- UseCase와 외부 시스템 간 인터페이스
- "이 기능이 외부 시스템을 어떻게 사용해야 하는지" 정의

in
- Adapter(in)가 호출하는 인터페이스

out
- Adapter(out)에서 구현할 인터페이스




impl
- 실제 유스케이스 구현체
- port를 주입받아 로직 수행