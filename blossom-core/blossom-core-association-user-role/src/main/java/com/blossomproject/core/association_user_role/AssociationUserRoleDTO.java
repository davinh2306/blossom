package com.blossomproject.core.association_user_role;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.blossomproject.core.common.dto.AbstractAssociationDTO;
import com.blossomproject.core.role.RoleDTO;
import com.blossomproject.core.user.UserDTO;

public class AssociationUserRoleDTO extends AbstractAssociationDTO<UserDTO, RoleDTO> {

  @Override
  @JsonProperty("user")
  public UserDTO getA() {
    return super.getA();
  }

  @Override
  @JsonProperty("role")
  public RoleDTO getB() {
    return super.getB();
  }
}
