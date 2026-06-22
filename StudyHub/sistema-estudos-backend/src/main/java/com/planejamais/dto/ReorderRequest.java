package com.planejamais.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReorderRequest {
    @NotEmpty
    private List<Long> ids;
}
