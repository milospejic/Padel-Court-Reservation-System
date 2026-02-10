package api.event;

public class ClubDeletedEvent {
    private int clubId;

    public ClubDeletedEvent() {}

    public ClubDeletedEvent(int clubId) {
        this.clubId = clubId;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }
}