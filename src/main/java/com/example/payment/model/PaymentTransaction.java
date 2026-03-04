package com.example.payment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 27)
    private String orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int amount;

    private String accessId;
    private String accessPass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    private String tranId;
    private String approve;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(String orderId, Long productId, int amount, TransactionStatus status) {
        this.orderId = orderId;
        this.productId = productId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public Long getProductId() { return productId; }
    public int getAmount() { return amount; }
    public String getAccessId() { return accessId; }
    public String getAccessPass() { return accessPass; }
    public TransactionStatus getStatus() { return status; }
    public String getTranId() { return tranId; }
    public String getApprove() { return approve; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Product getProduct() { return product; }

    public void setAccessId(String accessId) { this.accessId = accessId; }
    public void setAccessPass(String accessPass) { this.accessPass = accessPass; }
    public void setStatus(TransactionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    public void setTranId(String tranId) { this.tranId = tranId; }
    public void setApprove(String approve) { this.approve = approve; }
}
