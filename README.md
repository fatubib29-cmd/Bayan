# Jamia Al-Bayan Global — Academic Management Platform
> **Where Clarity Meets Knowledge**

Jamia Al-Bayan Global is a premier international Islamic academy management platform designed with an Apple-inspired minimalist aesthetic, restrained glassmorphism, and a zero-trust student privacy architecture.

---

## 1. Brand Identity & Design System

- **Official Brand Emblem**: Centered circular seal featuring antique gold arabesque calligraphy and open book motif on warm ivory.
- **Color Palette**:
  - Warm Ivory: `#F7F4ED`
  - Deep Teal: `#1E5064`
  - Antique Gold: `#B38326`
  - Muted Blue-Grey: `#A8B8BA`
  - Soft Taupe: `#999889`
  - Surface White: `#FFFFFF`
- **Typography & Accessibility**:
  - Full bilingual support in **English** and **Bangla (বাংলা)** with immediate in-app toggling.
  - Authentic Arabic typesetting with proper right-to-left (RTL) rendering for educational components.
  - Interactive touch targets strictly adhering to the 48dp accessibility standard.

---

## 2. Privacy & Access Control Architecture

The platform enforces a strict separation between educational records and confidential personal data:

1. **Principal**:
   - Sole authority with clearance to access **Student Private Records** (legal name, guardian name, phone, email, home address, emergency notes).
   - Manages faculty appointments, student enrollments, course track assignments, and live Zoom schedules.
   - Inspects the immutable **System Audit Log**.
   - Publishes academy announcements.

2. **Teacher**:
   - Access is restricted exclusively to **assigned students**.
   - Records daily lesson progress (lesson chapter, skills covered, status: *Not started / In progress / Practicing / Achieved*, practice completed, homework, feedback).
   - Marks attendance (Present, Absent, Late, Excused).
   - Launches authorized live Zoom sessions.
   - **Zero-Access Guarantee**: Cannot view guardian contacts, home addresses, unassigned students, or administrative settings.

3. **Student**:
   - Minimalist, distraction-free learning space.
   - 1-Click **Join Zoom Class** action for live sessions.
   - Real-time review of assigned learning goals and teacher feedback.
   - Access to approved educational resources (Structured Qaida lessons and Daily Duas).
   - Cannot view other students, directories, or administrative controls.

---

## 3. Educational Modules

- **Course Tracks**:
  1. *Qaida Foundation*
  2. *Ampara (Juz 30)*
  3. *Quran Memorization & Tajweed* (Note: *Course track indicator only; strictly no unauthorized Quran reader or audio player integrated into the app, respecting academy pedagogy*).
- **Structured Qaida**:
  - 7 progressive lessons covering isolated alphabets, joint letter forms, Harakat (Fathah, Kasrah, Dammah), Tanween, and Sukoon.
  - Student progress checklists.
- **Authentic Daily Duas**:
  - Categorized supplications (Sleep & Waking, Food & Drink, Knowledge, Travel, Prayer).
  - Verified references (Sahih al-Bukhari, Sahih Muslim, Sunan Abu Dawud, Jami` at-Tirmidhi).
  - Arabic text, transliteration, English meaning, and Bangla meaning (বাংলা অর্থ) with bookmarking.

---

## 4. Verification & Testing

Local JVM and Robolectric security tests verify:
- `testTeacherCannotAccessStudentPrivateGuardianData`: Verifies that attempting to read confidential records as a Teacher throws `SecurityException`.
- `testStudentCannotAccessPrivateGuardianData`: Verifies student isolation from confidential data.
- `testPrincipalCanAccessPrivateGuardianData`: Validates Principal clearance to access guardian records.
- `testStudentCannotEnrollOtherStudents`: Prevents unauthorized privilege escalation.
- `testTeacherIsolationOnlyShowsAssignedStudents`: Validates strict faculty isolation to assigned students only.
- `testTeacherCanSubmitProgressAndCreatesAuditLog`: Validates progress recording and audit trail creation.

To execute tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 5. Deployment Guide (Android, Firebase & Netlify)

### A. Android Environment
- The app builds out-of-the-box using Kotlin, Jetpack Compose, Material 3, and Room.
- Pre-populated with authentic seed data for instant persona demonstration (Dr. Ahmad Al-Bayan / Principal, Ustadh Tariq / Teacher, Zayd Karim / Student).

### B. Cloud Firestore & Netlify Deployment
1. **Firestore Rules**: Deploy the included `firestore.rules` file to your Firebase project to enforce deny-by-default role segregation.
2. **Netlify PWA Deployment**:
   - Build directory: `dist`
   - Set single-page application redirect rule in `_redirects` (`/*  /index.html  200`).
   - Store service account credentials exclusively in Netlify server-side environment variables.
