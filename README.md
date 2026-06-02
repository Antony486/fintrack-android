# 💰 FinTrack — Personal Finance Tracker

FinTrack is a full-stack personal finance management system designed to help users track income, expenses, budgets, and payments in real time. It supports mobile money (M-Pesa), card payments (Stripe), and currency conversion.

---

## 🚀 Features

* 🔐 Secure authentication using JWT
* 💳 Stripe payment integration (test + production ready)
* 📱 M-Pesa STK Push integration (Daraja API)
* 💰 Budget tracking and expense categorization
* 📊 Transaction history management
* 💱 Real-time currency conversion
* 📦 RESTful API built with Kotlin + Ktor
* 📱 Android app built with Jetpack Compose

---

## 🧱 Tech Stack

### Backend

* Kotlin
* Ktor Server
* Exposed ORM
* H2 / PostgreSQL
* JWT Authentication
* Stripe API
* Safaricom Daraja API (M-Pesa)

### Android App

* Kotlin
* Jetpack Compose (NO XML)
* Koin (Dependency Injection)
* Retrofit
* Room Database
* DataStore Preferences

---

## 📁 Project Structure

```
fintrack/
├── fintrack-backend/
│   ├── src/main/kotlin/com/fintrack/
│   ├── payments/
│   ├── routes/
│   ├── repository/
│   └── plugins/
│
├── fintrack-android/
│   ├── ui/
│   ├── viewmodel/
│   ├── data/
│   ├── di/
│   └── navigation/
```

---

## ⚙️ Setup Instructions

### 1. Backend

```bash
cd fintrack-backend
chmod +x run.sh
./run.sh
```

Server runs at:

```
http://localhost:8080
```

---

### 2. Android App

* Open `fintrack-android/` in Android Studio
* Sync Gradle
* Connect physical device
* Set BASE_URL in config to your laptop IP:

```
http://YOUR_IP:8080/api/v1
```

---

## 🔐 Environment Variables

Create a `.env` file in backend:

```
STRIPE_SECRET_KEY=
STRIPE_PUBLISHABLE_KEY=
STRIPE_WEBHOOK_SECRET=
MPESA_CONSUMER_KEY=
MPESA_CONSUMER_SECRET=
MPESA_PASSKEY=
MPESA_CALLBACK_URL=
JWT_SECRET=
EXCHANGE_RATE_API_KEY=
```

---

## 📡 API Endpoints

### Auth

* POST `/auth/register`
* POST `/auth/login`

### Transactions

* GET `/transactions`
* POST `/transactions`

### Budget

* GET `/budgets`
* POST `/budgets`

### Payments

* POST `/mpesa/stkpush`
* POST `/stripe/checkout`

---

## 🧠 Architecture

* Clean layered architecture
* Repository pattern
* Separation of concerns (routes, services, repositories)
* Stateless JWT authentication
* REST API design

---

## 📱 Android Architecture

* MVVM (ViewModel + Repository pattern)
* Jetpack Compose UI
* Koin dependency injection
* Retrofit networking
* Room local caching

---

## 🧪 Status

* Backend: ✅ Complete (ktor)
* Android: ✅ Complete (Compose + Koin)
* Payment integrations: ✅ M-Pesa + Stripe
* Currency API: ✅ Integrated

---

## 👨‍💻 Author

**Antony486**

---

## 📌 Notes

* This project is in active development
* Built for learning and production-grade architecture practice
