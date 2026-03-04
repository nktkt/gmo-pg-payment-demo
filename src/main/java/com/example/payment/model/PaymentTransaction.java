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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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

    @Version
    private Long version;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(String orderId, Product product, int amount, TransactionStatus status) {
        this.orderId = orderId;
        this.product = product;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public Long getProductId() { return product != null ? product.getId() : null; }
    public Product getProduct() { return product; }
    public int getAmount() { return amount; }
    public String getAccessId() { return accessId; }
    public String getAccessPass() { return accessPass; }
    public TransactionStatus getStatus() { return status; }
    public String getTranId() { return tranId; }
    public String getApprove() { return approve; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setAccessId(String accessId) { this.accessId = accessId; }
    public void setAccessPass(String accessPass) { this.accessPass = accessPass; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setTranId(String tranId) { this.tranId = tranId; }
    public void setApprove(String approve) { this.approve = approve; }

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
