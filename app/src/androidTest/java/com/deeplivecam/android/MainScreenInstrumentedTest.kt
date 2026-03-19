package com.deeplivecam.android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deeplivecam.android.ui.theme.DeepLiveCamTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for core UI features
 * Run on device or emulator
 */
@RunWith(AndroidJUnit4::class)
class MainScreenInstrumentedTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testWelcomeScreenDisplayed() {
        composeTestRule.setContent {
            DeepLiveCamTheme {
                com.deeplivecam.android.ui.MainScreen()
            }
        }
        
        // Check that welcome screen is displayed initially
        composeTestRule.onNodeWithText("Deep Live Cam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Source Face").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Camera").assertIsDisplayed()
    }
    
    @Test
    fun testEthicalWarningDisplayed() {
        composeTestRule.setContent {
            DeepLiveCamTheme {
                com.deeplivecam.android.ui.MainScreen()
            }
        }
        
        // Check that ethical warning is shown
        composeTestRule.onNodeWithText("Use ethically. Obtain consent. Label as deepfake.")
            .assertIsDisplayed()
    }
    
    @Test
    fun testStartCameraButton() {
        composeTestRule.setContent {
            DeepLiveCamTheme {
                com.deeplivecam.android.ui.MainScreen()
            }
        }
        
        // Test that start camera button is clickable
        composeTestRule.onNodeWithText("Start Camera").assertHasClickAction()
    }
    
    @Test
    fun testSelectSourceFaceButton() {
        composeTestRule.setContent {
            DeepLiveCamTheme {
                com.deeplivecam.android.ui.MainScreen()
            }
        }
        
        // Test that select source face button is clickable
        composeTestRule.onNodeWithText("Select Source Face").assertHasClickAction()
    }
}
