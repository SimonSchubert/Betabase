# Screenshot image fixtures

**No binary images are stored in this directory.**

Paparazzi tests download live IFSC athlete portraits and YouTube thumbs at
runtime via `installLiveImageLoader()`:

- Source URLs live in `ScreenshotTestData.kt` (same CDNs the app uses).
- Bytes are cached under `screenshotTests/build/fixture-cache/` (gitignored
  with the rest of `build/`).
- Only the **rendered** store/README PNGs are committed (`media/`, `fastlane/`).

```bash
./gradlew :screenshotTests:updateScreenshots   # needs network on cold cache
rm -rf screenshotTests/build/fixture-cache     # force re-download
```
