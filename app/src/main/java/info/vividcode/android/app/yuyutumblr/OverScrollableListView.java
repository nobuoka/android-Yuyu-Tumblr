package info.vividcode.android.app.yuyutumblr;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

public class OverScrollableListView extends ListView {

    public OverScrollableListView(Context context) {
        super(context);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    public OverScrollableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    public OverScrollableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
            int scrollY, int scrollRangeX, int scrollRangeY,
            int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

        // オーバーライド
        return super.overScrollBy(0, deltaY, 0, scrollY, 0, scrollRangeY, 0,
                200, isTouchEvent);
    }

    private Runnable mOverScrolledEventListener;
    public void setOverScrolledEventListener(Runnable listener) {
        mOverScrolledEventListener = listener;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
            boolean clampedY) {

        Log.v("myListView", "scrollX:" + scrollX + " scrollY:" + scrollY
                + " clampedX:" + clampedX + " clampedY:" + clampedX);

        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (mOverScrolledEventListener != null) mOverScrolledEventListener.run();
    }
}
