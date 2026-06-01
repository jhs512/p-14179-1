# 글 목록·상세 (읽기)

Status: ready-for-agent

## Parent

`.scratch/community-board/PRD.md`

## What to build

글의 읽기 경로. `Post` 엔티티(작성자·제목·내용·viewCount·deleted)를 정의하고, 글 목록 페이지는 작성일 역순(최신순)으로 Spring Data `Pageable`을 사용해 페이지 단위(예: 10개)로 표시한다. 글 상세 페이지는 제목·내용·작성자·추천수 자리를 보여준다. 소프트 삭제된 글(`deleted=true`)은 목록·상세 어디에도 노출되지 않는다(ADR-0001). 이 슬라이스는 읽기만 — 작성/수정/삭제/조회수 증가는 후속.

## Acceptance criteria

- [ ] `Post` 엔티티가 `BaseEntity`를 상속하고 `deleted` 플래그를 가진다
- [ ] 글 목록이 최신순·페이지 단위로 렌더링된다
- [ ] 글 상세가 렌더링된다
- [ ] `deleted=true`인 글은 목록·상세에서 보이지 않는다(상세 직접 접근 시 404 또는 미노출)
- [ ] 비로그인도 목록·상세 열람 가능
- [ ] MockMvc 테스트: 최신순·페이징, 삭제글 비노출을 검증한다

## Blocked by

- 슬라이스 1 (워킹 스켈레톤)
