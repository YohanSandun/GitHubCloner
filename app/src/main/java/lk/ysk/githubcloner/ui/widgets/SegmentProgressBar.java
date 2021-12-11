package lk.ysk.githubcloner.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SegmentProgressBar extends View {

    private final List<Pair<Integer, Float>> segments = new ArrayList<>();
    private final Paint paint = new Paint();

    public SegmentProgressBar(Context context) {
        super(context);
    }

    public SegmentProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SegmentProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SegmentProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addSegment(Integer color, float percentage) {
        segments.add(new Pair<>(color, percentage));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GREEN);
        float left = 0;
        for (Pair<Integer, Float> pair : segments) {
            paint.setColor(pair.first);
            float right = getWidth()*pair.second;
            canvas.drawRect(right+left, 0, left, getHeight(), paint);
            left += right;
        }
    }
}
