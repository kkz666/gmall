package com.kkz.gmall.product.controller;

import com.kkz.gmall.common.result.Result;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/product")
public class FilUploadController {
    @Value("${minio.endpointUrl}")
    private String endpointUrl;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secreKey}")
    private String secreKey;
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * admin/product/fileUpload
     * 文件上传
     * @return
     */
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file){
        String url="";
        try {
            // 创建MinIoClint客户端对象
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpointUrl)
                            .credentials(accessKey, secreKey)
                            .build();
            // 判断指定的桶是否存在
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // 如果不存在指定的桶，创建.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("Bucket 'gmall' already exists.");
            }
            // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
            // 'asiatrip'.
//            minioClient.uploadObject(
//                    UploadObjectArgs.builder()
//                            .bucket("asiatrip")
//                            .object("asiaphotos-2015.zip")
//                            .filename("/home/user/Photos/asiaphotos.zip")
//                            .build());
//            System.out.println(
//                    "'/home/user/Photos/asiaphotos.zip' is successfully uploaded as "
//                            + "object 'asiaphotos-2015.zip' to bucket 'asiatrip'.");
            //不行，原因：无法明确知道文件的位置
            //文件名称：
            String fileName = System.currentTimeMillis() + UUID.randomUUID().toString();
            // Upload known sized input stream.
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                            file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            //拼接文件路径
            url = endpointUrl + "/" + bucketName + "/" + fileName;
            System.out.println("图片的路径："+url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  Result.ok(url);

    }
}
