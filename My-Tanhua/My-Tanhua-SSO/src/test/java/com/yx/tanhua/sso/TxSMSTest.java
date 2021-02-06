package com.yx.tanhua.sso;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import org.junit.Test;

@SuppressWarnings({"FieldCanBeLocal", "CommentedOutCode"})
public class TxSMSTest {
    /*腾讯云账户密钥对 secretId 和 secretKey*/
    private String secretId = "AKIDaihT0IdAF6k9IBoxRfntmOktozGP2zVl";
    private String secretKey = "T12mG4lHYfWgbFdqpY2IchW90d2DO6ao";
    /* 短信应用 ID: 在 [短信控制台] 添加应用后生成的实际 SDKAppID，例如1400006666 */
    String appid = "1400471795";
    /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名，可登录 [短信控制台] 查看签名信息 */
    String sign = "灵韵梦声";
    /* 模板 ID: 必须填写已审核通过的模板 ID，可登录 [短信控制台] 查看模板 ID */
    String templateID = "837429";
    /* 下发手机号码，采用 e.164 标准，+[国家或地区码][手机号]
     * 例如+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号*/
    // 13203966553 13783283476
    String[] phoneNumbers = {"+8613203966553"};
    /* 模板参数: 若无模板参数，则设置为空*/
    String[] templateParams = {"0522"};
    
    @Test
    public void testSend() {
        try {
            /* 必要步骤：
             * 实例化一个认证对象，入参需要传入腾讯云账户密钥对
             */
            Credential cred = new Credential(secretId, secretKey);
            
            /* 实例化 SMS 的 client 对象
             * 第二个参数是地域信息，可以直接填写字符串 ap-guangzhou，或者引用预设的常量 */
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            
            /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
             * 您可以直接查询 SDK 源码确定接口有哪些属性可以设置
             * 属性可能是基本类型，也可能引用了另一个数据结构
             * 推荐使用 IDE 进行开发，可以方便地跳转查阅各个接口和数据结构的文档说明 */
            SendSmsRequest req = new SendSmsRequest();
            /* 填充请求参数，这里 request 对象的成员变量即对应接口的入参
             * 您可以通过官网接口文档或跳转到 request 对象的定义处查看请求参数的定义 */
            req.setSmsSdkAppid(appid);
            req.setSign(sign);
            
            /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
            // String session = "xxx";
            // req.setSessionContext(session);
            
            req.setTemplateID(templateID);
            req.setPhoneNumberSet(phoneNumbers);
            req.setTemplateParamSet(templateParams);
            
            /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
             * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
            SendSmsResponse res = client.SendSms(req);
            // 输出 JSON 格式的字符串回包
            System.out.println(SendSmsResponse.toJsonString(res));
            // 可以取出单个值，您可以通过官网接口文档或跳转到 response 对象的定义处查看返回字段的定义
            System.out.println(res.getRequestId());
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
    }
}
