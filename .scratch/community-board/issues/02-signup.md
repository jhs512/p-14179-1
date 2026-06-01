# 회원가입

Status: ready-for-agent

## Parent

`.scratch/community-board/PRD.md`

## What to build

비회원이 `username`·`password`·`nickname`으로 가입하는 end-to-end 흐름. 가입 폼 페이지 → 폼 제출(POST) → 검증 → 비밀번호 해시 후 `Member` 저장(role=`USER`) → 완료 후 로그인 페이지로 리다이렉트. `username`은 유니크(중복 시 거부), 비밀번호는 인코더로 해시 저장(평문 금지).

## Acceptance criteria

- [ ] 회원가입 폼 페이지가 렌더링된다
- [ ] 유효한 입력으로 가입하면 `Member`가 저장되고 비밀번호는 해시로 저장된다
- [ ] 새 회원의 role은 `USER`다
- [ ] 중복 `username`으로 가입 시 거부되고 에러가 표시된다
- [ ] 빈 값/형식 오류는 Validation으로 거부된다
- [ ] MockMvc 테스트: 정상 가입(저장 확인), 중복 username 거부, 검증 실패를 검증한다

## Blocked by

- 슬라이스 1 (워킹 스켈레톤)
