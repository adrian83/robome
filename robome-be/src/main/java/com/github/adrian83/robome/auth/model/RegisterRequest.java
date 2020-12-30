package com.github.adrian83.robome.auth.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
	private String email;
	private String password;
}
