# AkaImage

**AkaImage** is a Minecraft mod that lets you display images from URLs on in-game frame blocks.  
**AkaImage**는 URL로 이미지를 불러와 게임 내 액자 블록에 표시할 수 있는 마인크래프트 모드입니다.

---

## Requirements / 요구사항

- Minecraft 1.21.1
- NeoForge
- [AkaCore]((https://github.com/aka-hub/AkaCore)

---

## Features / 기능

- Place an image frame block and display any image from a URL
- Right-click to open GUI — manage frames, images, and size in one screen
- Register image aliases and reuse them across multiple frames
- Image and frame data persists across server restarts
- Supports block facing direction

- URL 이미지를 액자 블록에 실시간으로 표시
- 우클릭 GUI에서 액자 설정, 이미지 목록 관리, 크기 조절을 한 화면에서
- 이미지 alias를 등록해 여러 액자에서 재사용 가능
- 서버 재시작 후에도 데이터 유지
- 블록 방향(FACING) 지원

---

## Usage / 사용법

### GUI
Right-click the frame block to open the settings screen.  
액자 블록을 우클릭하면 설정 화면이 열립니다.

**Left panel / 왼쪽 패널 — 액자 설정**
| Field | Description |
|-------|-------------|
| Frame ID | 이 액자의 고유 ID |
| 선택된 이미지 | 오른쪽 목록에서 클릭해서 선택 |
| 너비 / 높이 | 렌더링 크기 |
| 적용 | 설정 저장 및 적용 |

**Right panel / 오른쪽 패널 — 이미지 관리**
| Feature | Description |
|---------|-------------|
| 이미지 목록 | 등록된 이미지 alias 목록, 클릭으로 선택 |
| 삭제 | 선택된 이미지 alias 삭제 |
| 이미지 등록 | 새 alias와 URL을 입력해서 등록 |

### Commands
```
/img register <alias> <url>
```
이미지 URL을 alias로 등록합니다.

```
/img bind <frameId>
```
바라보고 있는 액자에 Frame ID를 등록합니다.

```
/img show <frameId> <imgAlias>
```
액자에 등록된 이미지를 표시합니다.

```
/img size <frameId> <width> <height>
```
액자 크기를 변경합니다.

**Example / 예시:**
```
/img register logo https://i.imgur.com/example.png
/img bind main_screen
/img show main_screen logo
/img size main_screen 3.0 2.0
```

---

## Notes / 주의사항

- Only image URLs that return a valid image file (PNG, JPG) are supported.
- 직접 이미지 파일을 반환하는 URL만 지원됩니다. (PNG, JPG)
- Imgur, direct image links 등을 권장합니다.

---

## License

MIT
