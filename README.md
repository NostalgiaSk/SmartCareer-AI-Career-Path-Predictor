# SmartCareer — AI Career Path Predictor

An Android application that helps students discover suitable career paths using AI-powered analysis and a structured certification learning system.

## Features

- **AI Career Analysis** — Answer questions about your skills, interests, and education to receive personalized career suggestions powered by Groq LLaMA AI
- **Career Match Scoring** — Each career suggestion includes a match percentage and description
- **Save & Track Careers** — Bookmark careers and revisit them anytime
- **Certification System** — AI-generated learning modules with locked progression, embedded quizzes (70% pass threshold), and Firestore-backed progress tracking
- **Certificate Completion** — Complete all modules and quizzes to earn certifications, viewable in "My Certifications"
- **Home Dashboard** — Gradient hero header, quick actions, latest career suggestions, saved careers, and certifications in progress at a glance

## Tech Stack

|     | Layer       | Technology                            |
|-----|------------ |---------------------------------------|
| ☕ | Language    | Java                                   |
| 🎨 | UI          | XML Layouts · Material Design 3        |
| 🔐 | Auth        | Firebase Authentication                |
| 🗄️ | Database    | Firebase Firestore                     |
| 🤖 | AI          | Groq API (LLaMA) via Retrofit          |
| 🏗️ | Architecture| Activity-based · RecyclerView Adapters |

## Project Structure

```
app/src/main/java/com/hanin/smartcareer/
├── activities/       # All screens (Home, Login, Quiz, Certificate, etc.)
├── adapters/         # RecyclerView adapters
├── models/           # Data models (Career, Module, QuizQuestion, etc.)
├── network/          # Retrofit API client & service
```

## Setup

1. Clone the repository
2. Open in Android Studio
3. Add your Groq API key in the network config
4. Connect a Firebase project (Authentication + Firestore)
5. Build and run on an emulator or device (API 24+)

## Screenshots

*Coming soon*

## License

This project is for educational purposes.
