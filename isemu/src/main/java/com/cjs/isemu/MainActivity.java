package com.cjs.isemu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.cjs.thirdpart.CommandUtil;
import com.cjs.thirdpart.EmulatorCheckUtil;
import com.lahm.library.EmulatorCheckCallback;

import java.io.DataOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.tv_result);
//        tv.setText("code:" + getVersionCode(this) + " 名字:" + getVersionName(this));
        /*EmulatorCheckUtil.getSingleInstance().readSysProperty(this, new EmulatorCheckCallback() {
            @Override
            public void findEmulator(String emulatorInfo) {
                tv.setText(emulatorInfo);
            }
        });*/
        tv.setText(isSUExist()?"当前设备已经root\n":"没有root\n"+" ro.secure是"+roSecure()+"\ncheckGetRootAuth:"+checkGetRootAuth());
    }

    private boolean isSUExist() {
        File file = null;
        String[] paths = {
                "/sbin/su",
                "system/bin/su",
                "system/xbin/su",
                "/data/local/xbin/su",
                "system/sd/xbin/su",
                "data/local/su"
        };
        for (String path : paths) {
            file = new File(path);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    private String roSecure(){
        String roSecureObj= CommandUtil.getSingleInstance().getProperty("ro.secure");
        return roSecureObj;
    }

    private static synchronized boolean checkGetRootAuth() {
        Process process = null;
        DataOutputStream os = null;
        try {
            Log.i("HookDetection", "to exec su");
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            Log.i("HookDetection", "exitValue=" + exitValue);
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.i("HookDetection", "Unexpected error - Here is what I know: "
                    + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * 获取当前apk的版本号
     *
     * @param mContext
     * @return
     */
    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取当前apk的版本名
     *
     * @param context 上下文
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionName
            versionName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}