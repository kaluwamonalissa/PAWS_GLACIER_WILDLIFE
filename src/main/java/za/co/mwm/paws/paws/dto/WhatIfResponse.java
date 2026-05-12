package za.co.mwm.paws.paws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatIfResponse {
    private String ward;
    private int additionalRangers;
    private double currentAvgResponseTimeHrs;
    private double projectedAvgResponseTimeHrs;
    private double improvementPct;
    private String recommendation;
}

