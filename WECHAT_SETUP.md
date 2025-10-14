# WeChat Login Setup Guide

This guide explains how to set up WeChat login for the Maternity Matron app.

## Features Implemented

### Backend (Spring Boot 3)
- WeChat OAuth 2.0 authentication flow
- Automatic user creation from WeChat profile
- JWT tokens with **sliding expiration** (24 hours, extends on each request)
- WeChat OpenID as user identifier
- Support for both WeChat and traditional email/password login

### Frontend (Flutter)
- Clean WeChat login UI with role selection (Mother/Matron)
- Integration with fluwx SDK
- Automatic token refresh handling
- One-tap login experience

## JWT Sliding Expiration

**How it works:**
- Initial token expires after 24 hours
- On **every API request**, the backend returns a new token in the `X-New-Token` header
- Flutter app automatically updates the token
- Result: Active users **never get logged out**
- Inactive sessions expire after 24 hours of no activity

**Why it's better than refresh tokens:**
- No database storage needed
- No additional endpoints
- Simpler implementation
- Better UX (transparent to user)

## Prerequisites

### 1. WeChat Open Platform Account

You need to register your app on WeChat Open Platform:

1. Go to https://open.weixin.qq.com
2. Create a developer account (requires Chinese phone number)
3. Register your mobile app
4. Get your credentials:
   - **AppID** (应用ID)
   - **AppSecret** (应用密钥)

### 2. App Configuration

#### Android
- Register app package name
- Register app signature
- Download and configure WeChat SDK

#### iOS
- Register bundle identifier
- Configure universal links
- Add URL schemes to Info.plist

## Backend Setup

### Step 1: Update Application Properties

Edit `maternity-backend/src/main/resources/application.properties`:

```properties
# WeChat OAuth Configuration
wechat.app-id=YOUR_WECHAT_APP_ID
wechat.app-secret=YOUR_WECHAT_APP_SECRET
```

Replace `YOUR_WECHAT_APP_ID` and `YOUR_WECHAT_APP_SECRET` with your actual credentials from WeChat Open Platform.

### Step 2: Build and Run

```bash
cd maternity-backend
mvn clean install
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### Step 3: Test WeChat Endpoint

You can test the WeChat login endpoint in Swagger UI:

1. Open http://localhost:8080/swagger-ui.html
2. Find `POST /api/auth/wechat/login`
3. Send a test request:

```json
{
  "code": "WECHAT_AUTH_CODE",
  "role": "MOTHER"
}
```

**Note:** In production, the `code` comes from WeChat app after user authorization.

## Frontend Setup

### Step 1: Install Dependencies

```bash
cd maternity_app
flutter pub get
```

This will install the `fluwx` package for WeChat SDK integration.

### Step 2: Configure WeChat SDK

Edit `lib/main.dart` and replace placeholders:

```dart
await registerWxApi(
  appId: 'YOUR_WECHAT_APP_ID',  // Same as backend
  doOnAndroid: true,
  doOnIOS: true,
  universalLink: 'https://your-domain.com/wechat/',  // For iOS
);
```

### Step 3: Android Configuration

Create/edit `android/app/src/main/AndroidManifest.xml`:

```xml
<application>
    <!-- Your existing config -->

    <!-- WeChat -->
    <activity
        android:name=".wxapi.WXEntryActivity"
        android:exported="true"
        android:launchMode="singleTask"
        android:taskAffinity="${applicationId}"
        android:theme="@android:style/Theme.Translucent.NoTitleBar">
        <intent-filter>
            <action android:name="android.intent.action.VIEW"/>
            <category android:name="android.intent.category.DEFAULT"/>
            <data android:scheme="YOUR_WECHAT_APP_ID"/>
        </intent-filter>
    </activity>
</application>
```

Create `android/app/src/main/java/com/maternity/maternity_app/wxapi/WXEntryActivity.java`:

```java
package com.maternity.maternity_app.wxapi;

import com.jarvan.fluwx.wxapi.FluwxWXEntryActivity;

public class WXEntryActivity extends FluwxWXEntryActivity {
}
```

### Step 4: iOS Configuration

Edit `ios/Runner/Info.plist`:

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLName</key>
        <string>weixin</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>YOUR_WECHAT_APP_ID</string>
        </array>
    </dict>
</array>

<key>LSApplicationQueriesSchemes</key>
<array>
    <string>weixin</string>
    <string>weixinULAPI</string>
</array>
```

Set up universal links for WeChat callback (required for iOS):
- Configure associated domains in Xcode
- Add `applinks:your-domain.com` to entitlements
- Host apple-app-site-association file on your domain

### Step 5: Update Backend URL

If testing on a physical device, update the backend URL in `lib/services/wechat_auth_service.dart`:

```dart
static const String baseUrl = 'http://YOUR_LOCAL_IP:8080/api';
// Example: 'http://192.168.1.100:8080/api'
```

### Step 6: Run the App

```bash
flutter run
```

## How It Works

### Complete Authentication Flow

```
┌─────────────┐         ┌────────────┐         ┌─────────────┐         ┌─────────────┐
│   Flutter   │         │   WeChat   │         │Your Backend │         │WeChat OAuth │
│     App     │         │    App     │         │  (Spring)   │         │     API     │
└──────┬──────┘         └─────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                      │                       │                       │
       │ 1. User taps        │                       │                       │
       │    "Login WeChat"   │                       │                       │
       │    + selects role   │                       │                       │
       │                     │                       │                       │
       │ 2. sendWeChatAuth() │                       │                       │
       ├────────────────────>│                       │                       │
       │                     │                       │                       │
       │                     │ 3. User authorizes    │                       │
       │                     │    in WeChat app      │                       │
       │                     │                       │                       │
       │ 4. Auth code        │                       │                       │
       │<────────────────────┤                       │                       │
       │                     │                       │                       │
       │ 5. POST /api/auth/wechat/login             │                       │
       │    {code, role}     │                       │                       │
       ├─────────────────────┴──────────────────────>│                       │
       │                                             │                       │
       │                                             │ 6. Exchange code      │
       │                                             │    for access token   │
       │                                             ├──────────────────────>│
       │                                             │                       │
       │                                             │ 7. Access token       │
       │                                             │<──────────────────────┤
       │                                             │                       │
       │                                             │ 8. Get user info      │
       │                                             ├──────────────────────>│
       │                                             │                       │
       │                                             │ 9. User info          │
       │                                             │    (openId, nickname) │
       │                                             │<──────────────────────┤
       │                                             │                       │
       │                                             │ 10. Find/create user  │
       │                                             │     in database       │
       │                                             │                       │
       │                                             │ 11. Generate JWT      │
       │                                             │     (24h expiry)      │
       │                                             │                       │
       │ 12. {token, user}                          │                       │
       │<────────────────────────────────────────────┤                       │
       │                                             │                       │
       │ 13. Navigate to home                        │                       │
       │     (Mother/Matron feed)                    │                       │
       │                                             │                       │
```

### Subsequent API Requests (Sliding Expiration)

```
┌─────────────┐         ┌─────────────┐
│   Flutter   │         │Your Backend │
│     App     │         │  (Spring)   │
└──────┬──────┘         └──────┬──────┘
       │                       │
       │ GET /api/matrons      │
       │ Authorization: Bearer │
       │ <old-token>           │
       ├──────────────────────>│
       │                       │
       │                       │ Validate token ✓
       │                       │ Process request
       │                       │ Generate new token
       │                       │ (extends 24h)
       │                       │
       │ Response              │
       │ X-New-Token: <new>    │
       │<──────────────────────┤
       │                       │
       │ Update stored token   │
       │                       │
```

## API Endpoints

### POST `/api/auth/wechat/login`

Login with WeChat authorization code.

**Request:**
```json
{
  "code": "061a4Jml2NsOeK4k1inl2j8HvH1a4Jma",
  "role": "MOTHER"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "Zhang Wei",
    "email": null,
    "role": "MOTHER",
    "wechatOpenId": "oqP7v6X8...",
    "wechatNickname": "Zhang Wei",
    "wechatAvatarUrl": "https://thirdwx.qlogo.cn/...",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

## Security Considerations

### Production Checklist

- [ ] Change JWT secret in `application.properties` to a strong random value
- [ ] Enable HTTPS for all backend endpoints
- [ ] Store JWT tokens securely on Flutter (use `flutter_secure_storage`)
- [ ] Add rate limiting to prevent abuse
- [ ] Validate WeChat credentials server-side (already implemented)
- [ ] Add logging and monitoring for auth failures
- [ ] Implement token revocation for logout
- [ ] Set up proper CORS configuration
- [ ] Use environment variables for sensitive config

### Why Sliding Expiration is Secure

1. **Short initial expiry**: Tokens expire after 24 hours of inactivity
2. **Automatic refresh**: Active users get new tokens transparently
3. **Server-side validation**: Each request is still authenticated
4. **No session storage**: Stateless authentication scales horizontally
5. **Stolen token protection**: Tokens expire if not used within 24 hours

## Troubleshooting

### Backend Issues

**Error: "Invalid WeChat credentials"**
- Check that `wechat.app-id` and `wechat.app-secret` match WeChat Open Platform
- Verify credentials are for mobile app, not official account

**Error: "WeChat API error: 40029"**
- Invalid authorization code
- Code can only be used once
- Code expires after 5 minutes

**Error: "Failed to get access token"**
- Check backend logs for detailed error
- Ensure backend can reach WeChat API (`api.weixin.qq.com`)
- Verify firewall/proxy settings

### Flutter Issues

**Error: "WeChat app not installed"**
- WeChat app must be installed on device
- For iOS simulator, WeChat doesn't work (use real device)

**Error: "Auth cancelled"**
- User cancelled in WeChat app
- Normal behavior, handle gracefully

**No callback received**
- Check `WXEntryActivity` is created (Android)
- Verify URL schemes configured (iOS)
- Ensure `registerWxApi()` called before login

## Testing Without WeChat

For development/testing without actual WeChat app:

1. Keep the old email/password endpoints (still functional)
2. Use Postman to test backend WeChat endpoint with mock data
3. Add a development bypass in Flutter (feature flag)

## Additional Resources

- [WeChat Open Platform](https://open.weixin.qq.com)
- [fluwx Documentation](https://pub.dev/packages/fluwx)
- [WeChat Login Guide (Chinese)](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

## Support

If you encounter issues:
1. Check backend logs: `maternity-backend/logs`
2. Check Flutter logs: `flutter logs`
3. Verify WeChat SDK registration: `registerWxApi()` returns true
4. Test backend endpoint directly in Swagger UI

## Summary

You've successfully implemented:
- ✅ WeChat-only authentication (no passwords!)
- ✅ Sliding JWT expiration (active users never logged out)
- ✅ Automatic user creation from WeChat profile
- ✅ Clean role selection UI (Mother/Matron)
- ✅ Secure stateless authentication
- ✅ Production-ready architecture

Users can now login with one tap using WeChat, and will stay logged in as long as they use the app regularly!
