package com.planejamais.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HeatmapSyncRequest {
    private Map<String, Integer> dias;
}
