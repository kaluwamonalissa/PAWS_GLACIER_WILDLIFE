package za.co.mwm.paws.paws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatIfRequest {
    private String ward;
    private int additionalRangers;
}

