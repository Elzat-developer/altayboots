package altay.boots.altayboots.model.entity;

import altay.boots.altayboots.service.PhotosOwner;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "products")
public class Product implements PhotosOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private String text;
    private int price;
    @Column(name = "old_price")
    private int oldPrice;
    @ManyToOne
    @JoinColumn(name = "catalogs_id",referencedColumnName = "id")
    private Catalog catalog;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "photo_order_index")
    private List<ProductPhoto> photos = new ArrayList<>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Promotion> promotions;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
    @ManyToMany(mappedBy = "products")
    private List<Order> orders;
}
