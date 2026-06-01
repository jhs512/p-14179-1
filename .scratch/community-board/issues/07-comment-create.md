# 댓글 작성

Status: ready-for-agent

## Parent

`.scratch/community-board/PRD.md`

## What to build

로그인 회원이 글에 최상위 댓글을 다는 흐름. `Comment` 엔티티(작성자·소속 Post·내용·부모 Comment(nullable)·deleted)를 정의하고, 글 상세에서 댓글을 작성하면 저장 후 상세에 노출된다. 이 슬라이스는 최상위 댓글만(부모 null) — 대댓글은 슬라이스 8. 소프트 삭제된 댓글은 상세에 노출되지 않는다.

## Acceptance criteria

- [ ] `Comment` 엔티티가 `BaseEntity` 상속, `parent`(nullable)·`deleted`를 가진다
- [ ] 로그인 회원이 글 상세에서 댓글을 작성하면 저장·노출된다
- [ ] 빈 댓글은 Validation으로 거부된다
- [ ] 미인증 댓글 작성은 로그인으로 리다이렉트된다
- [ ] 소프트 삭제된 댓글은 상세에 노출되지 않는다
- [ ] MockMvc 테스트: 댓글 작성·노출, 미인증 리다이렉트를 검증한다

## Blocked by

- 슬라이스 5 (글 작성)
