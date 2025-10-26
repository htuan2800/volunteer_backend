package com.volunteerBackend.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailResetPayload {
    private String token;
    private String email;
    private String Fullname;
}
