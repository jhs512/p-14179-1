# 작업지침
- 한국어 사용
- 일반적인 작업 흐름 : /grill-with-docs, /to-spec, /to-tickets, /implement
  - /implement 는 내부적으로 /tdd 로 구현하고 커밋 전에 /code-review 를 수행
- 버그 발생 시 /diagnosing-bugs, 아키텍처 개선이 필요할 때만 /improve-codebase-architecture
- 최대한 mattpocock 스킬들을 활용

# Agent skills

## Issue tracker

이슈/PRD는 `.scratch/<feature>/` 아래 마크다운 파일로 관리. 참조: `docs/agents/issue-tracker.md`.

## Triage labels

기본 라벨 어휘 사용(`needs-triage` / `needs-info` / `ready-for-agent` / `ready-for-human` / `wontfix`). 참조: `docs/agents/triage-labels.md`.

## Domain docs

단일 컨텍스트(루트 `CONTEXT.md` + `docs/adr/`). 참조: `docs/agents/domain.md`.