package com.laydown.srouter.api.util;

import android.content.Context;
import android.widget.Toast;

public class SToastUtil {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
