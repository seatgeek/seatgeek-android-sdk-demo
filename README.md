# SeatGeek Android SDK sample app
This repository contains basic example of how to integrate and use the SeatGeek SDK in an Android app.
For the documentation, please refer to our [developer documentation](https://developer.seatgeek.com/).

This branch (`sdk/5.3.0-host-appauth`) shows SDK **5.3.0** with **OIDC sign-in**, for an app that
**already uses [AppAuth](https://github.com/openid/AppAuth-Android)** for its own login.
If your app does not use AppAuth, see the
[`sdk/5.3.0`](https://github.com/seatgeek/seatgeek-android-sdk-demo/tree/sdk/5.3.0) branch instead.

## Installation & Usage
1. Pull down the repository and open the project in Android Studio.
2. In the `local.properties` file add your maven credentials to access the SDK repository.
    ```
    sdkMavenUsername=<mavenUsername>
    sdkMavenPassword=<mavenPassword>
    ```
3. Fill in your `ClientId`, `InstanceName` and redirect URI in `Application.kt`, and the redirect
   URI's scheme in the `RedirectUriReceiverActivity` intent-filter in `AndroidManifest.xml`.
4. `HostAuth.kt` stands in for your own AppAuth login: fill in your identity provider's issuer,
   client ID and redirect URI there, and your redirect scheme in `app/build.gradle.kts`
   (`appAuthRedirectScheme`).
5. Use the `app` configuration to run the sample app.
6. You can log in using some of your test accounts.

## OIDC sign-in (SDK 5.3.0+) next to your own AppAuth login
The SDK runs the whole OpenID Connect flow itself (authorization code + PKCE, token refresh,
end-session) through AppAuth, which it bundles. Your app ends up with **one** copy of AppAuth that
both logins share, and so with one `RedirectUriReceiverActivity` catching both browser redirects.
That is the only difference from an app without AppAuth: the redirect setup.

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
   Use a redirect scheme for the SDK that differs from your own login's.

2. **Keep `appAuthRedirectScheme` set to your own scheme**
   ([`app/build.gradle.kts`](app/build.gradle.kts)). The placeholder holds a single scheme, and your
   login already uses it. Do **not** switch it to the SDK's scheme: that would stop your own login's
   redirect from reaching the app.
   ```kotlin
   manifestPlaceholders["appAuthRedirectScheme"] = "com.yourcompany.yourapp" // unchanged
   ```

3. **Add the SDK's redirect as a second intent-filter on `RedirectUriReceiverActivity`**
   ([`AndroidManifest.xml`](app/src/main/AndroidManifest.xml)). The manifest merger keeps AppAuth's
   filter for your scheme and adds this one:
   ```xml
   <activity
       android:name="net.openid.appauth.RedirectUriReceiverActivity"
       android:exported="true">
       <intent-filter>
           <action android:name="android.intent.action.VIEW" />
           <category android:name="android.intent.category.DEFAULT" />
           <category android:name="android.intent.category.BROWSABLE" />
           <data android:scheme="yourAppScheme" android:host="oauth" />
       </intent-filter>
   </activity>
   ```
   AppAuth hands each redirect back to the flow in progress, so the two logins don't interfere.
   If your login uses an `https` App Link instead of a custom scheme, the same applies: add the SDK's
   filter next to your existing one.

4. **Remove the redirect intent-filter from `SeatGeekOAuthReceiverActivity`**. The legacy flow
   needed it; in OIDC mode two activities would match the SDK's redirect and break it:
   ```xml
   <activity
       android:name="com.seatgeek.android.sdk.oauth.SeatGeekOAuthReceiverActivity"
       android:exported="false"
       android:launchMode="singleTask" />
   ```

5. **Use AppAuth 0.11.1 or newer** ([`gradle/libs.versions.toml`](gradle/libs.versions.toml)). The SDK
   is built against 0.11.1, and Gradle resolves a single AppAuth version for your app and the SDK (the
   highest requested). If your app is on an older version, it gets upgraded to 0.11.1, so check that
   your own login still works; don't force a lower version than the SDK's.

The two sessions are independent: signing in or out of one does not affect the other, and each keeps
its own tokens.

### Logging out
`OAuthClient.logOut(logoutWebSession = false)` (the default) clears only the local SeatGeek session.
`logOut(logoutWebSession = true)` also ends the SeatGeek browser session (RP-initiated logout), so the
next sign-in asks for credentials again. Neither touches your own login.

## Build requirements for 5.3.0
- `compileSdk = 37` (the minimum declared by the SDK's AAR metadata).
- Core library desugaring enabled, with `desugar_jdk_libs` 2.1.5 or newer.
- An AGP that can compile against API 37: this sample uses AGP 9.4.0 / Gradle 9.7.1 / Kotlin 2.4.10.
  With AGP 9 and the standalone Kotlin plugin, set `android.builtInKotlin=false` and
  `android.newDsl=false` in `gradle.properties`, and configure `jvmTarget` in the `kotlin {}` block.

## Communication
If you have any questions, please contact us via Slack for details.
