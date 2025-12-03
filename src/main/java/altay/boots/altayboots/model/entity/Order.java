package altay.boots.altayboots.model.entity;

import altay.boots.altayboots.dto.status.PaidStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    @Column(name = "order_start_date")
    private LocalDateTime orderStartDate;
    @Column(name = "paid_status")
    @Enumerated(EnumType.STRING)
    private PaidStatus paidStatus;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "users_id",referencedColumnName = "id")  // FK на таблицу users
    private User user;
    @ManyToMany
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "orders_id"),
            inverseJoinColumns = @JoinColumn(name = "products_id")
    )
    private List<Product> products = new ArrayList<>();
}
