package com.kjipo.timetracker

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppStartTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsAndShowsTasks() {
        // "Tasks" should be visible in the top bar or as a heading
        // Based on previous knowledge of the app
        composeTestRule.onNodeWithText("Tasks").assertExists()
    }
}
