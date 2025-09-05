package com.training.config;

import com.aliyun.facebody20191230.models.DetectFaceAdvanceRequest;
import com.aliyun.facebody20191230.models.DetectFaceResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;

@Component
public class FaceDetector {



    @Autowired
    private CompareFace compareFace; // 复用 createClient()

    public boolean detect(String imageUrl) throws Exception {
        // 使用配置中的 AccessKeyId 和 Secret 创建客户端
        com.aliyun.facebody20191230.Client client = compareFace.createClient();

        // 读取图片
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openConnection().getInputStream();

        // 构造请求
        DetectFaceAdvanceRequest detectFaceRequest =
                new DetectFaceAdvanceRequest()
                        .setImageURLObject(inputStream);

        RuntimeOptions runtime = new RuntimeOptions();

        // 调用 API 进行人脸检测
        DetectFaceResponse detectFaceResponse =
                client.detectFaceAdvance(detectFaceRequest, runtime);

        // 获取检测结果
        int faceNum = detectFaceResponse.getBody().getData().getFaceCount();

        // 至少检测到一张人脸才算是人脸
        return faceNum > 0;
    }
}