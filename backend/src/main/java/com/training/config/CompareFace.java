package com.training.config;

import com.aliyun.facebody20191230.models.CompareFaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;

@Component
public class CompareFace {

    @Autowired
    private AliyunConfig aliyunConfig;

    public com.aliyun.facebody20191230.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(aliyunConfig.getAccessKeyId())
                .setAccessKeySecret(aliyunConfig.getAccessKeySecret());
        config.endpoint = "facebody.cn-shanghai.aliyuncs.com";
        return new com.aliyun.facebody20191230.Client(config);
    }

    public boolean compare(String urlA, String urlB) throws Exception {
        com.aliyun.facebody20191230.Client client = createClient();

        URL url1 = new URL(urlA);
        URL url2 = new URL(urlB);
        InputStream inputStreamA = url1.openConnection().getInputStream();
        InputStream inputStreamB = url2.openConnection().getInputStream();

        com.aliyun.facebody20191230.models.CompareFaceAdvanceRequest request =
                new com.aliyun.facebody20191230.models.CompareFaceAdvanceRequest()
                        .setImageURLAObject(inputStreamA)
                        .setImageURLBObject(inputStreamB);

        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();

        CompareFaceResponse response = client.compareFaceAdvance(request, runtime);
        double confidence = response.getBody().getData().getConfidence();

        System.out.println("相似度：" + confidence);
        return confidence >= 61.0;
    }
}