# wxpay_java
微信支付插件包
<style lang="scss">
  .container{
    max-width:72rem !important
  }
</style>
<script setup>
  import PictureComp from '../../components/PictureComp.vue'
  import Location from '../../components/Location.vue'
  const homeUrl = `${window.location.origin}/systemStandard/index`
</script>
<Location :homeUrl="homeUrl"/>

# 微信支付插件使用说明

## 开发插件前准备工作

::: warning

1. 需要先登录微信商家平台

https://pay.weixin.qq.com/index.php/core/home/login?return_url=%2Findex.php%2Fcore%2Fhome%2Fdefault_header

2. 登录之后

:::tip

1. 产品中心——开发配置——开通native支付功能

2. 点击产品配置——设置微信支付回调地址

:::


## 浅讲一下实现方法

### 架构的生成

__实体类__

:::tip

bean包：实体类包

notify包：通知回调类实体映射类

order包： 订单生成结果类

request包： 请求参数映射类

result包：  结果参数映射类

:::

__配置类__

:::warning

config包：配置类相关包

WxPayConfig：微信支付配置

WxPayConfigHolder：微信支付配置策略

WxPayHttpProxy：微信支付正向代理


:::

__常量类__

:::tip

WxPayConstants：微信支付常量类，基本是一些参数的固定取值枚举，微信官方限制参数值或者是其他返回固定值

WxPayErrorCode：微信支付错误代码，目前只是记录而已，没有地方用到

:::

__转换类__

:::tip

WxPayOrderNotifyResultConverter：通过继承AbstractReflectionConverter类来实现重写父类java对象序列化和反序列化，主要是操作WxPayOrderNotifyResult中Coupon代金券信息

:::

__异常类__

:::danger

WxPayException：微信支付异常类，自己封装的异常类，使用构造者模式，避免了新建多个构造函数的诟病

:::

__接口实现类__

:::tip

WxPayService：基础接口类，定义了一些必须要实现的方法，其中包括：请求微信官方接口、查询订单、创建订单、退款等必要接口

BaseWxPayServiceImpl：基础抽象实现类，子类可以进一步的详细实现

		子类实现：

		WxPayServiceImpl：微信支付实现类，无任何方法

		WxPayServiceApacheHttpImpl：网络实现类，主要包括：请求微信接口的能力、签名认证证书的能力


:::

__工具包__

```java

实体工具包类
BeanUtils: void checkRequiredFields(Object bean)，检查标记的@Required注解的值是否为空，为空提示含有必填字段无值

资源工具类包
ResourcesUtils：InputStream getResourceAsStream(final String resourceName, final ClassLoader callingClass)，主要用来获取classPath里面的文件流

签名工具类包
SignUtils：String createSign(Map<String, String> params, String signType, String signKey, String[] ignoredParams)，主要用来根据键值对里面的数据，用签名类型，签名秘钥，是否要忽略指定参与签名键值对，最后判断签名加密方式是MD5还是HMAC_SHA256，返回签名加密值

xml配置类
XmlConfig: static boolean fastMode = false，作为全局配置，是否要启动快速模式生成xml，默认使用XStream类库，进行映射

ZIP包类
ZipUtils：static File unGZip(final File file)，主要使用FileOutputStream、GZIPInputStream、IOUtils三个类库包，将GZIP解压，并写出来

```

### 基本实现思路

```java

1. 微信请求接口的实现，除了要封装HttpClients和HttpPost之外，还要使用XStream的toXML()和fromXML()方法。
	 小思路：微信v2支付版本，要求是请求和返回参数均是xml格式，所以要用XStream类库，用来序列化和反序列化
	     PS：1. 一些小注意的地方，退款接口需要认证证书，所以最后发送请求时，要读取本地证书，安全才可以访问
	 		 2. 微信接口返回的数据有签名认证，用户商户判断数据返回是否是安全的，所以，最后获取数据后，要使用MD5或者HMAC_SHA256进行签名算法比对后，正确才可以返回，否则，视为违法数据
2. 提高复用程度，配置类使用共用方法，放在了`BaseWxPayServiceImpl`实现类中，方便其他方法使用

3. 所以，为了提高可复用性和扩展性，请求和返回结果实体类，我新建了两个基础类
	 一来是为了共同属性的封装，在其他实体类中少些许多重复属性
	 二来是为了复用方法，检查和生成签名时方便，这样在请求前校验签名，请求后，根据签名生成算法校验数据合法性

```

### 实体类、算法、异常类的设计

```java

实体类：采用复用的思想，使用继承可以实现，并将基础实体类定义为抽象父类，子类除了实现必要方法外，还可以选择性的实现protected abstract 方法

算法：

签名算法——使用HMAC_SHA256或者MD5加密指定字符串，用于与服务器返回的签名进行比对

字段非空检测——使用定义的注解@Required进行判断，首先获取要判断的类的字段，进行注解判断非空

异常类：

主要自己封装要自定义返回的信息，以及使用Builder方式构造

```

## 插件两种使用方法

::: tip

1. JAR 包直接引入方式

  > 版本发布中提供jar包或者自己下载项目手动打包

2. 服务中获取 > _http://xx.xx.xx/api_，api 服务获取
  :::

### JAR 包直接引入方式


::: tip JAR包两种获取方式

1. > 请在git链接中-版本中自行提取JAR包版本

```

操作简单，不另做说明

```

2. > 自己打成JAR包
```bash

1. git fork下来之后

	 下载maven包后

2. mvn package打成jar包

3. 然后需要将jar打到自己的maven仓库中，cmd中请使用以下命令

	 > 1. mvn install:install-file -Dfile=项目下的src/main/resources/lib/java-pay-1.0.2.jar的绝对路径 -DgroupId=oms.pay -DartifactId=java-pay -Dversion=1.0.0 -Dpackaging=jar

	 > 2. 将下列dependency保存到项目中pom.xml的dependencies

	 		<dependency>
  				<groupId>oms.pay</groupId>
    			<artifactId>java-pay</artifactId>
    			<version>1.0.2</version>
  			</dependency>

	 > 3. 最后，右键选择-maven-reload project，重新下载包即可
```
> 使用方式——如何在自己的项目中使用

```java

1. 将以下配置加到自己项目spring中的配置文件中

wx:
   pay:
	appId: 
    	mchId: 
    	mchKey: 
    	subAppId:
    	subMchId:
    	keyPath:   //p12的证书文件（绝密文件，涉及到退款接口等）自己到商家微信支付平台中下载

2. 创建资源映射类以及微信支付配置类

下面配置方式，大多数的项目可能基本类似

微信支付资源配置映射类
@Data
@ConditionalOnClass(WxPayService.class)
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayProperties {
	private String appId;
	private String mchId;
	private String mchKey;
	private String subAppId;
	private String subMchId;
	private String keyPath;
}

微信支付配置类
@Configuration
@EnableConfigurationProperties(WxPayProperties.class)
@AllArgsConstructor
public class WxPayConfiguration {
	private WxPayProperties wxPayproperties;

	@Bean
	@ConditionalOnMissingBean
	public WxPayService wxPayService() {
		WxPayConfig wxPayConfig = new WxPayConfig();
		
		wxPayConfig.setAppId(StringUtils.trimToNull(this.wxPayproperties.getAppId()));
		wxPayConfig.setMchId(StringUtils.trimToNull(this.wxPayproperties.getMchId()));
		wxPayConfig.setMchKey(StringUtils.trimToNull(this.wxPayproperties.getMchKey()));
		wxPayConfig.setSubAppId(StringUtils.trimToNull(this.wxPayproperties.getSubAppId()));
		wxPayConfig.setSubMchId(StringUtils.trimToNull(this.wxPayproperties.getSubMchId()));
		wxPayConfig.setKeyPath(StringUtils.trimToNull(this.wxPayproperties.getKeyPath()));

		wxPayConfig.setUseSandboxEnv(false);

		WxPayService wxPayService = new WxPayServiceImpl();
		wxPayService.setConfig(wxPayConfig);
		return wxPayService;
	}
}

3. 在Controller层中直接使用接口能力即可

示例：
private WxPayService wxPayService;

/**
 * 查询订单能力
 * 传递微信订单号或者商家订单号
 * 
 */
@GetMapping("queryOrder")
public Result queryOrder(@RequestParam(required = false) String transactionId, @RequestParam(required = false) String outTradeNo) throw WxPayException {
	return Result.success(this.wxPayService.queryOrder(transactionId, outTradeNo));
}

```

:::


### 微信服务平台中获取

```java
__使用API方式获取__

方法1： 自己动手使用网路包进行手动发送
示例代码：

// 发送请求数据
public String post(String url, String xmlString, boolean useKey) {
	try {
		HttpClientBuilder hb = this.createHttpClientBuilder(useKey);
		HttpPost hp = this.createHttpPost(url, xmlString);
		try (CloseableHttpClient chc = hb.build()) {
			try (CloseableHttpResponse chr = chc.execute(hp)) {
				String responseString = EntityUtils.toString(chr.getEntity(), StandardCharsets.UTF_8);
				log.info("\n【请求地址】：{}\n【请求数据】：{}\n【响应数据】：{}", url, xmlString, responseString);
				return responseString;
			}
		} finally () {
			hp.releaseConnection();
		}
	} catch (Exception e) {
		log.error("\n【请求地址】:{}\n【请求数据】:{}\n【异常数据】:{}", url, xmlString, responseString);
		throw new WxPayException(e.getMessage(), e);
	}
}

方法2： 自己手动实现controller层的实现

微信服务平台接口前缀：
   > http://xx.xx.xx/api
```

## API接口以及参数说明

### 查询订单

```json

请求地址：http://xx.xx.xx/api/wx/pay/queryOrder
请求方法：get
参数注解：@RequestParam
请求参数：二者选其一

{
	// 微信订单号
	"transactionId"： "String",
	// 商家订单号
	"outTradeNo": "String"
}

请求地址：http://xx.xx.xx/api/wx/pay/queryOrder
请求方法：post
参数注解：@RequestBody
请求参数：二者选其一

{
	// 微信订单号
	"transactionId"： "String",
	// 商家订单号
	"outTradeNo": "String"
}

```

### 生成IP

```json

IP地址获取，用于获取客户端IP
请求地址：http://xx.xx.xx/api/wx/pay/getDeviceIp
请求方法：get
获取到的IP可以用于赋值下述的"spbillCreateIp"字段

```

### 创建订单

```json

请求地址：http://xx.xx.xx/api/wx/pay/createOrder
请求方法：post
参数注解：@RequestBody
请求参数：
{
	// 商品简单描述
	"body": "String，网页title名-商品概述",

	// 订单总金额，单位为分，详见
	"totalFee": "Integer，整数，注意将元转换成分，后台代码提供此方法",

	// trade_type=NATIVE时，此参数必传。此参数为二维码中包含的商品ID，商户自行定义
	"productId": "String，1",

	// 支持IPV4和IPV6两种格式的IP地址。用户的客户端IP
	"spbillCreateIp": "String，192.168.8.72",

	// body 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。 公网域名必须为https，如果是走专线接入，使用专线NAT IP或者私有回调域名可使用http
	"notifyUrl": "String，http://xx.xx.xx/api/wx/pay/notify/scanpay",

	// 商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一
	"outTradeNo": "String，推荐使用当前时间戳+特定不会重复的名称",

	// 交易类型
	"tradeType": "String，NATIVE"
}
```

### 客户扫码支付回调

```json

不是主动调用，前提是将链接发送给微信服务器，然后，微信服务器接收到支付请求后，将结果回传给该API

请求地址：http://xx.xx.xx/api/wx/pay/notify/scanpay
请求方式：post
参数注解：@RequestBody
参数：字符串形式xmlData

```

### 退款申请

```json

请求地址：http://xx.xx.xx/api/wx/pay/refund
请求方式：post
参数注解：@RequestBody
请求参数：
{
	// 商户内部订单号
	"outTradeNo": "String，创建订单所得",
	// 商户内部定义退款号
	"outRefundNo": "String，建议跟创建订单相同方式",
	// 异步接收微信支付退款结果通知的回调地址，通知URL必须为外网可访问的url，不允许带参数。公网域名必须为https，如果是走专线接入，使用专线NAT IP或者私有回调域名可使用http。如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效。
	"notifyUrl": "String，http://xx.xx.xx/api/wx/pay/notify/refund",
	// 订单总金额
	"totalFee": "Integer，订单总金额",
	// 退款金额
	"refundFee": "Integer，最大为订单金额"
}

```

### 客户退款回调

```json

不是主动调用，前提是将链接发送给微信服务器，然后，微信服务器接收到退款请求后，将结果回传给该API

请求地址：http://xx.xx.xx/api/wx/pay/notify/refund
请求方式：post
参数注解：@RequestBody
参数：字符串形式xmlData

```

### 退款查询

```json

请求地址：http://xx.xx.xx/api/wx/pay/refundQuery
请求方法：get
参数注解：@RequestParam
请求参数：四者选其一

{
	// 微信订单号
	"transactionId"： "String",
	// 商家订单号
	"outTradeNo": "String",
	// 退款订单号
	"outRefundNo": "String",
	// 退款id号
	"refundId": "String"
}

请求地址：http://xx.xx.xx/api/wx/pay/queryOrder
请求方法：post
参数注解：@RequestBody
请求参数：四者选其一

{
	// 微信订单号
	"transactionId"： "String",
	// 商家订单号
	"outTradeNo": "String",
	// 退款订单号
	"outRefundNo": "String",
	// 退款id号
	"refundId": "String"
}

```

### 下载对账单

```json

商户可以通过该接口下载历史交易清单。比如掉单、系统错误等导致商户侧和微信侧数据不一致，通过对账单核对后可校正支付状态。

注意：

1、微信侧未成功下单的交易不会出现在对账单中。支付成功后撤销的交易会出现在对账单中，跟原支付单订单号一致；

2、微信在次日9点启动生成前一天的对账单，建议商户10点后再获取；

3、对账单中涉及金额的字段单位为“元”。

4、对账单接口只能下载三个月以内的账单。

5、对账单是以商户号维度来生成的，如一个商户号与多个appid有绑定关系，则使用其中任何一个appid都可以请求下载对账单。对账单中的appid取自交易时候提交的appid，与请求下载对账单时使用的appid无关。

6、自2018年起入驻的商户默认是开通免充值券后的结算对账单，且汇总数据为总交易单数，应结订单总金额，退款总金额，充值券退款总金额，手续费总金额，订单总金额，申请退款总金额

请求地址：http://xx.xx.xx/api/wx/pay/downloadBill
请求方法：get
参数注解：@RequestParam
请求参数：

{
	// 对账单日期，为必填项，注意当天不出账单
	"billDate"： "String，20140603",
	// 账单类型
	"billType": "String，ALL || SUCCESS || REFUND || RECHARGE_REFUND",
	// 压缩账单
	"tarType": "String，GZIP"
}

请求地址：http://xx.xx.xx/api/wx/pay/downloadBill
请求方法：post
参数注解：@RequestBody
请求参数：

{
	// 对账单日期，为必填项
	"billDate"： "String，20140603",
	// 账单类型
	"billType": "String，ALL || SUCCESS || REFUND || RECHARGE_REFUND",
	// 压缩账单
	"tarType": "String，GZIP"
}

```

### 下载资金对账

```json

请求地址：http://xx.xx.xx/api/wx/pay/downloadFundFlow
请求方法：get
参数注解：@RequestParam
请求参数：

{
	// 对账单日期，为必填项
	"billDate"： "String，20140603",
	// 账单的资金来源账户
	"accountType": "String，Basic || Operation || Fees",
	// 压缩账单
	"tarType": "String，GZIP"
}

请求地址：http://xx.xx.xx/api/wx/pay/downloadFundFlow
请求方法：post
参数注解：@RequestBody
请求参数：

{
	// 对账单日期，为必填项
	"billDate"： "String，20140603",
	// 账单的资金来源账户
	"accountType": "String，Basic || Operation || Fees",
	// 压缩账单
	"tarType": "String，GZIP"
}

```

### 关闭订单

```json

请求地址：http://xx.xx.xx/api/wx/pay/closeOrder
请求方法：get
参数注解：@RequestParam
请求参数：

{
	// 商户订单号，为必填项
	"out_trade_no": ""
}

请求地址：http://xx.xx.xx/api/wx/pay/closeOrder
请求方法：post
参数注解：@RequestBody
请求参数：

{
	// 商户订单号，为必填项
	"out_trade_no": ""
}

```

### 交易保障

```json

请求地址：http://xx.xx.xx/api/wx/pay/report
请求方法：post
参数注解：@RequestBody
请求参数：
{
	// 报对应的接口的完整URL，类似：
	// https://api.mch.weixin.qq.com/pay/unifiedorder
	// 对于刷卡支付，为更好的和商户共同分析一次业务行为的整体耗时情况，对于两种接入模式，请都在门店侧对一次刷卡支付进行一次单独的整体上报，上报URL指定为：
	// https://api.mch.weixin.qq.com/pay/micropay/total
	// 关于两种接入模式具体可参考本文档章节：刷卡支付商户接入模式
	// 其它接口调用仍然按照调用一次，上报一次来进行
	"interfaceUrl": "https://api.mch.weixin.qq.com/pay/unifiedorder",
	// 接口耗时情况，单位为毫秒
	// 注意：该参数最后带有下划线“_”，参数设计如此，非文档问题
	"executeTime": 1000,
	// SUCCESS/FAIL
	// 此字段是通信标识，非交易标识，交易是否成功需要查看trade_state来判断
	"returnCode": "SUCCESS",
	// 当return_code为FAIL时返回信息为错误原因 ，例如
	// 签名失败
	// 参数格式校验错误
	"returnMsg": "OK",
	// SUCCESS/FAIL
	"resultCode": "SUCCESS",
	// 发起接口调用时的机器IP 
	"userIp": "可以上面的生成IP的功能"
}

```
