package com.programmerspalace.snippets;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * Represents a discord role, roleID is the id for the discord role
 * Any role represents someone that can create, edit and delete snippets
 * 
 * @author Sam
 *
 */

@Entity("roles")
public class Role {
	@Id
	private String roleId;

	public Role() {
		super();
	}
	
	public Role(String roleId) {
		super();
		this.roleId = roleId;
	}

	public String getRoleId() {
		return roleId;
	}

}
