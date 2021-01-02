package com.github.adrian83.robome.domain.user.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class User {
  private UUID id;
  private String email;
  private String passwordHash;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private List<Role> roles;
}
