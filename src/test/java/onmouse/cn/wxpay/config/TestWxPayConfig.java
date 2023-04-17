package onmouse.cn.wxpay.config;

public class TestWxPayConfig {
    public final WxPayConfig payConfig = new WxPayConfig();

//    @Test
//    public void testInitSSLContext_classpath() throws Exception {
//        payConfig.setMchId("123");
//        payConfig.setKeyPath("classpath:/dlt.p12");
//        payConfig.initSSLContext();
//    }
//
//    @Test
//    public void testInitSSLContext_http() throws Exception {
//        payConfig.setMchId("1613402222");
//        payConfig.setKeyPath("C://Users//OMS-IT//Downloads//WXCertUtil//cert//1613402222_20230403_cert//apiclient_cert.p12");
//        InputStream inputStream = payConfig.loadConfigInputStream("C://Users//OMS-IT//Downloads//WXCertUtil//cert//1613402222_20230403_cert//apiclient_cert.p12");
//        byte[] bytes = new byte[1024];
//        int len = -1;
//        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\OMS-IT\\Desktop\\test.txt");
//        while ((len = inputStream.read(bytes)) != -1) {
//            fileOutputStream.write(bytes, 0, len);
//            fileOutputStream.flush();
//        }
//    }
//
//    @Test
//    public void testSSLContent() throws Exception {
//        payConfig.setMchId("1613402222");
//        payConfig.setKeyPath("C://Users//OMS-IT//Downloads//WXCertUtil//cert//1613402222_20230403_cert//apiclient_cert.p12");
////        payConfig.setKeyPath("C:\\Users\\OMS-IT\\Desktop\\微信支付秘钥\\cfa9884441524ae106d34e4c5049341c.pem");
////        payConfig.setKeyPath("https://wxserver.onmouse.cn");
//        payConfig.initSSLContext();
//    }
//
//    @Test
//    public void testInitSSLContext() throws Exception {
//        this.testInitSSLContext_classpath();
//        this.testInitSSLContext_http();
//    }
//
//    @Test
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    public void testHashCode() {
//        payConfig.hashCode();
//    }
//
//    @Test
//    public void testInitSSLContext_base64() throws Exception {
//        payConfig.setMchId("123");
//        payConfig.setKeyString("MIIKmgIBAzCCCmQGCS...");
//        payConfig.initSSLContext();
//    }
}
