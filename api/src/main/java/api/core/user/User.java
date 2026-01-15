package api.core.user;

public class User {
    private int id;
    private String email;
    private String password;
    private String role;
    private String serviceAddress;

    public User() {}

    public User(int id, String email, String password, String role, String serviceAddress) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.serviceAddress = serviceAddress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getServiceAddress() { return serviceAddress; }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }
}