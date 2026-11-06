# YCAD — 모바일 DWG/DXF CAD 도면 편집기 & GitHub APK 자동 빌드 프로젝트

첨부해주신 **CAD Pockets 사용 매뉴얼 (1~7장 전체 도구)** 사양을 그대로 구현하고, **DWG 및 DXF 도면 열기/편집/저장**을 지원하며, **GitHub Actions를 통해 클라우드에서 즉시 `YCAD.apk` 안드로이드 설치 파일을 생성**하는 완성형 프로젝트입니다.

---

## 1. 구현된 매뉴얼 전체 기능 (1~7장)

1. **1) 그리기도구 (Drawing Tools)**
   - `1) Circle` (원: 중심점 + 반지름 드래그 또는 정확한 반지름 값 입력 후 확인 `✓`)
   - `2) Polyline` (연속 폴리라인 및 수치 입력)
   - `3) Line` (연속 선 그리기 및 길이/각도 정밀 입력)
   - `4) Rectangle` (직사각형 및 가로/세로 정밀 입력)
   - `5) Arc` (3점 원호 그리기)
   - `6) Text` (도면 문자 입력 대화상자)
   - `7) Insert Block` (오른쪽 슬라이딩 블록 패널, 1:1 눈금 삽입 및 블록 속성 Attribute 팝업 입력)
2. **2) 측정도구 (Measurement & Dimension Tools)**
   - `1) Aligned Dimension` (객체 정렬 치수)
   - `2) Radial Dimension` (원/호 반지름 치수)
   - `3) Angular Dimension` (3점 각도 치수)
   - `4) Linear Dimension` (수평/수직 선형 치수)
   - `5) Arc Length Dimension` (호 길이 치수)
   - `6) Ordinate Dimension` (X/Y 세로 좌표 치수)
   - `7) Area` (다각형 면적 및 둘레 실시간 측정)
   - `8,9) Distance` (두 점 사이 길이 및 각도 측정 + **길게 터치 시 정밀 스냅 돋보기(Loupe)** 표시)
3. **3) 표시도구 (Markup Tools)**
   - `1) Rectangle` (측면 길이/너비 프롬프트 사각형 주석)
   - `2) CLOUD` (구름형 리비전 수정 기호)
   - `3) SmartPen` (자유 스케치를 표준 원/직사각형/직선으로 자동 보정)
   - `4) Text` (주석 텍스트 상자)
   - `5) Brush` (자유선 브러시)
   - `6) Image` (앨범/카메라 사진 도면 삽입)
   - `7) Voice` (지점/영역 음성 녹음 객체 첨부 및 클릭 재생)
4. **4) 레이어도구 (Layering Tools)**
   - `1) New Layer (+)` 새 레이어 생성
   - `2) Rename Layer (AB|)` 레이어 이름 변경
   - `3) Change Current Layer` 더블 탭으로 현재 작업 레이어 전환
   - `4) Turn Layer On/Off` 전구 아이콘으로 레이어 켜기/끄기
   - `5) Change Layer Color` 레이어 색상 블록 변경
   - `6) Delete Layer (X)` 레이어 삭제
5. **5) 보기모드 설정, 모형도구 (View & Modeling Tools)**
   - `1) 2D` 와이어프레임 뷰 (팬 이동 / 줌 확대·축소)
   - `2) 3D` 와이어프레임 & 3D 솔리드 모드 (10가지 표준 3D 시각 프리셋 + 홈 버튼 + 피벗 회전, 3D 모드 시 편집 자동 잠금)
   - `3) Zoom Extents` (풀 익스텐드 전체 도면 보기)
   - `4) Regen` (리젠 화면 좌표 재계산)
   - `5) Change Background Color` (작업 배경색 전환)
   - `6) Modeling Tools` (Model 공간 ↔ Layout 배치 공간 전환)
6. **6) 편집도구 (Editing Tools)**
   - `Grip Edit` (객체 선택 시 파란색 그립점 직접 드래그 편집)
   - `Block Attribute Edit` (속성 블록 값 수정 인터페이스)
   - `1) Edit Text`, `2) Move`, `3) Copy`, `4) Rotate`, `5) Scale`, `6) Erase`, `7) Deselect`, `8) Mirror`, `9) Align (4점 교차 정렬)`
7. **7) 편집, 저장, 설정 (Output, Save & Settings)**
   - `1) Redo/Undo`, `2) Coloring Tools`, `3) Output (Email 공유, Export to JPEG, Export to DWF/PDF 플롯 설정)`, `4) Search in Text (도면 내 텍스트 검색)`, `5) Display the whole drawing`, `6) Full Screen (전체화면 전환 및 복귀)`, `7) Save / Save As (.DXF & .DWG 저장)`

---

## 2. GitHub로 `YCAD.apk` 만드는 방법 (3분 완성)

1. 본 압축 파일(`YCAD-Android-APK.zip`)의 압축을 풉니다.
2. [GitHub](https://github.com/new)에서 새 저장소(예: `YCAD`)를 생성합니다.
3. 압축 푼 파일들을 저장소에 업로드(Push)합니다:
   ```bash
   git init
   git add .
   git commit -m "Initial YCAD Mobile CAD App"
   git branch -M main
   git remote add origin https://github.com/<본인_깃허브_아이디>/YCAD.git
   git push -u origin main
   ```
4. GitHub 저장소 상단 메뉴의 **Actions** 탭을 클릭합니다.
5. `Build YCAD Android APK` 워크플로우가 자동으로 실행되며, 완료 후 하단 **Artifacts** 섹션에서 **`YCAD-Android-APK` (`YCAD-debug.apk`)** 파일을 다운로드하여 안드로이드 스마트폰에 바로 설치하시면 됩니다.
