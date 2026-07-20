package com.mall.product.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.mym.mall.common.api.CommonResult;
import com.mall.product.domain.dto.BucketPolicyConfigDto;
import com.mall.product.domain.dto.MinioUploadDto;
import io.minio.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MinIO 对象存储 Controller
 *
 * 【管理端专用】文件上传和删除操作，主要用于后台管理中商品图片、品牌Logo等文件的上传和删除。
 * 用户端通过 MinIO 的公开 URL 直接访问文件，不调用本接口。
 */
@Tag(name = "MinioController", description = "MinIO Object Storage Management")
@RestController
@RequestMapping("/minio")
public class MinioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioController.class);
    /** MinIO endpoint URL */
    @Value("${minio.endpoint}")
    private String ENDPOINT;
    /** MinIO bucket name */
    @Value("${minio.bucketName}")
    private String BUCKET_NAME;
    /** MinIO access key */
    @Value("${minio.accessKey}")
    private String ACCESS_KEY;
    /** MinIO secret key */
    @Value("${minio.secretKey}")
    private String SECRET_KEY;

    /** 【管理端】文件上传到 MinIO */
    @Operation(summary = "File upload")
    @PostMapping("/upload")
    public CommonResult<MinioUploadDto> upload(@RequestPart("file") MultipartFile file) {
        try {
            // Create MinIO client
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(ENDPOINT)
                    .credentials(ACCESS_KEY, SECRET_KEY)
                    .build();
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (isExist) {
                LOGGER.info("Bucket already exists");
            } else {
                // Create bucket with read-only policy
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
                BucketPolicyConfigDto bucketPolicyConfigDto = createBucketPolicyConfigDto(BUCKET_NAME);
                SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder()
                        .bucket(BUCKET_NAME)
                        .config(JSONUtil.toJsonStr(bucketPolicyConfigDto))
                        .build();
                minioClient.setBucketPolicy(setBucketPolicyArgs);
            }
            String filename = file.getOriginalFilename();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            // Generate object name with date prefix
            String objectName = sdf.format(new Date()) + "/" + filename;
            // Upload file using putObject
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), ObjectWriteArgs.MIN_MULTIPART_SIZE).build();
            minioClient.putObject(putObjectArgs);
            LOGGER.info("File upload successful");
            MinioUploadDto minioUploadDto = new MinioUploadDto();
            minioUploadDto.setName(filename);
            minioUploadDto.setUrl(ENDPOINT + "/" + BUCKET_NAME + "/" + objectName);
            return CommonResult.success(minioUploadDto);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("Upload failed: {}", e.getMessage());
        }
        return CommonResult.failed();
    }

    /**
     * Create bucket policy configuration for public read access
     */
    private BucketPolicyConfigDto createBucketPolicyConfigDto(String bucketName) {
        BucketPolicyConfigDto.Statement statement = BucketPolicyConfigDto.Statement.builder()
                .Effect("Allow")
                .Principal("*")
                .Action("s3:GetObject")
                .Resource("arn:aws:s3:::" + bucketName + "/*.**").build();
        return BucketPolicyConfigDto.builder()
                .Version("2012-10-17")
                .Statement(CollUtil.toList(statement))
                .build();
    }

    /** 【管理端】从 MinIO 删除文件 */
    @Operation(summary = "File delete")
    @PostMapping("/delete")
    public CommonResult delete(@RequestParam("objectName") String objectName) {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(ENDPOINT)
                    .credentials(ACCESS_KEY, SECRET_KEY)
                    .build();
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(objectName).build());
            return CommonResult.success(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CommonResult.failed();
    }
}
