package user_service.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;



@Table("user_model")
public class UserModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private Integer id;
	
	@Column("email")
	private String email;
	
	@Column("password")
	private String password;
	
	@Column("role") 
	private String role;
	
	public UserModel() {
		
	}
	
	public UserModel(String email, String password, String role) {
		super();
		this.email = email;
		this.password = password;
		this.role = role;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}