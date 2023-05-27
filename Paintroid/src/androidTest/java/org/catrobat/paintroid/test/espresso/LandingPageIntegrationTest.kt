package org.catrobat.paintroid.test.espresso

import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.catrobat.paintroid.LandingPage
import org.catrobat.paintroid.R
import org.catrobat.paintroid.test.utils.ScreenshotOnFailRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingPageIntegrationTest{

    @get:Rule
    var launchActivityRule = ActivityTestRule(LandingPage::class.java)

    @get:Rule
    var screenshotOnFailRule = ScreenshotOnFailRule()

    @Test
    fun testTopAppBarDisplayed(){
        Espresso.onView(ViewMatchers.isAssignableFrom(Toolbar::class.java))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testAppBarTitleDisplayPocketPaint() {
        Espresso.onView(ViewMatchers.withText("Pocket Paint"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testTwoFABDisplayed(){
        Espresso.onView(ViewMatchers.withId(R.id.fab_pocket_paint_open_new_project))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.fab_pocket_paint_load_project))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testMyProjectsTextDisplayed(){
        Espresso.onView(ViewMatchers.withText("My Projects"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}