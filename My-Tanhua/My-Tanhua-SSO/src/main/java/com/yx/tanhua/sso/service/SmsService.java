package com.yx.tanhua.sso.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.yx.tanhua.sso.config.AliyunSmsConfig;
import com.yx.tanhua.sso.config.TxyunSmsConfig;
import com.yx.tanhua.common.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * 短信 Service
 *
 * @author Yaoxi
 * @date 2021/01/17 11:39:38
 */
@Service
@Slf4j
public class SmsService {
    
    /**
     * 阿里云短信配置信息
     */
    @Resource
    private AliyunSmsConfig aliyunSmsConfig;
    
    @Resource
    private TxyunSmsConfig txyunSmsConfig;
    
    
    /**
     * redis模板
     * <p>
     * {@link RedisAutoConfiguration} 会生成两个RedisTemplate类型的bean
     * <p>
     * &#64;Resource 会为该对象注入{@link RedisTemplate}类型 泛型为{@code Object}的bean
     * 因为该对象被指定了{@code @ConditionalOnMissingBean(name = "redisTemplate")}
     * <p>
     * &#64;Autowired 会为其注入{@link StringRedisTemplate}类型 即{@link RedisTemplate}类型 泛型为{@code String}的子类 的 bean
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 发送短信验证码
     * 实现：发送完成短信验证码后，需要将验证码保存到redis中
     *
     * @param phone
     *     手机号
     *
     * @return {@link ErrorResult} 正常返回 {@code null}
     *     <p>
     *     上次的未失效 {@code errCode=000001}
     *     <p>
     *     发送失败 {@code errCode=000000}
     */
    public ErrorResult sendCheckCode(String phone) {
        
        String redisKey = "CHECK_CODE_" + phone;
        
        // 先判断该手机号发送的验证码是否还未失效
        // if (Boolean.TRUE.equals(this.redisTemplate.hasKey(redisKey))) {
        //     String msg = "上一次发送的验证码还未失效！";
        //     return ErrorResult.builder().errCode("000001").errMessage(msg).build();
        // }
        // 发验证码
        String code = sendSms(phone);
        // 发送验证码失败
        if (StringUtils.isEmpty(code)) {
            String msg = "发送短信验证码失败！";
            return ErrorResult.builder().errCode("000000").errMessage(msg).build();
        }
        
        log.debug("RedisKey: " + redisKey + " CODE: " + code);
        
        //短信发送成功，将验证码保存到redis中，有效期为5分钟
        this.redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(10));
        
        return null;
    }
    
    /**
     * 发送短信验证码tx实现
     *
     * @param mobile
     *     手机号
     *
     * @return 验证码
     *     <p>
     *     发送失败返回 {@code null}
     */
    public String sendSmsWithTxyun(String mobile) {
        // 文档 https://cloud.tencent.com/document/product/382/
        try {
            Credential cred = new Credential(txyunSmsConfig.getSecretId(), txyunSmsConfig.getSecretKey());
            SmsClient client = new SmsClient(cred, txyunSmsConfig.getRegionId());
            SendSmsRequest req = new SendSmsRequest();
            
            req.setSmsSdkAppid(txyunSmsConfig.getAppId());
            req.setSign(txyunSmsConfig.getSign());
            req.setTemplateID(txyunSmsConfig.getTemplateId());
            
            // 设置手机号
            req.setPhoneNumberSet(new String[]{"+86" + mobile});
            
            // 获取一个6位的随机数 作为验证码
            String code = RandomUtils.nextInt(100000, 999999) + "";
            req.setTemplateParamSet(new String[]{code});
            
            SendSmsResponse res = client.SendSms(req);
            // 输出 JSON 格式的字符串回包
            log.debug("手机号:" + mobile + " 回执..." + SendSmsResponse.toJsonString(res));
            
            if ("Ok".equals(res.getSendStatusSet()[0].getCode())) {
                // 发送成功 返回验证码
                return code;
            }
            log.info("发送短信验证码失败~ res = " + SendSmsResponse.toJsonString(res));
        } catch (TencentCloudSDKException e) {
            log.error("发送短信验证码失败~ mobile = " + mobile, e);
        }
        return null;
    }
    
    /**
     * 发送短信验证码aliyun实现
     *
     * @param mobile
     *     手机号
     *
     * @return 验证码
     *     <p>
     *     发送失败返回 {@code null}
     */
    public String sendSmsWithAliyun(String mobile) {
        DefaultProfile profile = DefaultProfile.getProfile(this.aliyunSmsConfig.getRegionId(),
                                                           this.aliyunSmsConfig.getAccessKeyId(),
                                                           this.aliyunSmsConfig.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        
        // 获取一个6位的随机数 作为验证码
        String code = RandomUtils.nextInt(100000, 999999) + "";
        
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(this.aliyunSmsConfig.getDomain());
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", this.aliyunSmsConfig.getRegionId());
        //目标手机号
        request.putQueryParameter("PhoneNumbers", mobile);
        //签名名称
        request.putQueryParameter("SignName", this.aliyunSmsConfig.getSignName());
        //短信模板code
        request.putQueryParameter("TemplateCode", this.aliyunSmsConfig.getTemplateCode());
        //模板中变量替换
        request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            if (StringUtils.contains(data, "\"Message\":\"OK\"")) {
                // 发送成功 返回验证码
                return code;
            }
            log.info("发送短信验证码失败~ data = " + data);
        } catch (Exception e) {
            log.error("发送短信验证码失败~ mobile = " + mobile, e);
        }
        return null;
    }
    
    public String sendSms(String mobile){
        // 获取一个6位的随机数 作为验证码
        // String code = RandomUtils.nextInt(100000, 999999) + "";
        String code = "123456";
        // todo 使用短信验证码
        // String code = this.sendSmsWithAliyun(phone);
        // String code = this.sendSmsWithTxyun(phone);
        log.info("验证码已生成 ("+code+")");
        return code;
    }
}