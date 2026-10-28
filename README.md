# SeatGeek Android SDK sample app
This repository contains basic example of how to integrate and use the SeatGeek SDK in an Android app.
For the documentation, please refer to our [developer documentation](https://developer.seatgeek.com/).

This branch (`sdk/5.3.0`) shows SDK **5.3.0** with **OIDC sign-in**, for an app that does **not**
use [AppAuth](https://github.com/openid/AppAuth-Android) itself.
If your app already uses AppAuth for its own login, see the
[`sdk/5.3.0-host-appauth`](https://github.com/seatgeek/seatgeek-android-sdk-demo/tree/sdk/5.3.0-host-appauth) branch instead: the redirect setup differs.

## Installation & Usage
1. Pull down the repository and open the project in Android Studio.
2. In the `local.properties` file add your maven credentials to access the SDK repository.
    ```
    sdkMavenUsername=<mavenUsername>
    sdkMavenPassword=<mavenPassword>
    ```
3. Fill in your `ClientId`, `InstanceName` and redirect URI in `Application.kt`, and the redirect
   URI's scheme in `app/build.gradle.kts` (`appAuthRedirectScheme`).
4. Use the `app` configuration to run the sample app.
5. You can log in using some of your test accounts.

## OIDC sign-in (SDK 5.3.0+)
The SDK runs the whole OpenID Connect flow itself (authorization code + PKCE, token refresh,
end-session) through AppAuth, which it bundles. The app needs no AppAuth code of its own. On top of
the standard integration, enabling OIDC takes three changes:

1. **Request the `openid` scope** when building the SDK
   ([`Application.kt`](app/src/main/java/com/seatgeek/android/sdk/sample/Application.kt)):
   ```kotlin
   SeatGeekBuilder(this)
       .setClientId(clientId)
       .setInstanceName(instanceName)
       .setRedirectUri("yourAppScheme://oauth".toUri())
       .additionalScopes(arrayOf("openid")) // the SDK adds "email" and "profile" itself
       .build()
   ```
   The SDK's "Sign in with SeatGeek" button then runs the OIDC flow. Without `openid` the SDK keeps
   using the legacy OAuth flow.

2. **Declare the redirect scheme for AppAuth**
   ([`app/build.gradle.kts`](app/build.gradle.kts)). It must be the scheme of the redirect URI above:
   ```kotlin
   android {
       defaultConfig {
           manifestPlaceholders["appAuthRedirectScheme"] = "yourAppScheme"
       }
   }
   ```
   AppAuth's `RedirectUriReceiverActivity` (merged into your manifest from the SDK) catches the
   browser redirect for this scheme.

3. **Remove the redirect intent-filter from `SeatGeekOAuthReceiverActivity`**
   ([`AndroidManifest.xml`](app/src/main/AndroidManifest.xml)). The legacy flow needed it; in OIDC
   mode two activities would match the redirect and break it:
   ```xml
   <activity
       android:name="com.seatgeek.android.sdk.oauth.SeatGeekOAuthReceiverActivity"
       android:exported="false"
       android:launchMode="singleTask" />
   ```

### Logging out
`OAuthClient.logOut(logoutWebSession = false)` (the default) clears only the local session.
`logOut(logoutWebSession = true)` also ends the SeatGeek browser session (RP-initiated logout), so the
next sign-in asks for credentials again.

## Build requirements for 5.3.0
- `compileSdk = 37` (the minimum declared by the SDK's AAR metadata).
- Core library desugaring enabled, with `desugar_jdk_libs` 2.1.5 or newer.
- An AGP that can compile against API 37: this sample uses AGP 9.4.0 / Gradle 9.7.1 / Kotlin 2.4.10.
  With AGP 9 and the standalone Kotlin plugin, set `android.builtInKotlin=false` and
  `android.newDsl=false` in `gradle.properties`, and configure `jvmTarget` in the `kotlin {}` block.

## Communication
If you have any questions, please contact us via Slack for details.
