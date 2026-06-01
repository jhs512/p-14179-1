# 내 정보 수정

Status: ready-for-agent

## Parent

`.scratch/community-board/PRD.md`

## What to build

로그인 회원의 내 정보 페이지. username(로그인 ID, 고정·읽기전용)을 확인하고 nickname과 password를 수정할 수 있다. password 수정 시 해시로 재저장된다. 미인증은 로그인으로 리다이렉트.

## Acceptance criteria

- [ ] 내 정보 페이지에 현재 username(읽기전용)·nickname이 표시된다
- [ ] nickname 수정이 반영된다
- [ ] password 수정 시 해시로 재저장되고 새 비밀번호로 로그인된다
- [ ] username은 변경할 수 없다
- [ ] 미인증 접근은 로그인으로 리다이렉트된다
- [ ] MockMvc 테스트: nickname 변경, password 변경 후 재로그인, username 불변, 미인증 리다이렉트를 검증한다

## Blocked by

- 슬라이스 3 (로그인/로그아웃)
