package altay.boots.altayboots.model.entity;

import altay.boots.altayboots.service.PhotosOwner;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.*;

@Entity
@Data
@Table(name = "products")
@Where(clause = "active = true")
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
    @ElementCollection
    @CollectionTable(
            name = "product_sizes",
            joinColumns = @JoinColumn(name = "products_id")
    )
    @Column(name = "size_value")
    private Set<String> sizes = new TreeSet<>();
    @Column(name = "active", nullable = false)
    private boolean active = true;
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
