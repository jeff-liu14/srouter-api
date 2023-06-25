package com.laydown.srouter.api.interceptor;

import android.content.Context;

import com.laydown.srouter.api.model.TargetMeta;

public interface DegradeService {
    void onLost(Context context, TargetMeta targetMeta);
}
