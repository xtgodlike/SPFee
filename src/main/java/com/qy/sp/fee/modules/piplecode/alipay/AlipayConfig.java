package com.qy.sp.fee.modules.piplecode.alipay;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayConfig {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "2088411889864894";
	// 商户的私钥
	public static String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKfCnzVvvESjzeuIZTIr87T8dkHDw8cUZcShwG6u5SF6CuoupFUhvrN3eARLOi5ANVN/Eh5iMCroe8/t5k+CIxPy5mqf+/VpCmN7K9XdIsH78FkWQBD5/0wNTEPVGzNGu7/qP7Z2+29+6T0Mytg4ntMDQ/i45fJTmcaQtRSf5RUzAgMBAAECgYBO+ruFptMV5aBekNpDopmbzXfHQmj7Ysj+p8A227QX7KMS2V6YjciAKXChZOFBQQ1Z/+sBzuwqZ0VTAQd6yyPDzwvboUEsCH38QTjU4Tn95Ikwvnc0Hz0V8/4EShOj8zH4cNoXY7Mu50VcJ0OHfCvXyczhvI6wSDkpyw/qFVNk0QJBANwy2PkY/RGH+IKjQfyqcfN9eeCpOhnSgZ6rh/qqG/WwdlXkicwvwNaKWWk++xUZViwt8xBcsgd1/k1EEwB4alUCQQDDCSxZ5il/M+uzAtOublSGkqfrh4SY+bhinSWoXF1WZ5q8PpBeCtXIgvhPZj0F475XPPTFH+MYqz3EBDAo0BlnAkB73B4v/95Z/iHLWgnEFhwC2pGzzXzhCYffuJzEYutXR+tIZUUdlb7i7u9cRBD3zUirkS0oBvwdNkHVNHV7zkABAkBMjcE22DzZjQurDCUChpPu7omyzVKiqQJS0tQBLaAFVk1KLlSEVURsm9vTbpHtSeNgYrDA3y9Ic46e7fXpTyVpAkB6XhJVn+JU9sCyDloaRuTfmx37w7hba2c+9rTe6IDOOSZYxEozny1bKSXmUy43dC9RSLBktklqtCQUAhm7FFdj";
	
	// 支付宝的公钥，无需修改该值
	public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";

	// 商户收款账号
	public static final String SELLER = "tiansongsong@newqy.net";
		
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "D:\\";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "utf-8";
	
	// 签名方式 不需修改
	public static String sign_type = "RSA";

}
