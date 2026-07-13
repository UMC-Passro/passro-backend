package com.passro.passrobackend.file.controller;

import com.passro.passrobackend.file.service.S3Service;
import com.passro.passrobackend.file.exception.code.FileSuccessCode;
import com.passro.passrobackend.global.response.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

@RequestMapping("/file")
@AllArgsConstructor
@RestController
public class FileController {
    private final S3Service s3Service;

    @GetMapping("{fileName}/upload")
    public APIResponse<String> getUploadUrl(@PathVariable String fileName) {
        URL url = s3Service.getPresignedUploadUrl(fileName);
        return APIResponse.onSuccess(FileSuccessCode.OK, url.toString());
    }

    @GetMapping("{fileName}/download")
    public APIResponse<String> getDownloadUrl(@PathVariable String fileName) {
        URL url = s3Service.getPresignedDownloadUrl(fileName);
        return APIResponse.onSuccess(FileSuccessCode.OK, url.toString());
    }
}
