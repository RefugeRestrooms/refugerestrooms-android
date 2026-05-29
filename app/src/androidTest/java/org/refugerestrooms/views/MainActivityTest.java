package org.refugerestrooms.views;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void init(){
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.getSupportFragmentManager().beginTransaction();
        });
    }

    @Test
    public void TestAutoComplete(){

    }
}
