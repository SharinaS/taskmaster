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
public class AddTaskTest {

    @Rule
    public ActivityTestRule<AddTask> activityRule =
            new ActivityTestRule<>(AddTask.class);


    @Test
    public void headingIsVisibleOnAddTaskView() {
        onView(withText("Total tasks:")).check(matches(isDisplayed()));
    }
}
