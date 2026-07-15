package com.passro.passrobackend.file.service;

import com.passro.passrobackend.file.exception.FileException;
import com.passro.passrobackend.file.exception.code.FileErrorCode;
import com.passro.passrobackend.global.configuration.S3Properties;
import java.net.URL;
import java.time.Duration;

import com.passro.passrobackend.global.exception.APIException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service {

	private static final Duration DEFAULT_SIGNATURE_DURATION = Duration.ofMinutes(10);
	private static final Duration MAX_SIGNATURE_DURATION = Duration.ofDays(7);

	private final S3Presigner s3Presigner;
	private final S3Properties s3Properties;

	public S3Service(S3Presigner s3Presigner, S3Properties s3Properties) {
		this.s3Presigner = s3Presigner;
		this.s3Properties = s3Properties;
	}

	public URL getPresignedUploadUrl(String objectKey) {
		return getPresignedUploadUrl(objectKey, DEFAULT_SIGNATURE_DURATION, null);
	}

	public URL getPresignedUploadUrl(String objectKey, Duration signatureDuration, String contentType) {

        try {
            validate(objectKey, signatureDuration);

            PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey);

            if (StringUtils.hasText(contentType)) {
                putObjectRequestBuilder.contentType(contentType);
            }

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(signatureDuration)
                    .putObjectRequest(putObjectRequestBuilder.build())
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            return presignedRequest.url();
        } catch (Exception e) {
            throw new FileException(FileErrorCode.FILE_UPLOAD_FAILED);
        }
	}

	public URL getPresignedDownloadUrl(String objectKey) {
		return getPresignedDownloadUrl(objectKey, DEFAULT_SIGNATURE_DURATION);
	}

	public URL getPresignedDownloadUrl(String objectKey, Duration signatureDuration) {
		try {
            validate(objectKey, signatureDuration);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(signatureDuration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url();
        }catch (Exception e) {
            throw new FileException(FileErrorCode.FILE_NOT_FOUND);
        }
	}

	private void validate(String objectKey, Duration signatureDuration) {
		if (!StringUtils.hasText(s3Properties.getBucket())) {
			throw new IllegalStateException("aws.s3.bucket is required");
		}

		if (!StringUtils.hasText(objectKey)) {
			throw new IllegalArgumentException("objectKey is required");
		}

		if (signatureDuration == null || signatureDuration.isNegative() || signatureDuration.isZero()) {
			throw new IllegalArgumentException("signatureDuration must be positive");
		}

		if (signatureDuration.compareTo(MAX_SIGNATURE_DURATION) > 0) {
			throw new IllegalArgumentException("signatureDuration cannot exceed 7 days");
		}
	}
}


