<<<<<<< HEAD
# 🩸 Blood Donation Finder — Android Application

**Version:** 2.0 | **Language:** Java | **IDE:** Android Studio  
**By:** Anuj Partani (BT24CSE163) & Aditi Thakre (BT24CSE152)

---

## 📋 Project Overview

Blood Donation Finder is a real-time Android application that connects blood donors with recipients using Firebase as the backend. Built from the SRS document (March 2026). The app includes **Multilingual Support (English & Hindi)**, **ML Kit-powered Prescription Scanning (OCR)**, a **Gamified Donor Loyalty Badge System**, and **Secure In-App Chat** for privacy-first donor communication.

---

## 🚀 Setup Instructions (Android Studio)

### Step 1 — Open the Project
1. Open **Android Studio** (Electric Eel / Flamingo or later)
2. Click **File → Open**
3. Navigate to this `BloodDonationFinder` folder and click **OK**
4. Wait for Gradle to sync

### Step 2 — Firebase Setup (REQUIRED)
1. Go to [https://console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add Project** → name it `BloodDonationFinder`
3. In the project, click **Add App** → choose **Android**
4. Enter package name: `com.blooddonation.finder`
5. Download the `google-services.json` file
6. **Replace** `app/google-services.json` with the downloaded file

### Step 3 — Enable Firebase Services
In your Firebase Console, enable these services:

| Service | Path |
|---|---|
| Authentication | Authentication → Sign-in method → Enable Email/Password & Phone |
| Realtime Database | Realtime Database → Create Database → Start in test mode |
| Cloud Messaging | Already enabled by default |
| Storage | Storage → Get Started |

### Step 4 — Apply Database Security Rules
1. In Firebase Console → Realtime Database → Rules tab
2. Paste the contents of `firebase_database_rules.json`
3. Click Publish

### Step 5 — Google Maps API Key
1. Go to [https://console.cloud.google.com](https://console.cloud.google.com)
2. Enable **Maps SDK for Android**
3. Create an API Key
4. In `AndroidManifest.xml`, replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your key

### Step 6 — Build & Run
1. Connect your Android device (or start an emulator with API 24+)
2. Click the **▶ Run** button in Android Studio
3. Select your device
4. The app will build and install

---

## 📁 Project Structure

```
BloodDonationFinder/
├── app/
│   ├── src/main/
│   │   ├── java/com/blooddonation/finder/
│   │   │   ├── activities/
│   │   │   │   ├── SplashActivity.java        ← App entry, auto-login check
│   │   │   │   ├── LoginActivity.java         ← Firebase email login
│   │   │   │   ├── RegisterActivity.java      ← New donor registration + GPS
│   │   │   │   ├── MainActivity.java          ← Home dashboard
│   │   │   │   ├── SearchActivity.java        ← Search donors by blood group + radius
│   │   │   │   ├── DonorProfileActivity.java  ← View donor, call/WhatsApp/Secure Chat
│   │   │   │   ├── ChatActivity.java          ← Privacy-first Firebase in-app chat
│   │   │   │   ├── MapActivity.java           ← Google Maps with donor pins
│   │   │   │   ├── PostRequestActivity.java   ← Post blood request → triggers FCM
│   │   │   │   ├── DonationHistoryActivity.java ← Past donations list
│   │   │   │   ├── NotificationsActivity.java ← Open requests for donor's blood type
│   │   │   │   ├── ProfileActivity.java       ← Edit profile, upload photo
│   │   │   │   └── AdminActivity.java         ← Admin panel: stats + manage donors
│   │   │   ├── adapters/
│   │   │   │   ├── DonorAdapter.java          ← RecyclerView for donor search results
│   │   │   │   ├── RequestAdapter.java        ← RecyclerView for blood requests
│   │   │   │   ├── HistoryAdapter.java        ← RecyclerView for donation history
│   │   │   │   └── AdminDonorAdapter.java     ← RecyclerView for admin user list
│   │   │   ├── models/
│   │   │   │   ├── Donor.java                 ← Firebase donor data model (+ badge & loyalty logic)
│   │   │   │   ├── BloodRequest.java          ← Firebase blood request model
│   │   │   │   └── DonationHistory.java       ← Firebase donation history model
│   │   │   └── utils/
│   │   │       ├── LocaleHelper.java          ← Dynamic language switching (English/Hindi)
│   │   │       ├── LocationUtils.java         ← Haversine distance, 56-day eligibility
│   │   │       └── MyFirebaseMessagingService.java ← FCM push notifications
│   │   ├── res/
│   │   │   ├── layout/                        ← All XML screen layouts
│   │   │   ├── drawable/                      ← Icons and backgrounds
│   │   │   ├── values/
│   │   │   │   ├── colors.xml                 ← Blood red theme colors
│   │   │   │   ├── strings.xml                ← Default app strings (English)
│   │   │   │   └── themes.xml                 ← Material Design 3 theme
│   │   │   ├── values-hi/
│   │   │   │   └── strings.xml                ← Hindi localized strings
│   │   │   └── mipmap-*/                      ← App launcher icons
│   │   └── AndroidManifest.xml                ← Permissions, activities declared
│   ├── build.gradle                           ← All dependencies (Firebase, Maps, Glide)
│   └── google-services.json                   ← ⚠️ REPLACE WITH YOUR OWN
├── build.gradle                               ← Root build config
├── settings.gradle
├── firebase_database_rules.json               ← Paste into Firebase Console
└── README.md
```

---

## 🔑 Key Features Implemented

| Feature | Implementation |
|---|---|
| Firebase Auth | Email/Password login & registration |
| Real-Time DB | Donor profiles, requests, history |
| GPS Location | FusedLocationProviderClient |
| Haversine Radius Search | LocationUtils.distanceKm() |
| Google Maps | SupportMapFragment with colour-coded donor pins |
| FCM Notifications | MyFirebaseMessagingService |
| Availability Toggle | Switch → Firebase real-time update |
| Multilingual Support | English & Hindi dynamic switch via LocaleHelper |
| Admin Panel | Stats dashboard + donor management |
| Donation History | Complete record per donor |
| Profile Photo | Firebase Storage + Glide |
| 56-Day Eligibility | LocationUtils.isEligibleToDonate() |
| WhatsApp Contact | Intent to wa.me/ deep link |
| **🔒 Secure In-App Chat** | Firebase Realtime DB chat between donor & requester (no phone sharing needed) |
| **📸 ML Kit OCR Scanning** | Scan hospital prescriptions → auto-fill blood group, urgency & notes |
| **🏅 Gamified Badge System** | Donor loyalty badges (Rookie → Diamond) + loyalty points (50 pts/donation) |

---

## 📦 Dependencies (auto-downloaded by Gradle)

- Firebase BOM 32.7.0 (Auth, Database, Messaging, Storage, Analytics)
- Google Maps SDK 18.2.0
- Google Location Services 21.1.0
- Glide 4.16.0 (image loading)
- CircleImageView 3.1.0
- Material Components 1.11.0
- Lottie Animations 6.3.0
- **Google ML Kit Text Recognition 16.0.0** (OCR prescription scanning)

---

## ⚠️ Important Notes

- **Minimum Android version:** API 24 (Android 7.0)
- **google-services.json MUST be replaced** before building
- **Google Maps API key MUST be added** in AndroidManifest.xml
- FCM push notifications require a real device (not emulator) for testing
- For admin access: manually set `isAdmin: true` in Firebase Realtime Database for a user's UID
- ML Kit OCR works offline — no extra API key needed
- In-App Chat uses Firebase path: `chats/{chatId}/messages/` — no phone number exposure

---

## 🏅 Donor Badge Progression

| Badge | Donations Required | Loyalty Points |
|---|---|---|
| 🔰 Rookie | 0 | 0 pts |
| 🥉 Bronze Helper | 1+ | 50+ pts |
| 🥈 Silver Lifeline | 3+ | 150+ pts |
| 🥇 Gold Donor | 5+ | 250+ pts |
| 💎 Diamond Saver | 10+ | 500+ pts |

---

*Blood Donation Finder © 2026 | Anuj Partani & Aditi Thakre*
=======
# BloodDonationFinder-App
>>>>>>> 7dc54f2026fd24e1baa4f371efa21ee68a5afb6a
