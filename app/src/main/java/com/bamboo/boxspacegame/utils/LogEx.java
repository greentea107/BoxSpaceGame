package com.bamboo.boxspacegame.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Log工具，类似android.util.Log。
 * 该工具提取自XUtils工具集，强化了输出功能，可以输出长字符串
 * tag自动产生，格式: customTagPrefix:className.methodName(L:lineNumber),
 * customTagPrefix为空时只输出：className.methodName(L:lineNumber)。
 * Author: wyouflf
 * 修改日期：2017-08-17
 */
public class LogEx {
    public static boolean isDebug = true;
    public static String customTagPrefix = "x_log";
    private static final int stringBuffer = 2000;

    private LogEx() {
    }

    private static String generateTag() {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    public static void d(String content) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("d", tag, content, null);
    }

    public static void d(String content, Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("d", tag, content, tr);
    }

    public static void e(String content) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("e", tag, content, null);
    }

    public static void e(String content, Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("e", tag, content, tr);
    }

    public static void i(String content) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("i", tag, content, null);
    }

    public static void i(String content, Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("i", tag, content, tr);
    }

    public static void v(String content) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("v", tag, content, null);
    }

    public static void v(String content, Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("v", tag, content, tr);
    }

    public static void w(String content) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("w", tag, content, null);
    }

    public static void w(String content, Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("w", tag, content, tr);
    }

    public static void w(Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("w", tag, null, tr);
    }


    public static void wtf(String content) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("wtf", tag, content, null);
    }

    public static void wtf(String content, Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("wtf", tag, content, tr);
    }

    public static void wtf(Throwable tr) {
        if (!isDebug) return;
        String tag = generateTag();
        printLog("wtf", tag, null, tr);
    }

    private static void printLog(String type, String tag, String content, Throwable tr) {
        long length = content.length();
        if (length < stringBuffer || length == stringBuffer) {
            switchLog(type, tag, content, tr);
        } else {
            while (content.length() > stringBuffer) {
                String logContent = content.substring(0, stringBuffer);
                content = content.replace(logContent, "");
                switchLog(type, tag, logContent, tr);
            }
            switchLog(type, tag, content, tr);
        }
    }

    private static void switchLog(String type, String tag, String content, Throwable tr) {
        switch (type.trim().toLowerCase()) {
            case "i":
                Log.i(tag, content, tr);
                break;
            case "v":
                Log.v(tag, content, tr);
                break;
            case "d":
                Log.d(tag, content, tr);
                break;
            case "e":
                Log.e(tag, content, tr);
                break;
            case "w":
                Log.w(tag, content, tr);
                break;
            case "wtf":
                Log.wtf(tag, content, tr);
            default:
                Log.i(tag, content, tr);
        }
    }
}