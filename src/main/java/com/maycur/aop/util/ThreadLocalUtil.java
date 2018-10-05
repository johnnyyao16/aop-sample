package com.maycur.aop.util;

public class ThreadLocalUtil {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void set(long val) {
        threadLocal.set(val);
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static long get() {
        return threadLocal.get();
    }
}
