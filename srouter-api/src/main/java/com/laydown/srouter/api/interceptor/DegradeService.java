package com.laydown.srouter.api.interceptor;

import android.content.Context;

import com.laydown.srouter.api.model.TargetMeta;

/**
 * 全局降级服务
 */
public interface DegradeService {
    void onLost(Context context, TargetMeta targetMeta);
}
