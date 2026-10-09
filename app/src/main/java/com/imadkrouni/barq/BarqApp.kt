package com.imadkrouni.barq

import android.app.Application
import com.imadkrouni.barq.data.AppDatabase

class BarqApp : Application() {
    val database by lazy { AppDatabase.get(this) }
}
