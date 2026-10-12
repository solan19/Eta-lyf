package io.github.mangi.hook;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 强制 Eta 流体云卡片渲染的 Xposed 模块。
 * 策略：
 * 1. hook SeedlingTool.isSupportSeedlingCard / isSupportFluidCloud → 返回 true
 * 2. hook SeedlingTool.isServicesEnabled → 返回 true
 * 3. hook 流体云卡片更新方法，确保即使服务端校验失败也本地渲染
 */
public class FluidCloudHook implements IXposedHookLoadPackage {

    private static final String TAG = "FluidCloudHook";
    private static final String ETA_PKG = "io.github.mangi.eta";
    private static final String SEEDLING_TOOL = "com.oplus.pantanal.seedling.util.SeedlingTool";

    private Context appContext;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!ETA_PKG.equals(lpparam.packageName)) return;
        log("Loaded for " + lpparam.packageName);

        // 1. hook isSupportSeedlingCard → true
        hookBool(lpparam.classLoader, SEEDLING_TOOL, "isSupportSeedlingCard", Context.class);
        hookBool(lpparam.classLoader, SEEDLING_TOOL, "isSupportFluidCloud", Context.class);
        hookBool(lpparam.classLoader, SEEDLING_TOOL, "isServicesEnabled", Context.class);

        // 2. hook Application.onCreate
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                appContext = (Application) param.thisObject;
                log("Got context: " + appContext.getPackageName());
                new Handler(Looper.getMainLooper()).postDelayed(this::forceInit, 5000);
            }

            private void forceInit() {
                try {
                    Class<?> n = XposedHelpers.findClass(
                        "io.github.mangi.eta.fluidcloud.FluidCloudNotifier",
                        appContext.getClassLoader());
                    XposedHelpers.callStaticMethod(n, "init", appContext);
                    log("FluidCloudNotifier.init called");
                    Object sup = XposedHelpers.getStaticObjectField(n, "isSupported");
                    log("isSupported = " + sup);
                    if (Boolean.TRUE.equals(sup)) {
                        log("SUCCESS: Seedling SDK is supported! Cards should display now.");
                    } else {
                        log("isSupported still false, trying direct...");
                        boolean ok = (boolean) XposedHelpers.callStaticMethod(
                            XposedHelpers.findClass(SEEDLING_TOOL, appContext.getClassLoader()),
                            "isSupportSeedlingCard", appContext);
                        log("Direct check isSupportSeedlingCard = " + ok);
                    }
                } catch (Throwable t) {
                    log("forceInit failed: " + t);
                }
            }
        });
        log("All hooks installed");
    }

    private void hookBool(ClassLoader cl, String cls, String method, Class<?>... params) {
        try {
            XposedHelpers.findAndHookMethod(cls, cl, method, params, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.setResult(true);
                }
            });
            log("Hooked " + cls + "." + method);
        } catch (Throwable t) {
            log("Hook " + cls + "." + method + " failed: " + t.getMessage());
        }
    }

    private void log(String msg) {
        XposedBridge.log("[" + TAG + "] " + msg);
        android.util.Log.i(TAG, msg);
    }
}
