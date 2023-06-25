package com.laydown.srouter.api;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Keep;

import com.laydown.srouter.api.interceptor.DegradeService;
import com.laydown.srouter.api.interceptor.InterceptorCallBack;
import com.laydown.srouter.api.model.TargetMeta;

@Keep
public class SimpleRouter {
    private volatile static SimpleRouter router;
    private volatile static boolean isInit = false;

    public static SimpleRouter getInstance() {
        if (router == null) {
            synchronized (SimpleRouter.class) {
                if (router == null) {
                    router = new SimpleRouter();
                }
            }
        }
        return router;
    }

    public static synchronized void init(Application application) {
        if (!isInit) {
            isInit = _SimpleRouter.init(application);
        }
    }

    public static synchronized void setInterceptorCallBack(InterceptorCallBack callBack) {
        _SimpleRouter.setInterceptorCallBack(callBack);
    }

    public static synchronized void setDegradeService(DegradeService service) {
        _SimpleRouter.setDegradeService(service);
    }

    /**
     * 加载路由文件
     *
     * @param simpleRouterKey 为路由文件加密/解密提供key，长度16
     */
    public static synchronized void scanRoute(String simpleRouterKey, Boolean isOpenAes) {
        if (simpleRouterKey.length() < 16) {
            throw new RuntimeException("simpleRouterKey长度必须为16");
        }
        _SimpleRouter.setSimpleRouterKey(simpleRouterKey);
        initSimpleRouter(isOpenAes);
    }

    private static void initSimpleRouter(Boolean isOpenAes) {
        _SimpleRouter.scanDestinationFromAsset(isOpenAes);
    }

    public TargetMeta build(String pageUrl) {
        return _SimpleRouter.getInstance().build(pageUrl);
    }

    public Object navigate(TargetMeta targetMeta) {
        return _SimpleRouter.getInstance().navigate(targetMeta);
    }

    public Object navigate(Context context, TargetMeta targetMeta) {
        return _SimpleRouter.getInstance().navigate(context, targetMeta);
    }

    public void navigateForResult(TargetMeta targetMeta, int requestCode) {
        _SimpleRouter.getInstance().navigateForResult(targetMeta, requestCode);
    }

    public void navigateForResult(Context context, TargetMeta targetMeta, int requestCode) {
        _SimpleRouter.getInstance().navigateForResult(context, targetMeta, requestCode);
    }

    public void navigateForResultX(ComponentActivity activity, TargetMeta targetMeta, ActivityResultLauncher<Intent> resultLauncher) {
        _SimpleRouter.getInstance().navigateForResultX(activity, targetMeta, resultLauncher);
    }
}
