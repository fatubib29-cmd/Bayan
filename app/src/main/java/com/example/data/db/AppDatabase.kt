package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AcademyDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        StudentEducationalProfile::class,
        StudentPrivateRecord::class,
        TeacherProfile::class,
        AttendanceRecord::class,
        DailyProgressRecord::class,
        ZoomClassRecord::class,
        MessageRecord::class,
        AnnouncementRecord::class,
        QaidaLesson::class,
        DailyDua::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jamia_albayan_academy.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.academyDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AcademyDao) {
            // 1. User accounts (for instant Persona / Role authentication)
            dao.insertUser(UserEntity("USR-001", "principal", Role.PRINCIPAL, "Dr. Ahmad Al-Bayan", "en", true, "ADM-001"))
            dao.insertUser(UserEntity("USR-101", "teacher_tariq", Role.TEACHER, "Ustadh Tariq Mahmud", "en", true, "TCH-001"))
            dao.insertUser(UserEntity("USR-102", "teacher_zainab", Role.TEACHER, "Ustadha Zainab Noor", "bn", true, "TCH-002"))
            dao.insertUser(UserEntity("USR-201", "student_zayd", Role.STUDENT, "Zayd Karim", "en", true, "STD-101"))
            dao.insertUser(UserEntity("USR-202", "student_maryam", Role.STUDENT, "Maryam Fatima", "bn", true, "STD-102"))
            dao.insertUser(UserEntity("USR-203", "student_yusuf", Role.STUDENT, "Yusuf Khan", "en", true, "STD-103"))

            // 2. Teachers
            dao.insertTeacher(TeacherProfile("TCH-001", "Ustadh Tariq Mahmud", "tariq.mahmud@jamia-albayan.edu", "+880 1710-112233", "en", "Makharij, Qaida Noorani, Tajweed Foundation"))
            dao.insertTeacher(TeacherProfile("TCH-002", "Ustadha Zainab Noor", "zainab.noor@jamia-albayan.edu", "+880 1715-445566", "bn", "Ampara Juz 30, Tartil, Daily Duas"))

            // 3. Students Educational Profiles
            dao.insertStudentEducational(StudentEducationalProfile("STD-101", "Zayd Karim", "Qaida", "Lesson 4: Harakat (Kasrah)", "TCH-001", "2026-01-15", "en", "UTC+6", "Master short vowels with accurate makharij."))
            dao.insertStudentEducational(StudentEducationalProfile("STD-102", "Maryam Fatima", "Ampara", "Surah Al-Inshirah (Juz 30)", "TCH-002", "2026-02-01", "bn", "UTC+6", "Complete Surah recitation with proper tajweed rules."))
            dao.insertStudentEducational(StudentEducationalProfile("STD-103", "Yusuf Khan", "Quran", "Surah Al-Mulk Review", "TCH-001", "2026-02-10", "en", "UTC+6", "Solidify memorization of verses 1-15 with tartil pacing."))

            // 4. Confidential Private Records (STRICT PRINCIPAL CLEARANCE ONLY)
            dao.insertStudentPrivateRecord(StudentPrivateRecord("STD-101", "Zayd Karim Chowdhury", "Mohammad Karim Chowdhury", "+880 1711-234567", "karim.chowdhury@example.com", "House 42, Road 9, Dhanmondi, Dhaka", "Mother's cell: +880 1712-987654. Mild peanut allergy.", "Authorized fee waiver candidate."))
            dao.insertStudentPrivateRecord(StudentPrivateRecord("STD-102", "Maryam Fatima Siddique", "Dr. Tariqul Siddique", "+880 1819-345678", "dr.tariqul@example.com", "Apt 5B, Gulshan-2, Dhaka", "Prefers evening contact after 6 PM.", "Enrolled through International Scholarship."))
            dao.insertStudentPrivateRecord(StudentPrivateRecord("STD-103", "Yusuf Abdullah Khan", "Abdullah Al-Mamun Khan", "+880 1912-456789", "mamun.khan@example.com", "Sector 4, Uttara, Dhaka", "Weekend travel schedule.", "Regular donor patron."))

            // 5. Prepopulated Zoom Classes
            dao.insertClass(ZoomClassRecord(
                title = "Live Qaida & Makharij Drill",
                teacherId = "TCH-001",
                studentId = "STD-101",
                scheduledDateTime = "Today, 10:30 AM",
                durationMinutes = 40,
                zoomUrl = "https://zoom.us/j/84920481928?pwd=JamiaAlBayanQaida",
                notes = "Focus on throat letters (Huroof Halqiyyah).",
                status = "LIVE"
            ))
            dao.insertClass(ZoomClassRecord(
                title = "Ampara Juz 30 Tartil Session",
                teacherId = "TCH-002",
                studentId = "STD-102",
                scheduledDateTime = "Tomorrow, 04:00 PM",
                durationMinutes = 45,
                zoomUrl = "https://zoom.us/j/91283746501?pwd=JamiaAlBayanAmpara",
                notes = "Review Surah Ad-Duha and Al-Inshirah.",
                status = "UPCOMING"
            ))

            // 6. Prepopulated Daily Progress
            dao.insertProgress(DailyProgressRecord(
                studentId = "STD-101",
                teacherId = "TCH-001",
                courseTrack = "Qaida",
                date = "2026-10-09",
                chapterLesson = "Lesson 3: Harakat - Fathah",
                skillsCovered = "Clear pronunciation of Ba-Ta-Tha with quick vowel sound",
                status = "ACHIEVED",
                practiceCompleted = "Read full practice table twice without dragging",
                homework = "Practice Lesson 4: Kasrah table lines 1-3",
                teacherFeedback = "Mashallah, very sharp makhraj improvement today!",
                submittedBy = "Ustadh Tariq Mahmud"
            ))
            dao.insertProgress(DailyProgressRecord(
                studentId = "STD-102",
                teacherId = "TCH-002",
                courseTrack = "Ampara",
                date = "2026-10-08",
                chapterLesson = "Surah Ash-Shams Verses 1-7",
                skillsCovered = "Noon and Meem Mushaddad with 2-count ghunnah",
                status = "PRACTICING",
                practiceCompleted = "Verses 1-7 recited 5 times",
                homework = "Memorize verses 8-12 with tajweed",
                teacherFeedback = "Good retention. Keep steady rhythm.",
                submittedBy = "Ustadha Zainab Noor"
            ))

            // 7. Prepopulated Attendance
            dao.insertAttendance(AttendanceRecord(studentId = "STD-101", teacherId = "TCH-001", classDate = "2026-10-09", status = "PRESENT", notes = "Arrived on time"))
            dao.insertAttendance(AttendanceRecord(studentId = "STD-102", teacherId = "TCH-002", classDate = "2026-10-08", status = "PRESENT", notes = "Attentive"))
            dao.insertAttendance(AttendanceRecord(studentId = "STD-103", teacherId = "TCH-001", classDate = "2026-10-07", status = "LATE", notes = "Joined 5 mins late with excuse"))

            // 8. Prepopulated Announcements
            dao.insertAnnouncement(AnnouncementRecord(
                titleEn = "Welcome to Jamia Al-Bayan Global Portal",
                titleBn = "জামিয়া আল-বায়ান গ্লোবাল পোর্টালে স্বাগতম",
                contentEn = "We are pleased to introduce our secure, Apple-inspired academic management portal with zero-trust privacy safeguarding our students.",
                contentBn = "শিক্ষার্থীদের সর্বোচ্চ গোপনীয়তা ও আধুনিক ব্যবস্থাপনার সাথে আমাদের একাডেমি পোর্টালে সকলকে স্বাগতম।",
                authorName = "Dr. Ahmad Al-Bayan (Principal)",
                isPinned = true
            ))
            dao.insertAnnouncement(AnnouncementRecord(
                titleEn = "Monthly Tajweed & Makhraj Assessment",
                titleBn = "মাসিক তাজবীদ ও মাখরাজ মূল্যায়ন পরীক্ষা",
                contentEn = "All assigned teachers are requested to submit comprehensive goal achievements by the 25th of this month.",
                contentBn = "সকল শ্রদ্ধেয় উস্তাদদের চলতি মাসের ২৫ তারিখের মধ্যে শিক্ষার্থীদের অগ্রগতি জমা দেওয়ার অনুরোধ করা হচ্ছে।",
                authorName = "Academic Dean Office",
                isPinned = false
            ))

            // 9. Structured Qaida Lessons
            val qaidaList = listOf(
                QaidaLesson(
                    lessonNumber = 1,
                    titleEn = "Lesson 1: The Arabic Alphabet (Mufradat)",
                    titleBn = "১ম পাঠ: আরবি বর্ণমালা (মুফরাদাত)",
                    arabicContent = "ا  ب  ت  ث  ج  ح  خ  د  ذ  ر  ز  س  ش  ص  ض  ط  ظ  ع  غ  ف  ق  ك  ل  م  ن  و  ه  ي",
                    descriptionEn = "Recognizing all 29 individual Arabic letters with distinct articulation points (Makharij).",
                    descriptionBn = "২৯টি পৃথক আরবি অক্ষরের সঠিক উচ্চারণ ও মাখরাজ চেনা।",
                    category = "Alphabets",
                    isCompleted = true
                ),
                QaidaLesson(
                    lessonNumber = 2,
                    titleEn = "Lesson 2: Compound Letters (Murakkabat)",
                    titleBn = "২য় পাঠ: যুক্ত বর্ণমালা (মুরাক্কাবাত)",
                    arabicContent = "لا  با  تا  ثا  جا  حا  خা  بل  تل  ثل  جل  حل  خل  يج  تح  نج",
                    descriptionEn = "Identifying letter shapes at the beginning, middle, and end of joined letter groups.",
                    descriptionBn = "শুরু, মধ্য ও শেষ অবস্থানে যুক্ত অক্ষরের রূপ শনাক্তকরণ।",
                    category = "Joint Letters",
                    isCompleted = true
                ),
                QaidaLesson(
                    lessonNumber = 3,
                    titleEn = "Lesson 3: Harakat - Fathah (Zabar)",
                    titleBn = "৩য় পাঠ: হারাকাত - ফাতহাহ (যবর)",
                    arabicContent = "أَ  بَ  تَ  ثَ  جَ  حَ  خَ  دَ  ذَ  رَ  زَ  سَ  شَ  صَ  ضَ  طَ  ظَ  عَ  غَ  فَ  قَ  كَ  لَ  مَ  نَ  وَ  هَ  يَ",
                    descriptionEn = "Short vowel 'A' sound pronounced briskly without stretching.",
                    descriptionBn = "যবরের সংক্ষিপ্ত 'আ' স্বরধ্বনি টান ছাড়া দ্রুত উচ্চারণ।",
                    category = "Harakat",
                    isCompleted = true
                ),
                QaidaLesson(
                    lessonNumber = 4,
                    titleEn = "Lesson 4: Harakat - Kasrah (Zer)",
                    titleBn = "৪র্থ পাঠ: হারাকাত - কাসরাহ (যের)",
                    arabicContent = "إِ  بِ  تِ  ثِ  جِ  حِ  خِ  دِ  ذِ  رِ  زِ  سِ  شِ  صِ  ضِ  طِ  ظِ  عِ  غِ  فِ  قِ  كِ  لِ  مِ  نِ  وِ  هِ  يِ",
                    descriptionEn = "Short vowel 'I' sound pronounced clearly with a smile position.",
                    descriptionBn = "যেরের সংক্ষিপ্ত 'ই' স্বরধ্বনি পরিষ্কার ও সুন্দরভাবে উচ্চারণ।",
                    category = "Harakat",
                    isCompleted = false
                ),
                QaidaLesson(
                    lessonNumber = 5,
                    titleEn = "Lesson 5: Harakat - Dammah (Pesh)",
                    titleBn = "৫ম পাঠ: হারাকাত - দাম্মাহ (পেশ)",
                    arabicContent = "أُ  بُ  تُ  ثُ  جُ  حُ  خُ  دُ  ذُ  رُ  زُ  سُ  شُ  صُ  ضُ  طُ  ظُ  عُ  غُ  فُ  قُ  كُ  لُ  مُ  نُ  وُ  هُ  يُ",
                    descriptionEn = "Short vowel 'U' sound pronounced with rounded lips without elongation.",
                    descriptionBn = "পেশের সংক্ষিপ্ত 'উ' স্বরধ্বনি ঠোঁট গোল করে উচ্চারণ।",
                    category = "Harakat",
                    isCompleted = false
                ),
                QaidaLesson(
                    lessonNumber = 6,
                    titleEn = "Lesson 6: Tanween (Double Vowels)",
                    titleBn = "৬ষ্ঠ পাঠ: তানবীন (দুই যবর, দুই যের, দুই পেশ)",
                    arabicContent = "بً  بٍ  بٌ  |  تً  تٍ  تٌ  |  ثً  ثٍ  ثٌ  |  رً  رٍ  رٌ",
                    descriptionEn = "Double Fathah, Double Kasrah, and Double Dammah containing hidden Noon Saakin.",
                    descriptionBn = "দুই যবর, দুই যের ও দুই পেশে লুকায়িত নূন সাকিনের ধ্বনি।",
                    category = "Tanween",
                    isCompleted = false
                ),
                QaidaLesson(
                    lessonNumber = 7,
                    titleEn = "Lesson 7: Sukoon / Jazm (Resting Mark)",
                    titleBn = "৭ম পাঠ: সাকীন বা জযম",
                    arabicContent = "أَبْ  أَتْ  أَثْ  أَجْ  أَحْ  أَخْ  أَدْ  أَذْ  أَرْ  أَزْ  أَسْ  أَشْ",
                    descriptionEn = "The letter connected with the preceding moving letter, experiencing a pause.",
                    descriptionBn = "পূর্ববর্তী হরফের সাথে মিলিয়ে বিশ্রামমূলক স্থির উচ্চারণ।",
                    category = "Sukoon",
                    isCompleted = false
                )
            )
            dao.insertQaidaLessons(qaidaList)

            // 10. Authentic Daily Duas Library
            val duasList = listOf(
                DailyDua(
                    category = "Sleep & Waking",
                    titleEn = "Upon Waking Up",
                    titleBn = "ঘুম থেকে জাগ্রত হওয়ার দুআ",
                    arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                    transliteration = "Alhamdu lillahilladhee ahyaanaa ba'da maa amaatanaa wa ilayhin-nushoor.",
                    meaningEn = "Praise is to Allah Who gave us life after having given us death, and unto Him is the resurrection.",
                    meaningBn = "সমস্ত প্রশংসা আল্লাহর জন্য, যিনি আমাদেরকে মৃত্যু (ঘুম) দেওয়ার পর পুনরায় জীবন দান করলেন এবং তাঁর দিকেই প্রত্যাবর্তন।",
                    reference = "Sahih al-Bukhari (6312)",
                    isFavorite = true
                ),
                DailyDua(
                    category = "Food & Drink",
                    titleEn = "Before Eating",
                    titleBn = "খাবার শুরুর পূর্বে দুআ",
                    arabicText = "بِسْمِ اللَّهِ",
                    transliteration = "Bismillah.",
                    meaningEn = "In the name of Allah.",
                    meaningBn = "আল্লাহর নামে শুরু করছি।",
                    reference = "Sahih al-Bukhari (5376)",
                    isFavorite = true
                ),
                DailyDua(
                    category = "Food & Drink",
                    titleEn = "After Finishing Meal",
                    titleBn = "খাবার শেষ করার পর দুআ",
                    arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
                    transliteration = "Alhamdu lillahilladhee at'amanaa wa saqaanaa wa ja'alanaa muslimeen.",
                    meaningEn = "Praise be to Allah Who has fed us and given us drink and made us Muslims.",
                    meaningBn = "সকল প্রশংসা আল্লাহর জন্য, যিনি আমাদের খাওয়ালেন, পান করালেন এবং মুসলিম বানালেন।",
                    reference = "Sunan Abu Dawud (3850), Jami` at-Tirmidhi (3457)",
                    isFavorite = false
                ),
                DailyDua(
                    category = "Knowledge & Study",
                    titleEn = "Seeking Increase in Knowledge",
                    titleBn = "ইলম ও জ্ঞান বৃদ্ধির দুআ",
                    arabicText = "رَبِّ زِدْنِي عِلْمًا",
                    transliteration = "Rabbi zidnee 'ilmaa.",
                    meaningEn = "My Lord, increase me in knowledge.",
                    meaningBn = "হে আমার প্রতিপালক! আমার জ্ঞান বৃদ্ধি করে দিন।",
                    reference = "Quran, Surah Ta-Ha (20:114)",
                    isFavorite = true
                ),
                DailyDua(
                    category = "Sleep & Waking",
                    titleEn = "Before Sleeping",
                    titleBn = "ঘুমানোর পূর্বে দুআ",
                    arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                    transliteration = "Bismikallahumma amootu wa ahyaa.",
                    meaningEn = "In Your name, O Allah, I die and I live.",
                    meaningBn = "হে আল্লাহ! আপনারই নামে আমি মৃত্যুবরণ (ঘুমাই) করি এবং জীবিত (জাগ্রত) হই।",
                    reference = "Sahih al-Bukhari (6324)",
                    isFavorite = false
                ),
                DailyDua(
                    category = "Travel & Safety",
                    titleEn = "Supplication for Travelling",
                    titleBn = "যানবাহনে আরোহণ ও সফরের দুআ",
                    arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
                    transliteration = "Subhaanal-ladhee sakhkhara lanaa haadhaa wa maa kunnaa lahu muqrineen, wa innaa ilaa rabbinaa lamunqaliboon.",
                    meaningEn = "Glory to Him Who has subjected this to us, and we could never have achieved it by our own efforts; and verily to our Lord we shall return.",
                    meaningBn = "পবিত্র সেই সত্তা যিনি এটিকে আমাদের বশীভূত করে দিয়েছেন, অথচ আমরা এটিকে বশীভূত করতে সক্ষম ছিলাম না; এবং আমরা আমাদের প্রতিপালকের দিকেই প্রত্যাবর্তনকারী।",
                    reference = "Sahih Muslim (1342)",
                    isFavorite = true
                ),
                DailyDua(
                    category = "Prayer & Worship",
                    titleEn = "Stepping Out of the Home",
                    titleBn = "ঘর থেকে বের হওয়ার সময় দুআ",
                    arabicText = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                    transliteration = "Bismillahi tawakkaltu 'alallahi, laa hawla wa laa quwwata illaa billaah.",
                    meaningEn = "In the name of Allah, I place my trust in Allah; there is no might nor power except with Allah.",
                    meaningBn = "আল্লাহর নামে, আল্লাহর ওপরই ভরসা করলাম। কোনো উপায় ও শক্তি নেই আল্লাহর সাহায্য ব্যতীত।",
                    reference = "Sunan Abu Dawud (5095), Jami` at-Tirmidhi (3426)",
                    isFavorite = false
                )
            )
            dao.insertDuas(duasList)

            // 11. Audit Log record
            dao.insertAuditLog(AuditLog(
                action = "SYSTEM_INITIALIZE",
                performedBy = "SYSTEM",
                targetEntity = "ACADEMY_BOOTSTRAP",
                details = "Initialized Jamia Al-Bayan Global schema with zero-trust role segregation."
            ))
        }
    }
}
