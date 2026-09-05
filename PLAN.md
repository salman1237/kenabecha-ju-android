# KenaBecha JU — Android App: Plan & Progress

A serial, phase-by-phase build plan for a native Android client on top of the existing KenaBecha JU FastAPI backend (`../kenabecha-ju`) — same database, same business rules, no rewrite, no parallel backend. Every phase is scoped so it can be built, verified for real (compiled, installed, launched, clicked through on a real connected device — not just "looks right"), committed, and pushed before the next one starts. Every phase below is a plan until marked `(implemented)`, at which point it also carries a "what shipped" note the same way this project's web counterpart's own `PLAN.md` does.

**Repo**: `salman1237/kenabecha-ju-android` (private). **Package**: `com.kenabechaju.app`. **Stack**: Kotlin, Jetpack Compose (Material 3), Hilt, Retrofit + OkHttp + kotlinx.serialization, Navigation Compose, DataStore, Coil. `minSdk 24` / `compileSdk 37` / `targetSdk 37`, on the same toolchain (AGP 9.3.2, Kotlin 2.2.10) already proven on this machine by the team's other Android app, `BloodConnectJU-Android` — its two real AGP-9/KSP compatibility fixes and its Material3-explicit-ColorScheme lesson are carried over directly rather than rediscovered.

**No feature in the web app is out of scope for mobile**, with one deliberate, disclosed exception: the admin/moderation panel (§ "What stays web-only", below) stays a web-only surface — reasoned, not silently dropped.

---

## Phase 0 — Project scaffold, brand theme, verified on a real device (implemented)

First slice: prove the whole toolchain — Gradle, Compose, Hilt/KSP, the brand theme — actually builds and renders on real hardware before any networking or feature code exists.

- Hand-built (no Android Studio wizard available to this session) Gradle project mirroring `BloodConnectJU-Android`'s exact proven `libs.versions.toml`, minus Firebase/Pusher (added back in the phases that actually need them — this app's WebSocket is raw OkHttp, not Pusher-protocol, so that dependency never comes back at all).
- `gradle.properties` carries `android.disallowKotlinSourceSets=false` up front — AGP 9's built-in-Kotlin mode conflicts with how KSP (Hilt's annotation processor) registers generated-sources directories, and this is Gradle's own documented escape hatch, already known from the reference app rather than rediscovered by hitting the same build failure again.
- **Two real, fresh bugs found and fixed this phase, distinct from the reference app's own list**:
  1. `local.properties`'s `sdk.dir` needs Java-properties escaping (`C\:\\Users\\...`, not `C:\Users\...`) — an unescaped Windows path produced a `java.io.IOException: Invalid file path` several layers deep in Gradle's dependency-resolution internals, not a clear error pointing at the actual cause. Android Studio always writes it pre-escaped; a hand-written one has to match that by hand.
  2. XML comments containing an em dash silently round-tripped through this session's file-write path as a literal double hyphen (`--`) — which XML forbids inside a comment body at all. Found via `aapt2`'s real (and this time genuinely clear) parse error, fixed by avoiding em dashes in XML comment text project-wide.
- Brand theme (`ui/theme/`) ports the web app's actual light/dark tokens (`frontend/app/globals.css` `:root`/`.dark`) into a fully-explicit Material3 `ColorScheme` — every role set by hand (`surfaceContainer*`, `outlineVariant`, `inversePrimary`, all of them), not just the ones a first screen happens to touch, since an unset role doesn't fail loudly, it just quietly renders Material's default purple instead of the brand — exactly the bug the reference app hit and fixed the hard way. `dynamicColor` never used, for the same reason (Android 12+ would otherwise silently override the whole scheme with a wallpaper-derived one).
- **Verified for real**: `gradlew assembleDebug` succeeds clean; installed via `adb install -r` on a connected physical device (Tecno BG6); launched via `adb shell am start`; confirmed as the foreground activity via `dumpsys activity`; screenshotted showing the correct emerald→teal gradient wordmark on the correct white background — not just a successful compile.

---

## The one backend decision this whole app rests on: auth

The existing web app authenticates via httpOnly cookies (`access_token`, 15 min; `refresh_token`, 30 days, path-scoped to `/auth`) — every REST endpoint and the WebSocket handshake read the same cookie, via FastAPI's `Cookie(...)` dependency, and there is no bearer-token mode today.

**Revising the earlier guidance** (the chat-only "Android Guide" artifact from before this plan existed): that document recommended reproducing the cookie flow with an OkHttp `CookieJar`, on the reasoning that `httpOnly` is a browser-only restriction a native client can freely ignore — true, but the team's own prior Android app (`BloodConnectJU-Android`, against a Laravel/Sanctum backend) went a different, more conventional route: a small dedicated token-auth surface, not a cookie jar. That precedent is the better one to follow here too — Bearer tokens are the pattern every Android networking tutorial, every Retrofit interceptor example, and every future contributor will actually recognize, versus a native app quietly depending on browser cookie semantics it was never designed to expect.

**What actually changes on the backend (small, additive, non-breaking):**

1. `get_current_user` / `get_current_user_ws` (`app/core/dependencies.py`, `app/routers/ws.py`) accept **either** the existing cookie **or** an `Authorization: Bearer <token>` header — cookie checked first (web behavior byte-for-byte unchanged), header as a fallback. The WebSocket handshake reads the same header (OkHttp's WebSocket client can set arbitrary headers on the upgrade request, unlike a browser's native `WebSocket` constructor, which is exactly why the web app *had* to use a cookie and mobile doesn't).
2. `POST /auth/login`, `/auth/signup`'s implicit login, `/auth/refresh`, and `/auth/google` additionally return `access_token`/`refresh_token` in the **JSON body** alongside the cookies they already set — harmless for the web client, which never looks at the body for these fields, and exactly what the Android client actually consumes.
3. No new tables, no new migration beyond what Phase 5 (push tokens) needs anyway.

**Android side**: a Retrofit `Authenticator` mirrors the web app's own `lib/api/client.ts` 401-handling exactly — on a 401, fire `POST /auth/refresh` once, de-duplicated across concurrent in-flight requests via a single shared `Deferred`, then retry. Tokens live in DataStore, read through an in-memory cache so the interceptor never blocks on disk I/O per request (the same shape `BloodConnectJU-Android`'s `TokenStore` already proved out).

**Google Sign-In** needs one Google Cloud config addition, no further backend change: a second OAuth client (type **Android**, package name + signing SHA-1, no secret) in the same `kenabecha-ju-507713` project — but the ID token request itself passes `setServerClientId(WEB_CLIENT_ID)` (the existing web client, `777828703576-e69r...`), so the resulting token's audience matches what `POST /auth/google` already verifies. Confirmed by reading `auth_service.py`'s actual `verify_oauth2_token(..., audience=settings.GOOGLE_CLIENT_ID)` call directly, not assumed.

---

## Phase 1 — Auth API layer & identity screens

**Backend**: the three additive changes above, landed and tested (a handful of new backend tests: bearer-header accepted alongside cookie, body carries tokens, WS handshake accepts the header) before any Android code touches it — same discipline the web `PLAN.md` itself follows for every phase.

**Android**:
- `ApiService` (Retrofit interface) mirroring `routers/auth.py`, `routers/reference.py`, `routers/users.py` exactly — DTOs matching the real JSON field names 1:1 (`student_id`, `registration_no`, `hall_id`, `department_id`, `session`, `batch`, `profile_complete`, etc.), not renamed to "nicer" Kotlin names.
- `TokenStore` (DataStore + in-memory cache), `AuthRepository` (signup/verify-otp/login/logout/forgot-password/reset-password/google/me), `parseApiError()` decoding the API's `{detail, code}` shape into a translated message the same way `lib/i18n/errors.ts`'s `translateApiError` does client-side on web — the `code` field exists specifically so a client can localize instead of showing raw English, and Android should use it instead of just displaying `detail` verbatim.
- Screens: **Signup** (JU fields, hall/department dropdowns sourced from `GET /reference/halls` / `/departments`, batch/session), **OTP verify**, **Login**, **Forgot/reset password**, **Google Sign-In** (Credential Manager, per above), **Complete profile** gate (mirrors `get_seller`'s server-side requirement — a profile-incomplete account is routed here before it can reach anything requiring seller status, not just told "no" after the fact).
- Navigation start-destination decided by whether a token is already stored, exactly like the reference app's own cold-start check.

---

## Phase 2 — Browse, search & listings

The single largest feature surface, split into three verifiable slices:

**2a — Browse & discovery**: home/browse grid consuming `GET /listings` with the same filter shape the web `BrowseFilters` type uses (`q`, `tags`, `min_price`, `max_price`, `condition`, `category`, `sort`, pagination), category chips from `GET /categories`, trending tags, search-as-you-type against `/listings/suggestions` (debounced, matching web's 350ms).

**2b — Listing detail & contact**: full detail screen, image gallery, related listings (`/listings/{id}/related`), seller/shop info block, Contact Seller → opens a conversation (Phase 5 territory for the thread itself), save/unsave, share.

**2c — Seller-side CRUD**: create/edit listing (title, description, price/price-type, condition, category-or-custom, tags, fulfillment type, pickup address), sequential photo upload to `/listings/{id}/images` with drag-reorder (`/listings/{id}/images/reorder` — same "must be every image id, not a partial list" contract the web client already respects), and every status action the web app has: mark sold, mark out-of-stock/available, pause/reactivate, relist, manual shop-wide display ordering (`/listings/reorder`), restock-request badge count + fulfill-on-restock (buyer-initiated request, seller sees a count not a name list, exactly as designed server-side).

**Known gotcha to pre-empt, not rediscover**: the reference app's `FilterChip` selected-state contrast bug (`secondaryContainer` role rendering nearly invisible against a `Card`'s own background) is architecturally impossible here only if Phase 0's full explicit `ColorScheme` actually covers every role a `FilterChip` reads — worth a specific visual check on the first screen that puts a chip inside a `Card` (the filter sheet), rather than assuming Phase 0's fix generalizes untested.

---

## Phase 3 — Shops

- Storefront screen: branding (logo/cover/description/type), listings grid (respecting the seller's manual order, `sort=manual`), reviews tab, follow/unfollow (`POST /shops/{id}/follow`), stats (`GET /shops/{slug}/stats` — active listings, sold count, followers, rating, the `rateable_listing` prompt for a buyer with something eligible to review).
- Shop dashboard: branding edit (logo/cover upload), details edit, multiple-shops-per-seller list, per-shop "Add listing" and "Posts" entry points.
- Browse shops screen (`GET /shops`, search/sort by name/rating/listing-count).

---

## Phase 4 — Shop posts

The rich-text "post about a product again to grab attention" feature (web `Phase 52`) — genuinely the most novel rendering problem in this whole app, and deliberately **not** solved with a `WebView`:

- `description_html` is already-sanitized server-side to a *tiny* fixed tag/style set (`p br b strong i em u span` + `color`/`font-family` inline styles — see `backend/app/services/sanitize.py`'s exact allow-list). A `WebView` is a full browser engine for six tags; a compact hand-written parser straight into a Compose `AnnotatedString` is less code, no WebView security surface, no WebView-vs-native-scrolling interaction bugs, and impossible for a post to ever escape into.
- Feed (follow-ranked, matching `GET /posts/feed`'s own ordering), storefront "Posts" tab (`GET /posts/shop/{id}`), detail page by slug (`GET /posts/{slug}`), seller create/edit/delete (own dashboard, `POST/PATCH/DELETE /posts`), image upload.
- Composer's rich-text *input* side (bold/italic/underline/font/color on selected text) is the harder half — Compose has no built-in rich-text editor. Realistic options to evaluate when this phase starts: a minimal hand-rolled selection-aware `AnnotatedString` builder (matches the backend's own tiny allow-list exactly, nothing more), or wrapping a lightweight open-source Compose rich-text-editor library if one on Maven Central actually covers this exact tag set without dragging in more than is needed. Decide once this phase starts, not preemptively here.
- Edit-resubmits-to-pending behavior (any edit to a `published`/`rejected` post silently flips it back to `pending` server-side) needs a visible banner in the composer, matching the web app's own warning copy, so a seller isn't surprised their edit temporarily un-published something.

---

## Phase 5 — Chat & notifications

**5a — Realtime chat**: raw OkHttp `WebSocket` to `wss://api.kenabechaju.salmandev.io/ws` (single global endpoint, not per-conversation — routing happens by `conversation_id` inside each frame), exponential-backoff reconnect, connection paused while the app is backgrounded (foreground-only, matching how the web app's own tab-visibility assumption already works). Per-listing conversation threads, owner inbox with shop filter tabs, unread badges, typing-indicator relay.

**5b — In-app notifications**: notification list/read-state against the existing REST endpoints, unread-count badge.

**5c — Push notifications (background/killed-app only) — flagged explicitly as backend work, not purely client-side**:
- New `device_tokens` table (`user_id`, `fcm_token`, `platform`, timestamps), `POST`/`DELETE /notifications/device-token`.
- `notification_service.notify()` gains one more delivery path alongside its existing in-app-WS + email ones: a Firebase Admin SDK `messaging.send()` call, best-effort (a failed push is a device missing a notification, never a user-facing error).
- Android: Firebase project creation (user-driven, same as the reference app — needs a real Firebase console visit and `google-services.json`, gitignored not committed), FCM SDK, token sync on login (and un-sync on logout, called *before* the local token is cleared, since unregistering still needs to authenticate — a real bug class the reference app hit once by getting this ordering backwards), a `FirebaseMessagingService` creating its own notification channel *eagerly* at `Application.onCreate()` (not lazily inside `onMessageReceived()`, which never runs at all for the backgrounded-app path FCM auto-displays through — the exact bug the reference app shipped once and had to fix from a live push landing under the wrong, generic channel).

---

## Phase 6 — Ratings, reports & the AI assistant

- Post-transaction star ratings: the screen reflects server-computed `can_rate`/eligibility (`GET /ratings/eligibility/{listing_id}`) rather than re-implementing the "sold or out-of-stock, messaged about it, not already rated" rule client-side — same reasoning `BloodConnectJU`'s `can_respond`/`can_fulfill` flags already established: the server is the one source of truth for whether an action is currently allowed, the client only renders what it says.
- Report flow for listings/shops/users.
- AI assistant screen: SSE stream (`delta` tokens rendered incrementally, one final `listings` event resolving recommended `ListingCard`-equivalents), bilingual per the visitor's locale, session-only history (no account tie, matching the web app's own explicit privacy stance — genuinely don't persist this one). OkHttp has no built-in SSE parser; a small dedicated client (e.g. LaunchDarkly's `okhttp-eventsource`) is the pragmatic choice over hand-rolling one.

---

## Phase 7 — Bilingual, branding & release

- **Bangla strings**: `values-bn/strings.xml`, hand-synced against `frontend/messages/bn.ts` — there's no shared translation API today, so this is maintained by hand on both sides until (if ever) a third client makes a shared endpoint worth building.
- **Real typography**: bundle Inter (matching the web app's actual font) as a local variable-font file, *not* through the Downloadable Fonts provider — the reference app found that path silently falls back to the system font on any device without a signed-in Google account, a real-world reliability gap discovered on real hardware, not a hypothetical.
- **Real launcher icon & in-app branding**: replace Phase 0's placeholder tile with the actual KenaBecha mark once available (adaptive icon foreground/background, legacy pre-API-26 fallbacks).
- **Play Console**: identity verification (start early — can take days), Play App Signing, internal testing track first (up to 100 testers, live in minutes, no review wait), Data Safety form (declares: email, phone, JU student ID, photos — no location, no ad tracking, no data sold), content rating questionnaire.

---

## What stays web-only, and why

**Admin & moderation** (reporting queue resolution, bulk moderation, the audit log, category/navigation/announcement management, the admin dashboard) is not planned as a mobile phase. This is staff tooling — moderators and the admin already have a fully-capable web panel, it's used far less often than any buyer/seller-facing screen, and duplicating a dense data-table-and-bulk-actions UI natively buys real engineering cost for a surface its own users don't need on a phone. Disclosed here rather than silently absent: if a moderator specifically wants a mobile moderation surface later, that's a real, addable phase — just not one with a use case behind it yet.

---

## Known gotchas worth watching for, carried over from the reference app

Not guaranteed to recur — this is a different backend (FastAPI/JWT vs. Laravel/Sanctum) and a different design system — but cheap to watch for since they're the same Compose/Material3/Retrofit stack on the same machine:

- **A `lightColorScheme()`/`darkColorScheme()` role left unset doesn't fail loudly** — it quietly renders Material's default purple. Phase 0 already set every role explicitly; re-verify visually the first time any new component reads a role Phase 0's own screens never happened to exercise.
- **List/detail ViewModels only refreshing in `init{}`** go stale on navigate-away-and-back without a full app restart, since a ViewModel survives as long as its back-stack entry does. Every list screen needs `LaunchedEffect(Unit) { refresh() }` in its composable, not just an `init` block.
- **Retrofit's `@DELETE` rejects a body outright** (fails at call time with a clear message, not compile time) — anything needing `DELETE` + a body (e.g. a password-gated account deletion, if ever added) needs `@HTTP(method = "DELETE", path = "...", hasBody = true)`.
- **`enableEdgeToEdge()` disables automatic keyboard-avoidance** — every scrollable form screen needs `Modifier.imePadding()` on its root `Scaffold`, or the keyboard covers whatever's being typed with no way to scroll it into view.
- **Google Autofill can silently overwrite a just-typed field** with a stale saved credential when a password field gains focus, if this is ever driven by on-device UI automation rather than a human — re-verify actual on-screen field state after any password-field focus change rather than trusting a typed sequence landed.
