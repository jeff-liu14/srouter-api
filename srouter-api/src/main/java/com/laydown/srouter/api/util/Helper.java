package com.laydown.srouter.api.util;

import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Helper {
    public interface CallBack {
        void onActivityResult(@Nullable ActivityResult result);
    }

    public static ActivityResultLauncher<Intent> startActivityForResult(@NonNull ComponentActivity activity, @Nullable CallBack callBack) {
        return activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (callBack != null) {
                callBack.onActivityResult(result);
            }
        });
    }
}
