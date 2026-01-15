package api.composite.club;

import java.util.List;

public class ClubAggregate {
    private final int clubId;
    private final String name;
    private final String location;
    private final String phoneNumber;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;

    public ClubAggregate(int clubId, String name, String location, String phoneNumber, 
                         List<ReviewSummary> reviews, ServiceAddresses serviceAddresses) {
        this.clubId = clubId;
        this.name = name;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.reviews = reviews;
        this.serviceAddresses = serviceAddresses;
    }

    // Getters...
    public int getClubId() { return clubId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public List<ReviewSummary> getReviews() { return reviews; }
    public ServiceAddresses getServiceAddresses() { return serviceAddresses; }
}
