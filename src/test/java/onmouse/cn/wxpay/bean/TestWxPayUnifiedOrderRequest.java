package onmouse.cn.wxpay.bean;

import onmouse.cn.wxpay.bean.request.WxPayUnifiedOrderRequest;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestWxPayUnifiedOrderRequest {

    @Test
    public void testCreateIp() throws UnknownHostException {
        InetAddress inet4Address = Inet4Address.getLocalHost();
        String address = inet4Address.getHostAddress();
        System.out.println(address);
    }

    @Test
    public void testSubStr() {
        String fileName = "方法士大夫_12月_fdsf2015";
        int underscoreIndex = fileName.indexOf("_"); // 获取第一个下划线在文件名中的位置
        int monthIndex = fileName.indexOf("月"); // 获取月份在文件名中的位置
        int digitIndex = underscoreIndex + 1; // 获取数字在文件名中的位置
        int digit = Integer.parseInt(fileName.substring(digitIndex, monthIndex)); // 获取数字并转换为int类型
        System.out.println(digit);
    }
}
