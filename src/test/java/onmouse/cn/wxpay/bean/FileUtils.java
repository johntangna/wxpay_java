package onmouse.cn.wxpay.bean;

public class FileUtils {
    public static Integer getFileName(String fileName) {
        int underscoreIndex = fileName.indexOf("_"); // 获取第一个下划线在文件名中的位置
        int monthIndex = fileName.indexOf("月"); // 获取月份在文件名中的位置
        int numberIndex = underscoreIndex + 1; // 获取数字在文件名中的位置
        return Integer.parseInt(fileName.substring(numberIndex, monthIndex)); // 获取数字并转换为int类型
    }
    public static void main(String[] args) {
    }
}
