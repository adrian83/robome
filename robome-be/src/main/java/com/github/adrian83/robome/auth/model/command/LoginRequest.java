package com.github.adrian83.robome.auth.model.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
	private String email;
	private String password;
}
