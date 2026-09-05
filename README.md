# Lumina — Extraordinary Gallery

Package: com.willykez.lumina

An Android gallery app (Jetpack Compose + Kotlin) implementing seven
extraordinary browsing experiences:

1. Parallax Depth — gyroscope-driven 3D layered scrolling
2. Cube Carousel — swipe-to-rotate 3D cube page transitions
3. Morphing Grid — thumbnails morph into fullscreen with shared-feel animation
4. Infinite Canvas — pinch-zoom / pan across a giant media map
5. Stacked Layers — swipeable album sheets that peek and stack
6. Color Mosaic — thumbnails sorted and tinted by extracted hue
7. Smart Clusters — orbiting bubble clusters grouped by album, tap to explode

## Build

Open in Android Studio (Koala+ recommended) and run, or from CLI:

    ./gradlew assembleDebug

APK output: app/build/outputs/apk/debug/app-debug.apk

## CI / CD

Two GitHub Actions workflows live in `.github/workflows/`:

- **ci.yml** — runs on every push/PR, builds an unsigned debug APK for
  fast compile-error feedback, uploads it as a build artifact.
- **release.yml** — runs on a `v*` tag push (or manual dispatch), builds
  a signed, minified release APK and attaches it to a GitHub Release.

The release workflow needs these repository secrets set under
**Settings → Secrets and variables → Actions**:

| Secret            | Value                                                        |
|-------------------|---------------------------------------------------------------|
| `KEYSTORE_B64`    | Your `.keystore`/`.jks` file, base64-encoded (`base64 -i key.jks`) |
| `STORE_PASSWORD`  | Keystore password                                              |
| `KEY_PASSWORD`    | Key password (must match `STORE_PASSWORD` for PKCS12 keystores) |

The key alias is hardcoded to `upload` in the workflow — generate your
keystore with that alias, e.g.:

    keytool -genkeypair -v -keystore repomaster-release.keystore \
      -alias upload -keyalg RSA -keysize 2048 -validity 10000 \
      -storetype PKCS12

To cut a signed release: `git tag v1.0.0 && git push origin v1.0.0`.

## Requirements

- minSdk 26, targetSdk 35, compileSdk 35
- Kotlin 2.0, Jetpack Compose (BOM 2024.09.00)
- Coil for image loading, Accompanist for runtime permissions
- Reads device photos via MediaStore — grant the media permission on first launch
