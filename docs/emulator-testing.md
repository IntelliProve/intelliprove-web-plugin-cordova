# Testing on an emulator

How to check, without a phone, that a host app survives when Android kills the scan's WebView. This is the failure behind "the whole app restarts at the end of the scan" on low-memory phones.

## Why this needs testing

All WebViews in an app share one renderer process. When Android's low-memory killer kills it during a scan, the host's own Cordova WebView dies with the scan's WebView, and Chromium kills the whole app unless **every** WebView on that renderer handles `onRenderProcessGone`.

The plugin handles it for both. The scan screen closes, and the host screen is rebuilt once it is back in front, so the app reloads its start page instead of restarting. The host's page dies with the renderer, so it never receives the `dismiss` message on this path. A native Android host does receive it.

The hook needs cordova-android 12 or later: older versions lack `CordovaPlugin.onRenderProcessGone`, so the app fails to compile. On Android 7 (API 24–25) WebView has no such event, so behaviour there is unchanged.

## What you need

- Android Studio's emulator. A Play Store image is enough for the main test.
- For the two optional tests at the end: a **Google APIs** image (not Play Store), because they need `adb root`.
- Gradle 8.2 does not run on the newest JDKs. If the build fails on the Java version, use Android Studio's: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`

## Set up the example app

From `example/`:

```bash
npm install
cordova platform add android
cordova plugin remove cdv-intelliprovewebview --nosave
cordova plugin add ../plugin --nosave
cordova run android
```

Cordova copies the plugin's `.kt` and `.aar` into the app only on `plugin add`, never on `run`, so repeat the remove and add after every plugin change. `--nosave` keeps `package.json` unchanged.

## The test URLs

Type these into the example app's URL field.

| URL | What it does | What the log says |
| --- | --- | --- |
| `chrome://crash` | Crashes the renderer | `Render process gone (crashed: true)` |
| `chrome://kill` | Kills the renderer, as the low-memory killer does | `crash detected (code -1)`, then `Render process gone (crashed: false)` |
| `data:text/html,<script>setTimeout(()=>postMessage({stage:'rigtest'}),2000)</script>` | Sends one message to the host | `CDV PostMessage Stage: rigtest` |

`chrome://kill` is the one that matches the field failure.

## Steps

1. Note the app's process id: `adb shell pidof com.intelliprove.cdvwebview.example`
2. Clear the log: `adb logcat -c`
3. Type `chrome://kill` into the URL field and press **Open WebView**.
4. Wait about 10 seconds, then check:

| Check | Command | Pass |
| --- | --- | --- |
| The app survived | `adb shell pidof com.intelliprove.cdvwebview.example` | Same id as in step 1 |
| Chromium did not kill the app | `adb logcat -d \| grep "killing application"` | No output |
| The host screen was rebuilt once | `adb logcat -d \| grep -c "PluginManager.*init()"` | `1` |

On screen, the scan screen closes and the start page comes back with an empty URL field.

5. Open the message test page (the `data:` URL above). The host should log `CDV PostMessage Stage: rigtest`, which shows the rebuilt app still receives messages.

## Proving the test can fail

Install the old plugin and repeat the steps. Cordova cannot add a `.tgz` directly, so unpack it first:

```bash
mkdir -p /tmp/old-plugin && tar xzf ../plugin/cdv-intelliprovewebview-1.2.0.tgz -C /tmp/old-plugin
cordova plugin remove cdv-intelliprovewebview --nosave
cordova plugin add /tmp/old-plugin/package --nosave
cordova run android
```

The app must die, with `wasn't handed by all associated webviews, killing application` in the log. If it survives, the test is not exercising the kill. Afterwards, put the current plugin back with the remove and add steps from the setup.

## Optional: kills the URL field cannot cause

Both need a Google APIs image and `adb root`. Open any page in the scan screen first, then find the renderer: it is the process whose user starts with `u0_i` in `adb shell ps -A` (its name is cut off in the listing).

| Test | How | Pass |
| --- | --- | --- |
| Kill while the app is in the background | Press Home during a scan, `adb shell kill -9 <renderer pid>`, then reopen the app | Same app process id, start page reloaded |
| Kill outside a scan | Close the scan, then `adb shell kill -9 <renderer pid>` | The app dies as before. Outside a scan the plugin leaves the host's own handling alone |

## Gotchas

- `https://test.health-dev.intelliprove.com/measurement` embeds the scan in a cross-origin frame, so its messages never reach the host, crash or not. Use the `data:` page or a direct scan URL to test messages.
- A headless emulator can show "System UI isn't responding" right after boot. Tap **Wait**.
- The emulator does not reproduce real memory pressure. A retest on the affected phone is the final check.
