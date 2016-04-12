package com.svvorf.yandex.musicians.misc;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.drawable.Drawable;

/**
 * Provides static methods to animate specific parts of the app's UI.
 */
public class AnimationManager {

    private static final int FRAGMENT_TRANSITION_DURATION = 200;

    public enum FadeDirection {
        OPAQUE, TRANSPARENT
    }

    /**
     * Fades a drawable in/out in the specified direction.
     * @param drawable The drawable to fade.
     * @param fadeDirection The direction for the animation. See {@link FadeDirection}
     */
    public static void fadeDrawable(Drawable drawable, FadeDirection fadeDirection) {
        PropertyValuesHolder propertyValuesHolder = null;
        switch (fadeDirection) {
            case OPAQUE:
                propertyValuesHolder = PropertyValuesHolder.ofInt("alpha", 0, 255);
                break;
            case TRANSPARENT:
                propertyValuesHolder = PropertyValuesHolder.ofInt("alpha", 255, 0);
                break;
        }

        ObjectAnimator animator = ObjectAnimator
                .ofPropertyValuesHolder(drawable, propertyValuesHolder);
        animator.setDuration(FRAGMENT_TRANSITION_DURATION);
        animator.start();
    }
}
