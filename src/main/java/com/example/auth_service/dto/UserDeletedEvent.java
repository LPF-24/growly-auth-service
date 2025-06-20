package com.example.auth_service.dto;

import com.example.auth_service.util.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserDeletedEvent {
    @Schema(description = SwaggerConstants.ID_DESC, example = SwaggerConstants.ID_EXAMPLE)
    private Long personId;

    public UserDeletedEvent() {
    }

    public UserDeletedEvent(Long personId) {
        this.personId = personId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }
}
