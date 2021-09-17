package com.cjs.widgets.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.fragment.app.FragmentActivity;

import com.cjs.widgets.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 消息对话框
 *
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/9/16 17:09
 */
public class MsgDialog extends BaseDialogFragment implements View.OnClickListener {
    private String title = "提示", msg = "", btnTextCancel = "取消", btnTextSubmit = "知道了";
    private TextView tv_title, tv_msg, btn_submit, btn_cancel;
    private @MsgDialogStyle
    int dialogStyle = Style.ONE_BUTTON;
    private OnClickListener onClickListener;

    public MsgDialog() {
        Log.d("BDF", "构造");
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_msg_dialog;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        tv_title = findViewById(R.id.tv_title);
        tv_msg = findViewById(R.id.tv_msg);
        btn_submit = findViewById(R.id.btn_submit);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    @Override
    public void initEvents(Bundle savedInstanceState) {
        setTitle(title);
        setMsg(msg);
        setBtnTextSubmit(btnTextSubmit);
        setBtnTextCancel(btnTextCancel);
        setDialogStyle(dialogStyle);
        btn_submit.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (tv_title != null) {
            tv_title.setText(title);
        }
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
        if (tv_msg != null) {
            tv_msg.setText(msg);
        }
    }

    public String getBtnTextCancel() {
        return btnTextCancel;
    }

    public void setBtnTextCancel(String btnTextCancel) {
        this.btnTextCancel = btnTextCancel;
        if (btn_cancel != null) {
            btn_cancel.setText(btnTextCancel);
        }
    }

    public String getBtnTextSubmit() {
        return btnTextSubmit;
    }

    public void setBtnTextSubmit(String btnTextSubmit) {
        this.btnTextSubmit = btnTextSubmit;
        if (btn_submit != null) {
            btn_submit.setText(btnTextSubmit);
        }
    }

    public int getDialogStyle() {
        return dialogStyle;
    }

    public void setDialogStyle(int dialogStyle) {
        this.dialogStyle = dialogStyle;
        if (dialogStyle == Style.TWO_BUTTON) {
            if (btn_cancel != null) {
                btn_cancel.setVisibility(View.VISIBLE);
            }
        } else {
            if (btn_cancel != null) {
                btn_cancel.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btn_submit) {
            if (onClickListener != null) {
                onClickListener.onSubmit(v, this);
            } else {
                dismiss();
            }
        } else if (v == btn_cancel) {
            if (onClickListener != null) {
                onClickListener.onCancel(v, this);
            }
        }
    }

    /**
     * 显示单按钮窗体
     *
     * @param activity
     * @param title
     * @param msg
     * @param onClickListener
     */
    public void showMsg(FragmentActivity activity, String title, String msg, OnClickListener onClickListener) {
        new Builder()
                .setTitle(title)
                .setMsg(msg)
                .setOnClickListener(onClickListener)
                .build()
                .show(activity.getSupportFragmentManager());
    }

    /**
     * 显示单按钮窗体
     *
     * @param activity
     * @param msg
     * @param onClickListener
     */
    public void showMsg(FragmentActivity activity, String msg, OnClickListener onClickListener) {
        showMsg(activity, "提示", msg, onClickListener);
    }

    /**
     * 显示单按钮窗体
     *
     * @param activity
     * @param msg
     */
    public void showMsg(FragmentActivity activity, String msg) {
        showMsg(activity, "提示", msg, null);
    }

    /**
     * 显示双按钮窗体
     *
     * @param activity
     * @param title
     * @param msg
     * @param onClickListener
     */
    public void showCancel(FragmentActivity activity, String title, String msg, OnClickListener onClickListener) {
        new Builder()
                .setTitle(title)
                .setMsg(msg)
                .setDialogStyle(Style.TWO_BUTTON)
                .setOnClickListener(onClickListener).build().show(activity.getSupportFragmentManager());
    }

    /**
     * 显示双按钮窗体
     *
     * @param activity
     * @param msg
     * @param onClickListener
     */
    public void showCancel(FragmentActivity activity, String msg, OnClickListener onClickListener) {
        showCancel(activity, "提示", msg, onClickListener);
    }

    /**
     * 窗体样式
     */
    public static class Style {
        /**
         * 单按钮 确认键
         */
        public static final int ONE_BUTTON = 0;
        /**
         * 双按钮 取消+确认
         */
        public static final int TWO_BUTTON = 1;
    }

    /**
     * MsgDialog按钮点击事件
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/9/16 15:07
     */
    public interface OnClickListener {

        /**
         * 确认
         *
         * @param v
         * @param dialog
         */
        void onSubmit(View v, MsgDialog dialog);

        /**
         * 取消
         *
         * @param v
         * @param dialog
         */
        void onCancel(View v, MsgDialog dialog);
    }

    /**
     * 事件监听器 简易版 不用强制实现cancel
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/9/16 17:08
     */
    public abstract class OnClickListenerSimple implements OnClickListener {
        @Override
        public void onCancel(View v, MsgDialog dialog) {

        }
    }

    /**
     * 窗体构造器
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/9/17 15:26
     */
    public static class Builder extends BaseBuilder<MsgDialog,Builder>{
        String title = "提示", msg, btnTextSubmit = "知道了", btnTextCancel = "取消";
        @MsgDialogStyle
        int dialogStyle;
        OnClickListener onClickListener;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder setBtnTextSubmit(String btnTextSubmit) {
            this.btnTextSubmit = btnTextSubmit;
            return this;
        }

        public Builder setBtnTextCancel(String btnTextCancel) {
            this.btnTextCancel = btnTextCancel;
            return this;
        }

        public Builder setOnClickListener(OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }

        /**
         * 设置窗体样式
         *
         * @param dialogStyle {@link Style#ONE_BUTTON,Style#TWO_BUTTON}
         * @return
         */
        public Builder setDialogStyle(@MsgDialogStyle int dialogStyle) {
            this.dialogStyle = dialogStyle;
            return this;
        }

        @Override
        public MsgDialog build() {
            dialog.setTitle(title);
            dialog.setMsg(msg);
            dialog.setBtnTextSubmit(btnTextSubmit);
            dialog.setBtnTextCancel(btnTextCancel);
            dialog.setOnClickListener(onClickListener);
            dialog.setDialogStyle(dialogStyle);
            return super.build();
        }
    }


    /**
     * 窗体样式属性限定类型注解
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Style.ONE_BUTTON, Style.TWO_BUTTON})
    public @interface MsgDialogStyle {
    }

    @Override
    protected int getWindowWidth() {
        return (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
    }
}
