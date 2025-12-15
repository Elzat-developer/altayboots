package altay.boots.altayboots.model.entity;

import altay.boots.altayboots.service.PhotosOwner;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "promotions")
public class Promotion implements PhotosOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    @Column(name = "percentage_discounted")
    private int percentageDiscounted; // % скидки
    private boolean global;
    private boolean active;
    @Column(name = "start_date")
    private String startDate;
    @Column(name = "end_date")
    private String endDate;
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "photo_order_index")
    private List<ProductPhoto> photos = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "catalogs_id",referencedColumnName = "id")
    private Catalog catalog;
    @ManyToOne
    @JoinColumn(name = "products_id",referencedColumnName = "id")
    private Product product;
}

