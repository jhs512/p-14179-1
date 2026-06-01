# 댓글·대댓글 수정·삭제 (소프트 삭제·권한)

Status: ready-for-agent

## Parent

`.scratch/community-board/PRD.md`

## What to build

댓글·대댓글의 수정·삭제. 작성자 본인 또는 `ADMIN`만 가능하다. 수정은 내용을 갱신한다. 삭제는 `deleted=true` 소프트 삭제(ADR-0001)로, 글과 동일 규칙 — 해당 댓글만 숨기고 그 대댓글은 유지된다(부모 댓글이 삭제돼도 대댓글은 남음). 권한 없는 회원의 시도는 거부된다.

## Acceptance criteria

- [ ] 작성자 본인이 댓글·대댓글을 수정하면 내용이 갱신된다
- [ ] `ADMIN`은 임의의 댓글·대댓글을 수정·삭제할 수 있다
- [ ] 작성자/ADMIN이 아닌 회원의 수정·삭제는 거부된다
- [ ] 삭제는 `deleted=true`로 처리되고, 삭제된 댓글의 대댓글은 유지된다
- [ ] 삭제된 댓글은 상세에 노출되지 않는다
- [ ] MockMvc 테스트: 본인/ADMIN 수정·삭제, 타인 거부, 부모 삭제 시 대댓글 유지를 검증한다

## Blocked by

- 슬라이스 8 (대댓글)
