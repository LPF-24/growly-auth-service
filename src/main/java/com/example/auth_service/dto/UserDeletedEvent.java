package com.example.auth_service.dto;

public class UserDeletedEvent {
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
