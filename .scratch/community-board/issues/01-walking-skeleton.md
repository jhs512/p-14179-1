# 워킹 스켈레톤

Status: ready-for-agent

## Parent

`.scratch/community-board/PRD.md`

## What to build

자프링 프로젝트의 걷는 뼈대를 세운다. `back/` 폴더에 Spring Boot 4.0.6 / JDK 25 / Gradle Kotlin DSL로 스캐폴딩하고, 홈 페이지가 떠서 공통 레이아웃(타임리프 + layout-dialect, 테일윈드 4 Play CDN, DaisyUI 5 CDN, Pretendard 다이나믹 서브셋)이 적용된 화면을 브라우저로 확인할 수 있게 한다. 보안·영속 계층의 골격(Spring Security 기본 설정, H2 + JPA, `BaseEntity` 감사 필드)도 이 슬라이스에서 함께 깐다.

- 루트 패키지 `com.back`, 메인 클래스 `com.back.BackApplication`에 `@EnableJpaAuditing`.
- OSIV 끄기, 트랜잭션은 액션 메서드 레벨 `@Transactional` 원칙.
- 프로파일: dev(`application.yml`+`application-dev.yml`, 파일 DB `./db_dev.mv.db`, ddl-auto update, H2 콘솔), test(`application.yml`+`application-test.yml`, H2 in-memory, ddl-auto create).
- `BaseEntity`: `id` + 생성일시·수정일시 감사 필드, 모든 엔티티가 상속.
- Spring Security: 일단 홈·정적 리소스는 공개, 폼 로그인 골격만(실제 로그인은 슬라이스 3).

## Acceptance criteria

- [ ] `back/`에서 앱이 dev 프로파일로 기동되고 홈 페이지가 200으로 렌더링된다
- [ ] 공통 레이아웃(테일윈드·DaisyUI·Pretendard)이 적용된 화면이 보인다
- [ ] H2 콘솔 접근 가능, dev는 파일 DB·test는 in-memory로 분리된다
- [ ] OSIV가 꺼져 있다(`open-in-view: false`)
- [ ] `BaseEntity`가 존재하고 감사 필드가 자동 채워진다
- [ ] test 프로파일에서 `@SpringBootTest` + `MockMvc`로 홈 200 응답을 검증하는 테스트가 통과한다

## Blocked by

None - can start immediately
