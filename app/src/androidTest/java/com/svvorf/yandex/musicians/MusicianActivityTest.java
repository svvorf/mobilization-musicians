package com.svvorf.yandex.musicians;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.svvorf.yandex.musicians.activities.MusicianActivity;
import com.svvorf.yandex.musicians.matchers.Matchers;
import com.svvorf.yandex.musicians.models.Musician;
import com.svvorf.yandex.musicians.models.RealmString;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class MusicianActivityTest {

    Musician testMusician;

    RealmString testGenre1;
    RealmString testGenre2;

    Realm realm;
    int musicianId; //is used while deleting the musician from the database after test

    @Before
    public void initMusician() {

        RealmConfiguration config = new RealmConfiguration.Builder(InstrumentationRegistry.getInstrumentation()
                .getTargetContext()).name("test.realm").inMemory().build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        testMusician = realm.createObject(Musician.class);
        testMusician.setId(13371337);
        musicianId = testMusician.getId();
        testMusician.setName("David Gilmour");
        testMusician.setDescription("David Jon Gilmour, CBE (born 6 March 1946), is an English singer, songwriter, composer, multi-instrumentalist, and record producer. In a career spanning more than 50 years, Gilmour is best known[1] for his work as the guitarist and co-lead vocalist of the progressive rock band Pink Floyd. It was estimated that by 2012 the group had sold over 250 million records worldwide, including 75 million units sold in the United States.[2]");
        testMusician.setTracks(100);
        testMusician.setBigCover("https://upload.wikimedia.org/wikipedia/commons/3/34/David_Gilmour_and_stratocaster.jpg");
        testMusician.setLink("http://davidgilmour.com");
        testGenre1 = realm.createObject(RealmString.class);
        testGenre1.setValue("progressive rock");
        testGenre2 = realm.createObject(RealmString.class);
        testGenre2.setValue("art rock");
        RealmList<RealmString> testGenres = new RealmList<>(testGenre1, testGenre2);
        testMusician.setGenres(testGenres);
        testMusician.setAlbums(10);

        realm.commitTransaction();
    }


    @Rule
    public ActivityTestRule<MusicianActivity> mActivityRule =
            new ActivityTestRule<>(MusicianActivity.class, true, false);


    @Test
    public void test_musicianInformationIsProperlyShown() {
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();

        Intent intent = new Intent(targetContext, MusicianActivity.class);
        intent.putExtra("id", testMusician.getId());

        mActivityRule.launchActivity(intent);

        onView(withId(R.id.collapsing_toolbar)).check(matches(Matchers.withCollapsingToolbarTitle(is(testMusician.getName()))));
        onView(withId(R.id.description)).check(matches(withText(testMusician.getDescription())));
        onView(withId(R.id.statistics)).check(matches(withText(targetContext.getString(R.string.statistics, testMusician.getAlbums(), testMusician.getTracks()))));
        onView(withId(R.id.link)).check(matches(withText(testMusician.getLink())));

        onView(withId(R.id.genres_container))
                .check(matches(withNumberOfChildren(is(2))))
                .check(matches(withNthChildsText(is("progressive rock"), 0)))
                .check(matches(withNthChildsText(is("art rock"), 1)));
    }


    /**
     * Returns a matcher for a number of children of a ViewGroup.
     */
    public Matcher<View> withNumberOfChildren(final Matcher<Integer> numChildrenMatcher) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                return view instanceof ViewGroup && numChildrenMatcher.matches(((ViewGroup) view).getChildCount());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" a view with # children is ");
                numChildrenMatcher.describeTo(description);
            }
        };
    }

    /**
     * Returns a matcher for a number of children of a ViewGroup.
     */
    public Matcher<View> withNthChildsText(final Matcher<String> text, final int childPosition) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                return view instanceof ViewGroup
                        && ((ViewGroup) view).getChildAt(childPosition) instanceof TextView
                        && text.matches(((TextView) (((ViewGroup) view).getChildAt(childPosition))).getText());

            }

            @Override
            public void describeTo(Description description) {
                description.appendText(childPosition + " child's text ");
                text.describeTo(description);
            }
        };
    }
}
