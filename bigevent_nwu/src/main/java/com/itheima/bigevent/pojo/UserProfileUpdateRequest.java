package com.itheima.bigevent.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Only editable profile fields. The account id is always obtained from the JWT. */
@Data
public class UserProfileUpdateRequest {
    @NotEmpty
    @Pattern(regexp = "^\\S{1,10}$")
    private String nickname;

    @NotEmpty
    @Email
    private String email;
}
