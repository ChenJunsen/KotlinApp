package com.cjs.ultraflowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 自定义流式布局
 *
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/11/11 17:18
 */
public class UltraFlowLayout extends ViewGroup {

    /**
     * 标记每行的最大高度 并以此作为行高
     */
    private SparseIntArray maxHeightArray;

    /**
     * 子View的对齐方式
     */
    private @Align
    int align = Align.TOP;
    /**
     * 子View之间通用的横向间距，可以与其margin值叠加
     */
    private int gapHorizontal;
    /**
     * 子view之间的通用的纵向间距，可以与其margin值叠加
     */
    private int gapVertical;

    public UltraFlowLayout(Context context) {
        super(context);
        initAttr(context, null, 0, 0);
    }

    public UltraFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs, 0, 0);
    }

    public UltraFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UltraFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 初始化控件属性
     *
     * @param context      _
     * @param attrs        _
     * @param defStyleAttr _
     * @param defStyleRes  _
     */
    private void initAttr(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UltraFlowLayout, defStyleAttr, defStyleRes);
            gapHorizontal = array.getDimensionPixelOffset(R.styleable.UltraFlowLayout_gap_horizontal, 0);
            gapVertical = array.getDimensionPixelOffset(R.styleable.UltraFlowLayout_gap_vertical, 0);
            align = array.getInt(R.styleable.UltraFlowLayout_align, Align.TOP);
            array.recycle();
        }
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
        int currentColumnNo = 0;//当前子View在其所在行的第几列
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            //由于我们重写了generateLayoutParams方法，所以可以强转子view的布局参数为UltraFlowLayout.LayoutParams
            LayoutParams mlp = (LayoutParams) child.getLayoutParams();
            int childMarginLeft = mlp.leftMargin;
            int childMarginRight = mlp.rightMargin;
            int childMarginTop = mlp.topMargin;
            int childMarginBottom = mlp.bottomMargin;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int _childWidth = childMarginLeft + childWidth + childMarginRight + (currentColumnNo != 0 ? gapHorizontal : 0);
            if (currentRowWidth + _childWidth > widthMax) {//换行
                maxHeightArray.put(rows, currentRowMaxHeight);//换行时，先标记一下之前行的最大高度。哪个子元素的高度最大就去其作为行高
                rows++;//行计数器自增，偏移至下一行
                currentRowTop += currentRowMaxHeight;//标记下一行起始绘制的top
                currentRowWidth = 0;//重置行宽
                currentRowMaxHeight = 0;//重置行最大高度
                currentColumnNo = 0;//重置列数
                _childWidth = childMarginLeft + childWidth + childMarginRight;//关键点 换行时，重新计算这个child的实际占用宽度，这个宽在下面代码中会被复用。换行之前可能加上了gap，但是不一定那行放得下。换行后，从第一个开始，肯定是不要gap的。
            }
            int _childHeight = childMarginTop + childHeight + childMarginBottom + (rows != 0 ? gapVertical : 0);//child实际占用的高度空间，需要加上上下间距值
            //默认状态
            currentRowMaxHeight = Math.max(currentRowMaxHeight, _childHeight);//当前child的实际占高与当前已经存在的最大行高作比较，取大的那个作为新行高
            //接下来就是获取这个child的左上角及右下角点坐标的位置了
            //需要注意的是，实际摆放需要考虑margin值。因为我们每个子View已经算出了总的占用空间，这个空间包含了margin.而视觉上是看不见margin的，所以实际摆放要去除这个空间的margin值
            int l = currentRowWidth + childMarginLeft + (currentColumnNo != 0 ? gapHorizontal : 0);
            int t = currentRowTop + childMarginTop + (rows != 0 ? gapVertical : 0);
            int r = l + childWidth;
            int b = t + childHeight;
            currentRowWidth += _childWidth;
            //以静态id的形式设置tag,防止外部使用时占用默认tag
            child.setTag(R.id.rectId, new RectX(l, t, r, b, rows, currentColumnNo));//这里用到了一个技巧 因为layout时要传入左上右下四个数值，恰好系统的Rect就是存这四个值的模型。但是系统的Rect不能满足需求且不能被继承，所以仿写一个
            if (i == childCount - 1) {//还需要注意的是，如果没换行，但是测完了，此时也要标记一下当前行的行高
                maxHeightArray.put(rows, currentRowMaxHeight);
            }
            currentColumnNo++;//累加列数
        }
        return widthMeasureSpec;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            RectX rec = (RectX) child.getTag(R.id.rectId);
            int _gapVertical = rec.row == 0 ? 0 : gapVertical;//第一行的元素不设置gap
            LayoutParams mlp = (LayoutParams) child.getLayoutParams();
            int mAlign = (AlignSelf.INHERIT == mlp.alignSelf) ? align : mlp.alignSelf;
            if (Align.TOP == mAlign) {
                //因为measure阶段已经将所有的子View顶点位置标记出来了，所以如果是默认对齐方式，布局阶段就很简单了，直接取出标记进行摆放
                child.layout(rec.left, rec.top, rec.right, rec.bottom);
            } else {
                int rowMaxHeight = maxHeightArray.get(rec.row);
                int childHeight = child.getMeasuredHeight();
                int t0 = 0, b0 = 0;
                if (Align.CENTER == mAlign) {
                    int centerOffset = (rowMaxHeight - childHeight - _gapVertical) / 2;//计算出居中偏移量
                    t0 = rec.top + centerOffset;//在默认顶部居中模式下向下偏移
                    b0 = t0 + childHeight;
                } else if (Align.BOTTOM == mAlign) {
                    int bottomOffset = rowMaxHeight - childHeight - _gapVertical;//计算出底部对齐偏移量
                    t0 = rec.top + bottomOffset;//在默认顶部居中模式下向下偏移
                    b0 = t0 + childHeight;
                }
                child.layout(rec.left, t0, rec.right, b0);
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        //关键步骤 使得能把child的layoutParam转换为MarginLayoutParam
        return new UltraFlowLayout.LayoutParams(getContext(), attrs);
    }

    /**
     * 获取当前子View的对齐方式
     *
     * @return {@link Align#TOP}、{@link Align#CENTER}、{@link Align#BOTTOM}
     */
    @SuppressWarnings("unused")
    public @Align
    int getAlign() {
        return align;
    }

    /**
     * 设置对齐方式
     *
     * @param align {@link Align#TOP}、{@link Align#CENTER}、{@link Align#BOTTOM}
     */
    public void setAlign(@Align int align) {
        this.align = align;
        requestLayout();//requestLayout会重新调用onMeasure和onLayout
    }

    /**
     * 获取子View之间的通用间距
     *
     * @return 每行第一个没有间距
     */
    @SuppressWarnings("unused")
    public int getGapHorizontal() {
        return gapHorizontal;
    }

    /**
     * 设置横向间距 像素
     *
     * @param gapHorizontal 每行第一个没有间距,可以用padding设置
     */
    public void setGapHorizontal(int gapHorizontal) {
        this.gapHorizontal = gapHorizontal;
        requestLayout();
    }

    /**
     * 获取当前子View的纵向间距通用值
     *
     * @return 纵向间距
     */
    @SuppressWarnings("unused")
    public int getGapVertical() {
        return gapVertical;
    }

    /**
     * 获取当前子View的纵向间距通用值
     * 第一行的元素没有纵向间距值，可用padding值代替
     *
     * @param gapVertical 纵向间距 像素
     */
    @SuppressWarnings("unused")
    public void setGapVertical(int gapVertical) {
        this.gapVertical = gapVertical;
        requestLayout();
    }

    /**
     * 子控件的对齐方式限制
     */
    @IntDef({Align.TOP, Align.CENTER, Align.BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressWarnings("unused")
    public @interface Align {
        /**
         * 子View顶部对齐(默认)
         */
        int TOP = 0;
        /**
         * 子View居中对齐
         */
        int CENTER = 1;
        /**
         * 子View底部对齐
         */
        int BOTTOM = 2;
    }

    /**
     * 单独设置指定子View的对齐方式，其优先级高于{@link Align}
     */
    @IntDef({Align.TOP, Align.CENTER, Align.BOTTOM, AlignSelf.INHERIT})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressWarnings("unused")
    public @interface AlignSelf {
        /**
         * 子View顶部对齐
         */
        int TOP = Align.TOP;
        /**
         * 子View居中对齐
         */
        int CENTER = Align.CENTER;
        /**
         * 子View底部对齐
         */
        int BOTTOM = Align.BOTTOM;
        /**
         * 子View采用父布局的对齐方式进行对齐，父布局的默认对齐方式为{@link Align#TOP}
         */
        int INHERIT = -1;
    }


    /**
     * 因为系统的{@link Rect}为final方法，不能被继承改写，所以特此自定义该类，增加元素行列属性
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/11/12 13:56
     */
    public static class RectX {

        public int left, top, right, bottom, row, column;

        /**
         * 构建坐标信息
         *
         * @param left   左
         * @param top    上
         * @param right  右
         * @param bottom 下
         * @param row    所在行数(从零开始)
         * @param column 所在列数(从零开始)
         */
        public RectX(int left, int top, int right, int bottom, int row, int column) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.row = row;
            this.column = column;
        }

        @NonNull
        @Override
        public String toString() {
            return "RectX{" +
                    "left=" + left +
                    ", top=" + top +
                    ", right=" + right +
                    ", bottom=" + bottom +
                    ", row=" + row +
                    ", column=" + column +
                    '}';
        }

        /**
         * 返回元素长宽及坐标信息
         *
         * @return 格式化后的信息
         */
        @SuppressWarnings("unused")
        public String toWHString() {
            return String.format("位置信息:[%1$s,%2$s] 长度:%3$s 宽度:%4$s", row, column, right - left, bottom - top);
        }
    }

    /**
     * 子View的携带参数
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/11/17 11:12
     */
    public static class LayoutParams extends MarginLayoutParams {
        /**
         * 对齐方式
         */
        public @AlignSelf
        int alignSelf = AlignSelf.INHERIT;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.UltraFlowLayout_Layout);
            alignSelf = array.getInt(R.styleable.UltraFlowLayout_Layout_align_self, AlignSelf.INHERIT);
            array.recycle();
        }

        @SuppressWarnings("unused")
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings("unused")
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @SuppressWarnings("unused")
        public LayoutParams(LayoutParams source) {
            super(source);
            alignSelf = source.alignSelf;
        }
    }
}
