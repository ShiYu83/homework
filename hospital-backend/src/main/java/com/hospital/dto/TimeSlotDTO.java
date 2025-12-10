package com.hospital.dto;

import lombok.Data;

@Data
public class TimeSlotDTO {
    private Long scheduleId;
    
    private String timeSlot;
    
    private Integer remaining;
    
    private Integer totalCount;
}

