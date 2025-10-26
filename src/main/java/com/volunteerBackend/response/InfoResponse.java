package com.volunteerBackend.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoResponse<T> {
    private boolean success;
    private String message;
    private T dataInfo;
}
