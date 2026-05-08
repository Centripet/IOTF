package org.iotf.authservice.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.iotf.entity.auth.dao.TFile;
import org.iotf.entity.auth.UploadPolicy;
import org.iotf.requestFormation.auth.uploadSubmitRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AliOssService {
    // 阿里云基本配置
    private static final String endpoint = "https://oss-cn-beijing.aliyuncs.com";
    @Value("${aliyunService.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyunService.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyunService.bucket}")
    private String bucket;
    private static final String dirPrefix = "uploads"; // 上传目录前缀
    private String host;
    @PostConstruct
    public void init() {
        this.host = "https://" + bucket + ".oss-cn-beijing.aliyuncs.com";
    }
    private static final long EXPIRE_TIME_SECONDS = 1800; // 0.5 hour
    public static final long MAX_FILE_SIZE_50MB = 50L * 1024 * 1024; // 50MB
    public static final long MAX_FILE_SIZE_500MB = 500L * 1024 * 1024; // 500MB
    public static final long EXPIRE_TIME_ONE_HOUR = 60 * 60;
    public static final long EXPIRE_TIME_HALF_HOUR = 30 * 60;
    public static final long EXPIRE_TIME_TEN_MIN = 10 * 60;
    public static final long EXPIRE_TIME_7_DAY = 7 * 24 * 60 * 60;


    private OSS createClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public UploadPolicy generateUploadPolicy(String key, long MAX_FILE_SIZE) {
        System.out.println(bucket);
        System.out.println(host);

        OSS ossClient = createClient();
        try {
            long expireEndTime = System.currentTimeMillis() + EXPIRE_TIME_SECONDS * 1000;
            Date expiration = new Date(expireEndTime);

            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, MAX_FILE_SIZE);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, key);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            String encodedPolicy = Base64.getEncoder().encodeToString(postPolicy.getBytes(StandardCharsets.UTF_8));
            String signature = ossClient.calculatePostSignature(postPolicy);

            return new UploadPolicy(
                    accessKeyId,
                    encodedPolicy,
                    signature,
                    key,
                    host,
                    expireEndTime / 1000
            );
        } finally {
            ossClient.shutdown();
        }
    }

    public String generateObjectKey(String path, Long uploader, String suffix) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String datePrefix = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        return String.format("%s/%s/%s/%s/%s%s", dirPrefix, path, uploader, datePrefix, uuid, suffix);
    }

    public String generateDownloadUrl(String objectKey, long expireSeconds) {
        OSS ossClient = createClient();
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey);
            request.setExpiration(expiration);
            URL signedUrl = ossClient.generatePresignedUrl(request);
            return signedUrl.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    public void setPublicRead(String objectKey) {
        OSS ossClient = createClient();
        try {
            ossClient.setObjectAcl(bucket, objectKey, CannedAccessControlList.PublicRead);
        } finally {
            ossClient.shutdown();
        }
    }

    public void uploadFile(String objectKey, File file, boolean publicRead) {
        OSS ossClient = createClient();
        try {
            PutObjectRequest request = new PutObjectRequest(bucket, objectKey, file);
            if (publicRead) {
                request.setProcess("true");
                ossClient.putObject(request);
                setPublicRead(objectKey);
            } else {
                ossClient.putObject(request);
            }
        } finally {
            ossClient.shutdown();
        }
    }

    public String generatePublicUrl(String key) {
        return host + "/" + key;
    }

    public boolean fileExists(String objectKey) {
        OSS ossClient = createClient();
        try {
            return ossClient.doesObjectExist(bucket, objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    public Map<uploadSubmitRequest, Boolean> fileExistsList(List<uploadSubmitRequest> requests) {
        OSS ossClient = createClient();
        Map<uploadSubmitRequest, Boolean> keyMap = new HashMap<>();
        try {
            for (uploadSubmitRequest request : requests) {
                keyMap.put(request, ossClient.doesObjectExist(bucket, request.key()));
            }
        } finally {
            ossClient.shutdown();
        }
        return keyMap;
    }

    public boolean fileExists(Map<uploadSubmitRequest, Boolean> keyMap) {
        for (Boolean flag : keyMap.values()) {
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    public File downloadFromOss(String objectKey) throws IOException {
        OSS ossClient = createClient();
        try {
            File tempFile = File.createTempFile("oss-", "-" + objectKey.substring(objectKey.lastIndexOf(".")));
            ossClient.getObject(new GetObjectRequest(bucket, objectKey), tempFile);
            return tempFile;
        } finally {
            ossClient.shutdown();
        }
    }

    public List<TFile> generateUrlForEntity(List<TFile> files) {

        for (TFile file : files) {
            String oss_url = null;
            if (file.getIs_public_read()) {
                oss_url = generatePublicUrl(file.getFile_key());
                System.out.println(oss_url);
            } else {
                oss_url = generateDownloadUrl(file.getFile_key(), AliOssService.EXPIRE_TIME_ONE_HOUR);
                System.out.println(oss_url);
            }

            file.setOss_url(oss_url);
//            file.setPreview_url(fileViewService.generateKkFilePreviewUrl(oss_url));
        }

        return files;
    }

    public TFile generateUrlForEntity(TFile file) {

        String oss_url = null;
        if (file.getIs_public_read()) {
            oss_url = generatePublicUrl(file.getFile_key());
            System.out.println(oss_url);
        } else {
            oss_url = generateDownloadUrl(file.getFile_key(), AliOssService.EXPIRE_TIME_ONE_HOUR);
            System.out.println(oss_url);
        }

        file.setOss_url(oss_url);
//            file.setPreview_url(fileViewService.generateKkFilePreviewUrl(oss_url));

        return file;
    }

}
