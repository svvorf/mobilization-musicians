package com.svvorf.yandex.musicians;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.svvorf.yandex.musicians.activities.MainActivity;
import com.svvorf.yandex.musicians.adapters.MusiciansAdapter;
import com.svvorf.yandex.musicians.models.Musician;
import com.svvorf.yandex.musicians.models.RealmString;
import com.svvorf.yandex.musicians.network.RequestManager;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private String mockResponse;

    private ArrayList<Musician> musicians = new ArrayList<>();
    private Context targetContext;

    private final int TEST_ITEM_POSITION = 4;
    private Activity currentActivity;

    @Before
    public void initTestMusicians() throws IOException {
        RealmConfiguration config = new RealmConfiguration.Builder(getInstrumentation()
                .getTargetContext()).name("test.realm").inMemory().build();
        Realm.setDefaultConfiguration(config);

        StringBuilder mockResponseStringBuilder = new StringBuilder("[");
        for (int i = 1; i <= 10; i++) {
            Musician testMusician = new Musician();
            testMusician.setId(i);
            testMusician.setName("Musician " + i);
            testMusician.setDescription("A description for musician " + i);
            testMusician.setSmallCover("http://www.4-concepts.com/gulfspic/wp-content/uploads/2014/02/default_image_01-300x300.png");
            testMusician.setAlbums((int) (Math.random() * 100));
            testMusician.setTracks((int) (Math.random() * 1000));
            testMusician.setGenres(new RealmList<>(new RealmString("genre 1"), new RealmString("genre 2")));

            musicians.add(testMusician);

            mockResponseStringBuilder.append("{\"id\":")
                    .append(testMusician.getId()).append(",\"name\":\"")
                    .append(testMusician.getName())
                    .append("\",\"genres\":[\"genre 1\", \"genre 2\"]").append(",\"tracks\":")
                    .append(testMusician.getTracks()).append(",\"albums\":")
                    .append(testMusician.getAlbums()).append(",\"cover\":{\"small\":\"")
                    .append(testMusician.getSmallCover()).append("\",\"big\":\"")
                    .append(testMusician.getSmallCover()).append("\"}, \"link\":\"\", \"description\":\"")
                    .append(testMusician.getDescription()).append("\"},");
        }
        mockResponse = mockResponseStringBuilder.toString().substring(0, mockResponseStringBuilder.length() - 1) + "]";


        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(mockResponse));

        server.start();

        targetContext = getInstrumentation()
                .getTargetContext();

        RequestManager requestManager = RequestManager.getInstance();
        requestManager.initialize(targetContext, server.url("/").toString());


        mActivityRule.launchActivity(new Intent(targetContext, MainActivity.class));
    }

    @After
    public void tearDown() {
        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().clear(Musician.class);
        Realm.getDefaultInstance().clear(RealmString.class);
        Realm.getDefaultInstance().commitTransaction();

    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, false);

    @Test
    public void test_loadsMusiciansAndDisplaysCorrectly() {
        checkAllRecyclerViewItems();
    }


    @Test
    public void test_showsCorrectSearchResults() {
        inputSearchQuery(TEST_ITEM_POSITION);
        onView(withId(R.id.musicians_list)).check(matches(atPosition(0, musicians.get(TEST_ITEM_POSITION))));
    }


    @Test
    public void test_listComesBackAfterClosingSearchView() {
        inputSearchQuery(TEST_ITEM_POSITION);
        onView(withContentDescription("Clear query")).perform(click(), click());
        checkAllRecyclerViewItems();
    }

    @Test
    public void test_backButtonClosesSearch() {
        inputSearchQuery(TEST_ITEM_POSITION);
        Espresso.pressBack(); // this one for closing a keyboard
        Espresso.pressBack();
        checkAllRecyclerViewItems();
    }

    @Test
    public void test_itemClickOpensRightMusician() {
        onView(withId(R.id.musicians_list)).perform(RecyclerViewActions.actionOnItemAtPosition(TEST_ITEM_POSITION, click()));
        onView(withId(R.id.description)).check(matches(withText(musicians.get(TEST_ITEM_POSITION).getDescription())));
    }

    /**
     * Finds SearchView and inputs "Musician" + number
     *
     * @param number - the number of a musician
     */
    private void inputSearchQuery(int number) {
        onView(withId(R.id.search)).perform(click());
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText("Musician " + (number + 1)));
    }

    /**
     * Checks if all 10 mock musicians are shown in the RecyclerView
     */
    private void checkAllRecyclerViewItems() {
        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.musicians_list)).perform(scrollToPosition(i)).check(matches(atPosition(i, musicians.get(i))));
        }
    }

    /**
     * Matches if a given musician is on a given position in a recycler view
     */
    private Matcher<View> atPosition(final int position, final Musician musician) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("item's data at position " + position + " ");
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                MusiciansAdapter.ViewHolder viewHolder = (MusiciansAdapter.ViewHolder) view.findViewHolderForAdapterPosition(position);
                String genresString = MusiciansAdapter.getGenresString(musician.getGenres());

                return viewHolder != null
                        && ((TextView) ButterKnife.findById(viewHolder.itemView, R.id.name)).getText().equals(musician.getName())
                        && ((TextView) ButterKnife.findById(viewHolder.itemView, R.id.genres)).getText().equals(genresString)
                        && ((TextView) ButterKnife.findById(viewHolder.itemView, R.id.statistics)).getText()
                        .equals(targetContext.getResources().getString(R.string.statistics, musician.getAlbums(), musician.getTracks()));


            }
        };
    }


    public Activity getActivityInstance() {

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity;
    }
}
