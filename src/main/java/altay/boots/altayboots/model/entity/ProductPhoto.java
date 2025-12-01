package altay.boots.altayboots.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "product_photos")
public class ProductPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "photo_url")
    private String photoURL; // путь к фото
    @ManyToOne
    @JoinColumn(name = "products_id",referencedColumnName = "id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "promotions_id",referencedColumnName = "id")
    private Promotion promotion;
}
