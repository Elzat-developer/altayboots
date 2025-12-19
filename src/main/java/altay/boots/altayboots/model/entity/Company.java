package altay.boots.altayboots.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String text;
    @Column(name = "photo_url")
    private String photoURL;
    private String base;
    private String city;
    private String street;
    private String email;
    private String phone;
    @Column(name = "job_start")
    private String jobStart;
    @Column(name = "job_end")
    private String jobEnd;
    @Column(name = "free_start")
    private String freeStart;
    @Column(name = "free_end")
    private String freeEnd;
}
