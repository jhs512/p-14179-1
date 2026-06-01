# 초기 샘플 데이터 (BaseInitData)

Status: ready-for-agent

## Parent

`.scratch/community-board/PRD.md`

## What to build

`com.back.global.initData.BaseInitData`에 `baseInitDataApplicationRunner` 빈(`@Transactional`)을 만들어 앱 기동 시 샘플 데이터를 시드한다. 회원이 1명이라도 존재하면 시드 로직을 중단한다(중복 시드 방지). 비어 있으면 회원 5명(최소 1명은 `ADMIN`), 글 5개, 댓글 5개를 생성한다. 비밀번호는 해시로 저장한다.

## Acceptance criteria

- [ ] 빈 DB로 기동 시 회원 5명·글 5개·댓글 5개가 생성된다
- [ ] 회원 중 최소 1명의 role이 `ADMIN`이다
- [ ] 회원이 이미 존재하면 시드 로직이 중단되어 중복 생성되지 않는다
- [ ] 시드 회원의 비밀번호가 해시로 저장된다
- [ ] 재기동해도 데이터가 중복 누적되지 않는다
- [ ] 테스트: 빈 상태 시드 생성, 기존 회원 존재 시 미시드를 검증한다

## Blocked by

- 슬라이스 8 (대댓글) — Member·Post·Comment 엔티티 완성 필요
