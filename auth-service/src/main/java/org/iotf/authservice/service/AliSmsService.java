package org.iotf.authservice.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.aliyun.teautil.Common.toJSONString;

@Service
public class AliSmsService {
    @Value("${aliyunService.accessKeyId}")
    private String accessKey;
    @Value("${aliyunService.accessKeySecret}")
    private String accessKeySecret;

    private static final String SignName = "山西华正创新技术研究院";
    private static final String TemplateCode = "SMS_243201003";

    public Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKey)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }

    public void sendCodeForAliYun(String phoneNumber, String code) {
        try {
            Client client = this.createClient();
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(SignName)
                    .setTemplateCode(TemplateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");
            SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
            System.out.println(toJSONString(sendSmsResponse));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}