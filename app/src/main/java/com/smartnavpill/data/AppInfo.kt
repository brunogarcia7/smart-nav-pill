package com.smartnavpill.data

import android.graphics.drawable.Drawable

/** Representa um app instalado que pode ser selecionado para um gesto. */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
)
