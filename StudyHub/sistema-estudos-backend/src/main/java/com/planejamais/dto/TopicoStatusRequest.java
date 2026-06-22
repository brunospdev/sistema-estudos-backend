package com.planejamais.dto;

import com.planejamais.domain.StatusEstudo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicoStatusRequest {
    @NotNull
    private StatusEstudo status;
}
