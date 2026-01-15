package api.core.club;

public class Club {
    private int id;
    private String name;
    private String location;
    private String phoneNumber;
    private String serviceAddress; // For tracing

    public Club() {}

    public Club(int id, String name, String location, String phoneNumber, String serviceAddress) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.serviceAddress = serviceAddress;
    }

    // Getters and Setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getServiceAddress() { return serviceAddress; }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }
}