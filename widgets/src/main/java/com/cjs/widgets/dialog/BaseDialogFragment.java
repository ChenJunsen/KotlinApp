package com.cjs.widgets.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * <h1>窗体基类</h1>
 * 注意一些生命周期的调用顺序:<br>
 * onCreate -> onCreateDialog ->onCreateView<br>
 * <li>{@link #getDialog()}方法是在onCreateDialog才会有值的</li>
 *
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/9/16 11:15
 */
public abstract class BaseDialogFragment extends DialogFragment {

    /**
     * 默认的对话框tag
     */
    public static final String DEFAULT_TAG = "default_dialog_fragment";

    /**
     * 默认的弹窗动画样式
     */
    public static final int DEFAULT_ANIM_STYLE = -1;

    /**
     * 当前窗体的根视图
     */
    protected View rootView;


    /**
     * 窗体内部的dialog创建后的监听器 该方法位于{@link #onCreateView(LayoutInflater, ViewGroup, Bundle)},从生命周期上看，这里面的{@link #getDialog()}才不会为空
     */
    protected OnDialogCreatedListener onDialogCreatedListener;

    /**
     * 设置获取窗体布局Id
     *
     * @return 布局的资源id
     */
    public abstract @LayoutRes
    int getLayoutId();

    /**
     * 模板方法 初始化View
     *
     * @param savedInstanceState
     */
    public abstract void initView(Bundle savedInstanceState);

    /**
     * 模板方法 初始化控件事件
     *
     * @param savedInstanceState
     */
    public abstract void initEvents(Bundle savedInstanceState);

    private static final String TAG = "BDF";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach.getActivity->" + getActivity());
        Log.d(TAG, "onAttach.getDialog->" + getDialog());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate.getActivity->" + getActivity());
        Log.d(TAG, "onCreate.getDialog->" + getDialog());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog.getActivity->" + getActivity());
        Log.d(TAG, "onCreateDialog.getDialog->" + getDialog());
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (onDialogCreatedListener != null) {
            onDialogCreatedListener.onDialogCreated(dialog);
        }
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView.getActivity->" + getActivity());
        Log.d(TAG, "onCreateView.getDialog->" + getDialog());
        rootView = inflater.inflate(getLayoutId(), null);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated.getActivity->" + getActivity());
        Log.d(TAG, "onViewCreated.getDialog->" + getDialog());
        initWindow(Objects.requireNonNull(getDialog()).getWindow());
        initView(savedInstanceState);
        initEvents(savedInstanceState);
    }

    /**
     * 初始化窗体属性
     *
     * @param window
     */
    private void initWindow(Window window) {
        if (getAnimationStyle() != DEFAULT_ANIM_STYLE) {
            window.setWindowAnimations(getAnimationStyle());
        }
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = getGravity();
        lp.dimAmount = getDimAmount();
        lp.width = getWindowWidth();
        lp.height = getWindowHeight();
        Objects.requireNonNull(getDialog()).onWindowAttributesChanged(lp);
    }

    /**
     * show方法简写 显示对话框
     * <h2>PS：为什么不直接用{@link #getActivity()}去获取fragmentManager？</h3>
     * <h3>因为DialogFragment的创建顺序和Dialog有很大的别。在调用{@link #show(FragmentManager, String)}方法之前，内部是没有调用任何Fragment的生命周期
     * 的，更别说attach了，所以，只能外部哪里用哪里绑定
     * </h3>
     */
    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, DEFAULT_TAG);
    }

    /**
     * 自带转型的findViewById
     *
     * @param id
     * @param <T>
     * @return
     */
    protected <T extends View> T findViewById(@IdRes int id) {
        if (rootView != null) {
            return rootView.findViewById(id);
        } else {
            return null;
        }
    }

    /**
     * 设置窗体位置
     *
     * @return
     */
    protected int getGravity() {
        return Gravity.CENTER;
    }

    /**
     * 设置窗体宽度
     *
     * @return
     */
    protected int getWindowWidth() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    /**
     * 设置窗体高度
     *
     * @return
     */
    protected int getWindowHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    /**
     * 设置蒙层透明度
     *
     * @return 0到1取值
     */
    protected @DimAmount
    float getDimAmount() {
        return 0.6f;
    }

    /**
     * 设置弹窗动画样式
     *
     * @return 如果返回-1，表示使用默认的弹窗动画
     */
    protected int getAnimationStyle() {
        return DEFAULT_ANIM_STYLE;
    }

    /**
     * BaseDialogFragment内部窗体创建监听器
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/9/17 10:21
     */
    public interface OnDialogCreatedListener {
        /**
         * 内部窗体创建完成后<br>
         * 你会好奇为什么要写这个方法。因为Fragment创建和内部Dialog创建是两个过程，后者要晚一些。
         * 在以链式方式调用时，此时的{@link #getDialog()}是空的，所以需要这个方法来进行延时操作
         *
         * @param dialog dialogFragment内部的实际窗体
         */
        void onDialogCreated(Dialog dialog);
    }

    /**
     * 设置内部窗体创建的监听器
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/9/17 10:22
     */
    public void setOnDialogCreatedListener(OnDialogCreatedListener onDialogCreatedListener) {
        this.onDialogCreatedListener = onDialogCreatedListener;
    }

    /**
     * 窗体蒙层取值限定
     */
    @Retention(RetentionPolicy.SOURCE)
    @FloatRange(from = 0.0, to = 1.0)
    public @interface DimAmount {

    }

    /**
     * 链式写法构造器基类
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/9/17 15:26
     */
    public abstract static class BaseBuilder<D extends BaseDialogFragment, B extends BaseBuilder> {
        protected boolean cancelable = true;
        protected boolean outsideCancelable = true;
        protected D dialog;

        public BaseBuilder() {
            //通过反射方式，使得泛型D能创建实例对象
            Class<D> entityClass = (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            try {
                dialog = entityClass.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            }
        }

        public B setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return (B) this;
        }

        public B setOutsideCancelable(boolean outsideCancelable) {
            this.outsideCancelable = outsideCancelable;
            return (B) this;
        }

        /**
         * 将前面填写的参数构造进dialogFragment实体内，如需显示，还要调用相关的show方法
         * @return
         */
        public D build() {
            dialog.setCancelable(cancelable);
            dialog.setOnDialogCreatedListener(new OnDialogCreatedListener() {
                @Override
                public void onDialogCreated(Dialog dialog) {
//                    dialog.setCancelable(cancelable); //在dialogFragment中调用内置窗体的cancelable是没有效果的，必须调用dialogFragment本身的
                    dialog.setCanceledOnTouchOutside(outsideCancelable);
                }
            });
            return dialog;
        }
    }

}
