package com.planejamais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DescricaoResponse {
    private Long id;
    private String texto;
}
