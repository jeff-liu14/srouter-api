package com.laydown.srouter.api.interceptor;

import android.content.Context;

import com.laydown.srouter.api.model.TargetMeta;

/**
 * 全局拦截服务
 */
public interface InterceptorCallBack {
    /**
     * @param context
     * @param targetMeta
     * @return false-改次请求被拦截 true-继续进行此次请求
     */
    boolean onContinue(Context context, TargetMeta targetMeta);
}
