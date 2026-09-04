package com.mealcycle.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class that initializes Hilt dependency injection.
 * Must be registered in AndroidManifest.xml via android:name=".MealCycleApp"
 */
@HiltAndroidApp
class MealCycleApp : Application()
