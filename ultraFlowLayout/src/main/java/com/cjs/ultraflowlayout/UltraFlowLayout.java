package com.cjs.ultraflowlayout;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

/**
 * 自定义流式布局
 *
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/11/11 17:18
 */
public class UltraFlowLayout extends ViewGroup {
    public UltraFlowLayout(Context context) {
        super(context);
    }

    public UltraFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UltraFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private SparseIntArray maxHeightArray;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UltraFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //1、关键步骤 测量所有子view 这样，后续才能从子view获取measuredWidth和measuredHeight
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //2、测量行宽 同时标记每行最大高度
        int wms = measureWidth(widthMeasureSpec);
        //3、测量高度 不确定模式下，累加最大行高
        int hms = measureHeight(heightMeasureSpec);

        super.onMeasure(wms, hms);
    }

    /**
     * 测量布局高度(包含子View的margin)。
     *
     * @param heightMeasureSpec 默认获取到的测量规格
     * @return 返回重组好的测量规格
     */
    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            //非精确模式下的高度累计就是把所有行的最大高度加起来就行了
            int totalHeight = 0;//累计高度
            for (int row = 0; row < maxHeightArray.size(); row++) {
                totalHeight += maxHeightArray.get(row);
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(totalHeight, heightMode);
        }
        return heightMeasureSpec;
    }

    /**
     * 测量布局宽度(包含了子View的margin)。WRAP_CONTENT模式下的宽度为屏幕宽度
     *
     * @param widthMeasureSpec 默认获取到的测量规格
     * @return 返回重组好的测量规格
     */
    private int measureWidth(int widthMeasureSpec) {
        if (maxHeightArray == null) {
            maxHeightArray = new SparseIntArray();
        } else {
            maxHeightArray.clear();
        }
        int rows = 0;//累计行数(实际行数+1，因为从0开始)，同时也是当前行数指示器
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthMax;//最大可用宽度
        if (widthMode == MeasureSpec.EXACTLY) {
            widthMax = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//精确数值和MATCH_PARENT模式下，取测量的宽度，去除布局自身的padding
        } else {
            widthMax = getContext().getResources().getDisplayMetrics().widthPixels - getPaddingLeft() - getPaddingRight();//不确定模式下取屏幕宽度为基准
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthMax, widthMode);
        }
        int childCount = getChildCount();//子View的数量
        int currentRowWidth = getPaddingLeft();//当前行的累计宽度 默认算了padding
        int currentRowTop = getPaddingTop();//当前行的起始高度
        int currentRowMaxHeight = 0;//当前行的最大高度
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            //由于我们重写了generateLayoutParams方法，所以可以强转子view的布局参数为MarginLayoutParams
            MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
            int childMarginLeft = mlp.leftMargin;
            int childMarginRight = mlp.rightMargin;
            int childMarginTop = mlp.topMargin;
            int childMarginBottom = mlp.bottomMargin;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int _childHeight = childMarginTop + childHeight + childMarginBottom;//child实际占用的高度空间，需要加上上下间距值
            if (currentRowWidth + childWidth > widthMax) {//换行
                maxHeightArray.put(rows, currentRowMaxHeight);//换行时，先标记一下之前行的最大高度。哪个子元素的高度最大就去其作为行高
                currentRowTop += _childHeight;//标记下一行起始绘制的top
                rows++;//行计数器自增，偏移至下一行
                currentRowWidth = 0;//重置行宽
                currentRowMaxHeight = 0;//重制行最大高度
            }
            //默认状态
            currentRowMaxHeight = Math.max(currentRowMaxHeight, _childHeight);//当前child的实际占高与当前已经存在的最大行高作比较，取大的那个作为新行高
            //接下来就是获取这个child的左上角及右下角点坐标的位置了
            int l = currentRowWidth;
            int t = currentRowTop;
            currentRowWidth += (childMarginLeft + childWidth + childMarginRight);
            int r = currentRowWidth;
            int b = t + _childHeight;
            child.setTag(new Rect(l, t, r, b));//这里用到了一个技巧 因为layout时要传入左上右下四个数值，恰好系统的Rect就是存这四个值的模型
            if (i == childCount - 1) {//还需要注意的是，如果没换行，但是测完了，此时也要标记一下当前行的行高
                maxHeightArray.put(rows, currentRowMaxHeight);
            }
        }
        return widthMeasureSpec;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //因为measure阶段已经将所有的子View顶点位置标记出来了，所以布局阶段就很简单了，直接取出标记进行摆放
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            Rect rec = (Rect) child.getTag();
            Log.d("FlowXX", String.format("[%1$s,%2$s]", rec.right - rec.left, rec.bottom - rec.top));
            child.layout(rec.left, rec.top, rec.right, rec.bottom);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        //关键步骤 使得能把child的layoutParam转换为MarginLayoutParam
        return new MarginLayoutParams(getContext(), attrs);
    }
}
