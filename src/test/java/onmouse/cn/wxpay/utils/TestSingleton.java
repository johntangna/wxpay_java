package onmouse.cn.wxpay.utils;

public class TestSingleton {

    private static TestSingleton testSingleton = null;

    private TestSingleton(){}

    public static synchronized TestSingleton getInstance(){
        if (testSingleton == null){
            testSingleton = new TestSingleton();
        }

        return testSingleton;
    }
}
