package github.hmasum18.googlemaptutorial.util;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class AnimationUtils {
    public static ValueAnimator polyLineAnimator(){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,100);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(5000);
        return valueAnimator;
    }

    public static ValueAnimator satelliteAnimation(){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1f);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(30000);
        return valueAnimator;
    }

}
