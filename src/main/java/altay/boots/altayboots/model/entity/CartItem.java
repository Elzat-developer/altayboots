package altay.boots.altayboots.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Integer quantity;
    @ManyToOne
    @JoinColumn(name = "carts_id",referencedColumnName = "id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "products_id",referencedColumnName = "id")
    private Product product;
}
