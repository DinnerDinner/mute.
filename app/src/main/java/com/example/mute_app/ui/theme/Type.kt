package com.example.mute_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mute_app.R

// Define the Comfortaa FontFamily
val Comfortaa = FontFamily(
    Font(R.font.comfortaa_light, FontWeight.Light),
    Font(R.font.comfortaa_regular, FontWeight.Normal),
    Font(R.font.comfortaa_bold, FontWeight.Bold)
)

// Apply Comfortaa to all key text styles
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Comfortaa,
        fontWeight = FontWeight.Light,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
