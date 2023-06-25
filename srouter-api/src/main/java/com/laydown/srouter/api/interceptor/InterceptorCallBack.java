package com.laydown.srouter.api.interceptor;

import android.content.Context;

import com.laydown.srouter.api.model.TargetMeta;

public interface InterceptorCallBack {
    boolean onContinue(Context context, TargetMeta targetMeta);
}
