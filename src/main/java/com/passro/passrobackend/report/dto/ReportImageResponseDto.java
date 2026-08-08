package com.passro.passrobackend.report.dto;

import com.passro.passrobackend.report.entity.ReportImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportImageResponseDto {

    private String imageKey;
    private Integer displayOrder;

    public static ReportImageResponseDto from(ReportImage reportImage) {
        return new ReportImageResponseDto(
                reportImage.getImageKey(),
                reportImage.getDisplayOrder()
        );
    }
}
