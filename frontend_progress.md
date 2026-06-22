# Frontend Progress

Role: short operational tracker for the Android/Figma frontend work. The long-lived source of truth remains `context_deseign.md`.

## Current Phase

- Phase 0 - Stabilisation memoire et tokens: DONE on 2026-05-25.

## Done

- Added Figma-aligned color tokens in `ELearningColors`:
  - `BrandBlue = #2954C8`
  - `BrandBlueDark = #1E3A8A`
  - `AppBackground = #FAFAFC`
  - `CardSurface = #FFFFFF`
  - text and subtle border tokens for Figma-derived screens.
- Added Figma layout tokens:
  - `FigmaSpacing.pageHorizontal`
  - `FigmaSpacing.sectionGap`
  - `FigmaSpacing.cardPadding`
  - `FigmaSpacing.heroPadding`
  - `FigmaSpacing.itemGap`
  - `FigmaSpacing.rowGap`
  - `Radius.search`
  - `Radius.dialog`
- Implemented Phase 2 Home:
  - `HomeScreen`
  - `HomeHeader`
  - `SearchFilterBar`
  - `LearningHeroBanner`
  - `CategoryShortcut`
  - `RecommendedCourseCard`
  - `ResumeLearningCard`
- Implemented Phase 3 Mes formations:
  - `MyTrainingsScreen`
  - `MyTrainingCard`
  - `TrainingModuleAccordion`
  - `TrainingSessionRow`
- Implemented Phase 4 enriched player:
  - compact header
  - Media3 video in a 16:9 area
  - lesson metadata
  - tabs for lessons, files, and Q&A
  - curriculum accordions
- Updated authenticated shell to start at `home` and expose five main tabs:
  - `home`
  - `my_trainings`
  - `favorites`
  - `certificates`
  - `profile`
- Implemented Phase 5 screens:
  - `FavoritesScreen` reuses `FormationCard` and exposes loading/empty/error/content states.
  - `CertificatesScreen` reuses `CertificateBadge` and exposes loading/empty/error/content states.
  - `NotificationsScreen` uses `AppNotification` and exposes loading/empty/error/content states.
- Phase 6 cleanup:
  - Added `MainRoutes` for the authenticated graph.
  - Aligned duplicated `FORMATION_DETAIL` route to `formation_detail/{formationId}`.
  - Replaced placeholder routes for favorites/certificates/notifications.
  - Added critical Compose instrumented tests for Phase 5 empty states.
- Executed prompt tasks 6.1/6.2/6.3:
  - Audited real frontend state and documented it in `prompt_final_android.md`.
  - Added Figma vs Android synchronization matrix in `prompt_final_android.md`.
  - Added `FigmaTokensPreview` in `presentation/theme/Theme.kt`.
- Executed prompt tasks 6.4/6.5:
  - Confirmed 5-item shell navigation is implemented and documented final routes.
  - Confirmed bottom nav/rail is hidden on secondary routes.
  - Confirmed HomeScreen components, callbacks, preview, and Figma tokens.
  - Replaced resume-learning random UUID navigation with a stable local sample seance id.
- Executed prompt tasks 6.6/6.7/6.8:
  - Confirmed MyTrainingsScreen structure, components, preview, and player navigation.
  - Marked the first accessible planned session as current instead of neutral.
  - Confirmed SeancePlayerScreen keeps Media3/ExoPlayer and progress saving.
  - Replaced player files mock content with an empty state fallback while backend files API is absent.
  - Kept new screen-specific components local instead of adding global component duplicates.
- Executed prompt tasks 6.9/6.10/6.11/6.12:
  - Reduced route duplication: `Routes` is now root/auth only, `MainRoutes` owns app graph routes.
  - Added route helper checks for player/quiz deeplinks.
  - Added UI consistency audit findings to `prompt_final_android.md`.
  - Centered app content on medium/expanded widths with a 600dp max width.
  - Added active nav icon scale and progress animations for Home/MyTrainings.
- Executed prompt task 6.13:
  - Android screenshot capture blocked: no connected device/emulator and no AVD listed.
  - Figma screenshot capture blocked: `node_modules` is absent from the Figma export folder.
  - Completed static pixel check for Home, MyTrainings, Player, and Root shell.
  - Added `Radius.item` and `Radius.panel`.
  - Made MyTrainings header sticky.
  - Aligned player card/lesson radii with Figma.
- Removed production mock/fake data paths on 2026-05-27:
  - Replaced `QuizRepository -> MockQuizRepositoryImpl` with real Retrofit-backed `QuizRepositoryImpl`.
  - Deleted `MockFormationRepositoryImpl`, `MockSeanceRepositoryImpl`, and `MockQuizRepositoryImpl`.
  - Added empty-state repositories for unavailable backend contracts: favorites, certificates, notifications.
  - Home no longer navigates to a hardcoded resume seance and now uses real recommended formations.
  - MyTrainings, Certificates, Notifications, and Player now render loading/empty/error or real API-backed state only.
  - Removed production `sample*`, `mock*`, test UUID literals, and `UUID.randomUUID()` usage from `src/main`.
- Completed media metadata pipeline fixes on 2026-05-28:
  - Backend formation responses now expose signed `coverImageUrl`/`thumbnailUrl`, `totalDuration`, `durationHours`, and counts alongside MinIO keys.
  - Backend seance responses now include `description`; resource DTOs expose file-style aliases for Android.
  - Android formation DTO/mapping now prefers real image URLs and no longer treats MinIO keys as Coil URLs.
  - Android course/seance DTOs tolerate absent nested `seances` and nullable metadata from current backend responses.
  - Android player now loads `/api/v1/seances/{id}/resources` and opens resource download URLs from `/api/v1/resources/{id}/download-url`.
- Added dynamic LMS API contracts and UI bindings on 2026-05-30:
  - Backend added `GET /api/v1/users/me/profile` with user-specific stats: hours spent, completed courses, completed formations, enrolled formations, certificates.
  - Backend added `GET /api/v1/formations/enrolled`, `GET /api/v1/categories`, `GET /api/v1/formations?categoryId=...`, and certificate DTO responses with MinIO presigned download URLs.
  - Android added `ProfileRepository`, `ProfileViewModel`, and a dynamic Figma-token profile screen.
  - Android `MyTrainingsViewModel` now observes Room enrolled formations through `Flow`; enrollment success upserts the real formation into Room.
  - Android Home now loads backend categories, shows the Figma-style categories bottom sheet, and navigates to a filtered catalogue.
  - Android Catalogue now supports debounced search plus level and category filters sent to `ResourceApiService.getFormations(...)`.
  - Android Certificates now consumes the real certificates API and opens the returned PDF URL.

## Convention

- New screens derived from the Figma export should use the Figma-aligned tokens above.
- Existing screens should not be mass-migrated until their dedicated phase.
- The legacy Material3 palette remains active to avoid regressions.

## Not Done Yet

- Backend replacement for `Mock*RepositoryImpl`: DONE for existing repository bindings. Favorites, certificates, notifications, scanner presence, resume-learning, and player curriculum still need backend endpoints; Android now returns empty/error states instead of fake content.
- Pixel-perfect validation still needs screenshots from emulator/device and the Figma React export.
- Favorites, Certificates, Notifications, player curriculum, Q&A, scanner presence, and resume-learning are empty/error fallback states until backend contracts exist.
- Player files are now connected to the seance resources API; they still show an empty state when the backend returns no resources.
- Route slide-in player animation is intentionally not implemented until screenshot/QA validation.
- Pixel-perfect status is `PIXEL_PENDING` until Android and Figma screenshots can be captured.

## Next Recommended Task

- Add real ViewModels/repositories for Home, MyTrainings, Favorites, Certificates, and Notifications when backend contracts are available.
- Run screenshot/pixel validation against the Figma export.

## Validation

- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-25.
- Final validation after warning cleanup: SUCCESS on 2026-05-25.
- `./gradlew.bat :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`: SUCCESS on 2026-05-26.
- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-26 after `FigmaTokensPreview`.
- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-26 after prompt 6.4/6.5 updates.
- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-26 after prompt 6.6/6.7/6.8 updates.
- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-26 after prompt 6.9/6.10/6.11/6.12 updates.
- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-26 after prompt 6.13 static pixel corrections.
- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-27 after removing mock repositories and fake runtime data.
- `./gradlew.bat clean installDebug`: SUCCESS on 2026-05-27; installed on Pixel_7 AVD.
- `./gradlew :app:compileDebugKotlin`: SUCCESS on 2026-05-28 after media metadata/resource pipeline fixes.
- `./gradlew.bat :app:compileDebugKotlin`: SUCCESS on 2026-05-30 after dynamic profile/enrollment/categories/catalogue/certificates contracts.
- `docker compose build resource-server`: SUCCESS on 2026-05-30 after backend API contract additions.
- `docker compose up -d resource-server`: SUCCESS on 2026-05-30; `resource-server` healthy after runtime JPQL/controller validation.
