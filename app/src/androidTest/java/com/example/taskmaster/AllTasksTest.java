package com.example.taskmaster;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AllTasksTest {
    @Rule
    public ActivityTestRule<AllTasks> activityRule =
            new ActivityTestRule<>(AllTasks.class);

    @Test
    public void headingIsVisibleOnAllTasks() {
        onView(withText("All Tasks")).check(matches(isDisplayed()));
    }
}
