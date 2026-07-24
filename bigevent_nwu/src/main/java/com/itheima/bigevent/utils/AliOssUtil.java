package com.itheima.bigevent.utils;
import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class AliOssUtil
{    
    
     // 填写Bucket所在地域。
    private static final String region = "cn-beijing";
     // 填写Bucket名称，例如examplebucket。
    private static final String bucketName = "nwuwfy202421145";
    // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
    private static final String endpoint = "https://oss-cn-beijing.aliyuncs.com";

    public static String uploadFile(String objectName,InputStream in) throws Exception
    {
        
       // 从环境变量/系统属性读取 OSS 凭证
        String ak = OSSConfig.getAccessKeyId();
        String sk = OSSConfig.getAccessKeySecret();
        System.out.println("OSS AK=" + (ak != null ? ak.substring(0, Math.min(6, ak.length())) + "..." : "NULL"));
        System.out.println("OSS SK=" + (sk != null ? "***" : "NULL"));
        com.aliyun.oss.common.auth.DefaultCredentialProvider credentialsProvider =
            new com.aliyun.oss.common.auth.DefaultCredentialProvider(ak, sk);
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        //String objectName = "001.png";
        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);        
        OSS ossClient = OSSClientBuilder.create()
        .endpoint(endpoint)
        .credentialsProvider(credentialsProvider)
        .clientConfiguration(clientBuilderConfiguration)
        .region(region)               
        .build();
        String url = "";
        try {
            // 填写字符串。
            String content = "Hello OSS，你好世界";

            // 创建PutObjectRequest对象，设置公开读权限
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, in);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
            putObjectRequest.setMetadata(metadata);
           
            // 上传字符串。
            PutObjectResult result = ossClient.putObject(putObjectRequest); 
            url = "http://" + bucketName + "." + endpoint.substring(endpoint.lastIndexOf("/")+1) + "/" + objectName;      
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return url;
    }

    /** 删除 OSS 对象；对象不存在时 OSS 的删除操作仍可安全执行。 */
    public static void deleteFile(String objectName) throws Exception {
        if (objectName == null || objectName.isBlank()) return;
        String ak = OSSConfig.getAccessKeyId();
        String sk = OSSConfig.getAccessKeySecret();
        DefaultCredentialProvider credentialsProvider = new DefaultCredentialProvider(ak, sk);
        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
        configuration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
            .endpoint(endpoint)
            .credentialsProvider(credentialsProvider)
            .clientConfiguration(configuration)
            .region(region)
            .build();
        try {
            ossClient.deleteObject(bucketName, objectName);
        } finally {
            ossClient.shutdown();
        }
    }
}
