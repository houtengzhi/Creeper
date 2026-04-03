# Creeper

**Creeper** is an Android application designed for managing and converting proxy subscriptions. It features an embedded web server that allows for flexible subscription management and distribution directly from your Android device.

[中文说明](README_ZH.md)

### 🚀 Features

- **Subscription Management**: Easily add, edit, delete, and list proxy subscription sources.
- **Embedded Web Server**: Built-in HTTP server (powered by AndServer) to provide RESTful APIs and serve converted subscription files.
- **Protocol Support**: Designed to handle various proxy protocols including Clash and V2Ray.
- **Modern Tech Stack**:
    - **UI**: 100% Jetpack Compose for a modern, fluid user interface.
    - **Dependency Injection**: Hilt (Dagger) for robust architecture.
    - **Database**: Room for local data persistence.
    - **Networking**: Retrofit & OkHttp for reliable remote data fetching.
    - **Server**: AndServer for hosting a web server within the Android app.
- **Foreground Service**: Ensures the local server remains active and reliable.

### 🛠️ Architecture

The project follows modern Android development best practices:
- **MVVM / MVI** patterns with Jetpack Compose.
- **Repository Pattern** for data abstraction (DB, File, and Network).
- **Hilt** for dependency injection across activities, services, and controllers.

### 📦 Getting Started

1.  **Build**: Open the project in Android Studio (Ladybug or newer recommended).
2.  **Run**: Deploy to an Android device (API 24+).
3.  **Use**: Open the app to manage subscriptions. The local server starts as a foreground service, enabling API access at the configured port.
