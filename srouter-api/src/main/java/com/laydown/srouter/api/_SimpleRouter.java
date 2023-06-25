package com.laydown.srouter.api;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSONObject;
import com.laydown.srouter.api.interceptor.DegradeService;
import com.laydown.srouter.api.interceptor.InterceptorCallBack;
import com.laydown.srouter.api.model.RouterMeta;
import com.laydown.srouter.api.model.TargetMeta;
import com.laydown.srouter.api.provider.IProvider;
import com.laydown.srouter.api.util.AesHelper;
import com.laydown.srouter.api.util.FileUtil;
import com.laydown.srouter.api.util.JsonTool;
import com.laydown.srouter.api.util.SToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import static com.laydown.srouter.api.util.Const.END_FIX;

public class _SimpleRouter {
    private static Context mContext;
    private static Handler mHandler;

    private volatile static HashMap<String, RouterMeta> routerMetaMap;

    private volatile static _SimpleRouter router;

    private volatile static String SIMPLE_ROUTER_KEY = "";

    private volatile static InterceptorCallBack interceptorCallBack;

    private volatile static DegradeService degradeService;

    protected static _SimpleRouter getInstance() {
        if (router == null) {
            synchronized (_SimpleRouter.class) {
                if (router == null) {
                    router = new _SimpleRouter();
                }
            }
        }
        return router;
    }

    protected static synchronized boolean init(Application application) {
        mContext = application;
        routerMetaMap = new HashMap<>();
        mHandler = new Handler(Looper.getMainLooper());
        return true;
    }

    protected static synchronized void setSimpleRouterKey(String simpleRouterKey) {
        SIMPLE_ROUTER_KEY = simpleRouterKey;
    }

    protected static synchronized void setInterceptorCallBack(InterceptorCallBack callBack) {
        interceptorCallBack = callBack;
    }

    protected static synchronized void setDegradeService(DegradeService service) {
        degradeService = service;
    }

    protected static synchronized void scanDestinationFromAsset(Boolean isOpenAes) {
        String[] nameList = FileUtil.getAllAssetsList(mContext, "");
        ArrayList<String> stringArrayList = FileUtil.filterArray(nameList, END_FIX);
        if (stringArrayList.size() > 0) {
            routerMetaMap.clear();
            for (String item : stringArrayList) {
                String decryptContent = "";
                String jsonString = FileUtil.getAssetFile(mContext.getAssets(), item);
                if (!TextUtils.isEmpty(SIMPLE_ROUTER_KEY) && isOpenAes) {
                    decryptContent = AesHelper.decrypt(jsonString, SIMPLE_ROUTER_KEY);
                } else {
                    decryptContent = jsonString;
                }
                loadModuleFile(decryptContent);
            }
        } else {
            throw new RuntimeException("路由加载异常，请检查asset文件夹中是否存在*.json.simple文件");
        }
    }

    protected static synchronized void loadModuleFile(String jsonString) {
        HashMap<String, JSONObject> json2Map = JsonTool.json2Map(jsonString);
        Iterator<HashMap.Entry<String, JSONObject>> iterator = json2Map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, JSONObject> entry = iterator.next();
            String key = entry.getKey();
            JSONObject value = entry.getValue();
            System.out.println(key);
            System.out.println(value.toString());
            if (!routerMetaMap.containsKey(key)) {
                RouterMeta routerMeta = JsonTool.json2Bean(value.toJSONString(), RouterMeta.class);
                routerMetaMap.put(key, routerMeta);
            } else {
                RouterMeta oldRouterMeta = routerMetaMap.get(key);
                RouterMeta routerMeta = JsonTool.json2Bean(value.toJSONString(), RouterMeta.class);
                throw new RuntimeException("\npageUrl -> \"" + key + "\" has exist; \n old route class is -> "
                        + oldRouterMeta.getClazzName()
                        + "\n new route class is -> "
                        + routerMeta.getClazzName());
            }
        }
    }

    protected static synchronized HashMap<String, RouterMeta> getRouterMetaMap() {
        return routerMetaMap;
    }

    protected Object navigate(TargetMeta targetMeta) {
        Object obj = processOn(mContext, "degrade", targetMeta, -1);
        return obj;
//        return interceptorCall(mContext, targetMeta, -1);
    }

    protected Object navigate(Context context, TargetMeta targetMeta) {
        return processOn(context, "degrade", targetMeta, -1);
//        return interceptorCall(context, targetMeta, -1);
    }

    protected void navigateForResult(TargetMeta targetMeta, int requestCode) {
        processOn(mContext, "degrade", targetMeta, requestCode);
//        interceptorCall(mContext, targetMeta, requestCode);
    }

    protected void navigateForResult(Context context, TargetMeta targetMeta, int requestCode) {
        processOn(context, "degrade", targetMeta, requestCode);
//        interceptorCall(context, targetMeta, requestCode);
    }

    protected void navigateForResultX(ComponentActivity activity, TargetMeta targetMeta, ActivityResultLauncher<Intent> resultLauncher) {
//        interceptorCallX(activity, targetMeta, resultLauncher);
        processOnX(activity, "degrade", targetMeta, resultLauncher);
    }

    private void interceptorCallX(ComponentActivity activity, TargetMeta targetMeta, ActivityResultLauncher<Intent> resultLauncher) {
        runInMainThread(() -> {
            if (targetMeta.isGreenChannel()) {
                _realNavigateX(activity, targetMeta, resultLauncher);
            } else {
                if (interceptorCallBack != null) {
                    if (interceptorCallBack.onContinue(activity, targetMeta)) {
                        _realNavigateX(activity, targetMeta, resultLauncher);
                    }
                } else {
                    _realNavigateX(activity, targetMeta, resultLauncher);
                }
            }
        });
    }

    private void _realNavigateX(ComponentActivity activity, TargetMeta targetMeta, ActivityResultLauncher<Intent> resultLauncher) {
        Intent intent = new Intent(activity, targetMeta.getaClass());
        intent.putExtras(targetMeta.getmBundle());
        resultLauncher.launch(intent);
    }

    private String processDegrade(TargetMeta targetMeta) {
        String clazzName = targetMeta.getClazzName();
        try {
            targetMeta.setaClass(Class.forName(clazzName));
        } catch (ClassNotFoundException exception) {
            if (degradeService != null) {
                runInMainThread(() -> {
                    degradeService.onLost(mContext, targetMeta);
                });
            } else {
                runInMainThread(() -> {
                    SToastUtil.showToast(mContext, "ops path: " + targetMeta.getPath() + " clazz: " + clazzName + " lost");
                });
            }
            return "";
        }
        return "interceptor";
    }

    private String processOnX(ComponentActivity activity, String type, TargetMeta targetMeta, ActivityResultLauncher<Intent> resultLauncher) {
        if (targetMeta.isGreenChannel()) {
            try {
                targetMeta.setaClass(Class.forName(targetMeta.getClazzName()));
            } catch (ClassNotFoundException exception) {
                throw new RuntimeException("Find fragment error, " + exception.getLocalizedMessage());
            }
            interceptorCallX(activity, targetMeta, resultLauncher);
        } else {
            switch (type) {
                case "degrade":
                    String tp = processDegrade(targetMeta);
                    processOnX(activity, tp, targetMeta, resultLauncher);
                    break;
                case "interceptor":
                    interceptorCallX(activity, targetMeta, resultLauncher);
                    break;
                default:
                    break;
            }
        }
        return "";
    }

    private Object processOn(Context context, String type, TargetMeta targetMeta, int requestCode) {
        if (targetMeta.isGreenChannel()) {
            try {
                targetMeta.setaClass(Class.forName(targetMeta.getClazzName()));
            } catch (ClassNotFoundException exception) {
                throw new RuntimeException("Find fragment error, " + exception.getLocalizedMessage());
            }
            return interceptorCall(context, targetMeta, requestCode);
        } else {
            switch (type) {
                case "degrade":
                    String tp = processDegrade(targetMeta);
                    processOn(context, tp, targetMeta, requestCode);
                    break;
                case "interceptor":
                    Object obj = interceptorCall(context, targetMeta, requestCode);
                    return obj;
                default:
                    break;
            }
        }
        return "";
    }

    private Object interceptorCall(Context context, TargetMeta targetMeta, int requestCode) {
        if (targetMeta.isGreenChannel()) {
            return _realNavigate(context, targetMeta, requestCode);
        } else {
            if (interceptorCallBack != null) {
                runInMainThread(() -> {
                    if (interceptorCallBack.onContinue(context, targetMeta)) {
                        _realNavigate(context, targetMeta, requestCode);
                    }
                });
                return null;
            } else {
                return _realNavigate(context, targetMeta, requestCode);
            }
        }
    }

    private void startActivity(Context context, TargetMeta targetMeta, int requestCode) {
        Intent intent = new Intent(context, targetMeta.getaClass());
        if (context instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtras(targetMeta.getmBundle());
        if (requestCode > 0) {
            if (context instanceof Activity) {
                ActivityCompat.startActivityForResult((Activity) context, intent, requestCode, targetMeta.getmBundle());
            } else {
                throw new RuntimeException("Must use [navigation(activity, ...)] to support [startActivityForResult]");
            }
        } else {
            ActivityCompat.startActivity(context, intent, targetMeta.getmBundle());
        }
    }

    private Object _realNavigate(Context context, TargetMeta targetMeta, int requestCode) {
        if (!targetMeta.getmType().isEmpty()) {
            switch (targetMeta.getmType()) {
                case "activity":
                    if (isMainThread()) {
                        startActivity(context, targetMeta, requestCode);
                    } else {
                        runInMainThread(() -> {
                            startActivity(context, targetMeta, requestCode);
                        });
                    }
                    break;
                case "fragment":
                    Class<?> fragmentMeta = targetMeta.getaClass();
                    try {
                        Object instance = fragmentMeta.getConstructor().newInstance();
                        if (instance instanceof Fragment) {
                            ((Fragment) instance).setArguments(targetMeta.getmBundle());
                        } else if (instance instanceof androidx.fragment.app.Fragment) {
                            ((androidx.fragment.app.Fragment) instance).setArguments(targetMeta.getmBundle());
                        } else {
                            // noting
                            String nothing = "";
                        }
                        return instance;
                    } catch (Exception e) {
                        throw new RuntimeException("Fetch fragment instance error, " + e.getLocalizedMessage());
                    }
                case "provider":
                    Class<? extends IProvider> providerClazz = (Class<? extends IProvider>) targetMeta.getaClass();
                    IProvider provider;
                    try {
                        provider = providerClazz.getConstructor().newInstance();
                        provider.init(context);
                        return provider;
                    } catch (Exception e) {
                        throw new RuntimeException("Fetch provider instance error, " + e.getLocalizedMessage());
                    }
                default:
                    break;
            }
            return null;
        }
        return null;
    }

    public TargetMeta build(String pageUrl) {
        HashMap<String, RouterMeta> routerMetaHashMap = _SimpleRouter.getRouterMetaMap();
        if (routerMetaHashMap.containsKey(pageUrl)) {
            return parseRouterMeta(Objects.requireNonNull(routerMetaHashMap.get(pageUrl)));
        }
        TargetMeta targetMeta = new TargetMeta();
        targetMeta.setPath(pageUrl);
        targetMeta.setClazzName("");
        return targetMeta;
    }

    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    private TargetMeta parseRouterMeta(RouterMeta routerMeta) {
        TargetMeta targetMeta = new TargetMeta();
        targetMeta.setId(routerMeta.getId());
        targetMeta.setPath(routerMeta.getPath());
        targetMeta.setmType(routerMeta.getDestType());
        if (routerMeta.getDestType().equals("fragment") || routerMeta.getDestType().equals("provider")) {
            targetMeta.greenChannel();
        }
        targetMeta.setClazzName(routerMeta.getClazzName());
//        try {
//            targetMeta.setaClass(Class.forName(routerMeta.getClazzName()));
//        } catch (ClassNotFoundException exception) {
//            if (degradeService != null) {
//                degradeService.onLost(mContext, targetMeta);
//            }
//        }
        return targetMeta;
    }

    private boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

}
