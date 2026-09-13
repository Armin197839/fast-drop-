package com.example.model

enum class BackgroundTheme(
    val id: String,
    val title: String,
    val description: String,
    val isDark: Boolean
) {
    AURORA_BLUE(
        id = "aurora_blue",
        title = "شفق قطبی آبی (Aurora)",
        description = "موج‌های سیال نوری سفید و آبی کریستالی",
        isDark = false
    ),
    CYBER_NEON(
        id = "cyber_neon",
        title = "سایبر نئون تکنو (Cyber Grid)",
        description = "شبکه پرسرعت داده‌ها و نورهای فیروزه‌ای",
        isDark = true
    ),
    FROST_MINIMAL(
        id = "frost_minimal",
        title = "شیشه‌ای سفید مات (Frost Glass)",
        description = "سفید یخچالی تمیز با هاله‌های نوری معلق",
        isDark = false
    ),
    MIDNIGHT_PULSE(
        id = "midnight_pulse",
        title = "آسمان شب و پالس الکتریک",
        description = "سرمه‌ای و آبی نئون با امواج رادار زنده",
        isDark = true
    ),
    ROYAL_CYAN(
        id = "royal_cyan",
        title = "اقیانوس الکتریک (Royal Cyan)",
        description = "طیف لاجوردی و فیروزه‌ای پرانرژی",
        isDark = false
    )
}
