package isel.sisinf.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dock")
public class Dock {
    @Id
    @Column(name = "number")
    private int id;

    private int station;

    private String state;

    private Integer scooter;

    @Version
    private java.sql.Timestamp version;

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public void setScooter(Integer scooter) {
        this.scooter = scooter;
    }
}
