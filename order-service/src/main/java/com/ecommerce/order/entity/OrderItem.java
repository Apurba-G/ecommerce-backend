package com.ecommerce.order.entity;

import com.ecommerce.order.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    @Column(name = "product_image", columnDefinition = "TEXT")
    private String productImage;

    @Column(name = "product_sku", length = 100)
    private String productSku;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "selling_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "product_snapshot", columnDefinition = "jsonb")
    private String productSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 30)
    @Builder.Default
    private OrderItemStatus itemStatus = OrderItemStatus.ACTIVE;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (sellingPrice != null && quantity != null) {
            this.totalPrice = sellingPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
