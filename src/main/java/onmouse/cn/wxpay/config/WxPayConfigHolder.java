package onmouse.cn.wxpay.config;

/**
 * 微信支付配置策略
 */
public class WxPayConfigHolder {
    private static final ThreadLocal<String> THREAD_LOCAL = ThreadLocal.withInitial(() -> "default");

    /**
     * 获取当前微信支付策略
     */
    public static String get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 设置当前微信支付策略
     *
     * @param label 策略名称
     */
    public static void set(String label) {
        THREAD_LOCAL.set(label);
    }

    /**
     * 此方法需要用户根据情况自己手动调用，内部无法判断何时删除比较合适
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
