package jpabasic.pinnolbe.global.logging.aop;

public class LogHolder {
    private static final ThreadLocal<LogContext> contextHolder = new ThreadLocal<>();
    public static void set(LogContext context) { contextHolder.set(context); }
    public static LogContext get() { return contextHolder.get(); }
    public static void clear() { contextHolder.remove(); }
}
