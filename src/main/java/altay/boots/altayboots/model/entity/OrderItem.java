package altay.boots.altayboots.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int quantity; // количество товара

    @ManyToOne
    @JoinColumn(name = "products_id",referencedColumnName = "id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "orders_id",referencedColumnName = "id")
    private Order order;
}
