package com.example.payment.service;

import com.example.payment.dto.AlterTranResponse;
import com.example.payment.dto.EntryTranResponse;
import com.example.payment.dto.ExecTranResponse;
import com.example.payment.exception.GmoPgApiException;
import com.example.payment.model.PaymentTransaction;
import com.example.payment.model.Product;
import com.example.payment.model.TransactionStatus;
import com.example.payment.repository.PaymentTransactionRepository;
import com.example.payment.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final GmoPgApiClient gmoPgApiClient;
    private final ProductRepository productRepository;
    private final PaymentTransactionRepository transactionRepository;

    public PaymentService(GmoPgApiClient gmoPgApiClient,
                          ProductRepository productRepository,
                          PaymentTransactionRepository transactionRepository) {
        this.gmoPgApiClient = gmoPgApiClient;
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public PaymentTransaction authorize(Long productId, String token) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        String orderId = generateOrderId();
        int amount = product.getPrice();

        PaymentTransaction transaction = new PaymentTransaction(
                orderId, productId, amount, TransactionStatus.AUTHORIZED
        );

        try {
            // Step 1: EntryTran (AUTH)
            log.info("EntryTran: orderId={}, amount={}", orderId, amount);
            EntryTranResponse entryResponse = gmoPgApiClient.entryTran(orderId, "AUTH", amount);
            transaction.setAccessId(entryResponse.accessId());
            transaction.setAccessPass(entryResponse.accessPass());

            // Step 2: ExecTran (token)
            log.info("ExecTran: orderId={}", orderId);
            ExecTranResponse execResponse = gmoPgApiClient.execTran(
                    entryResponse.accessId(),
                    entryResponse.accessPass(),
                    orderId,
                    token
            );
            transaction.setTranId(execResponse.tranId());
            transaction.setApprove(execResponse.approve());
            transaction.setStatus(TransactionStatus.AUTHORIZED);

            log.info("Authorization successful: orderId={}, tranId={}, approve={}",
                    orderId, execResponse.tranId(), execResponse.approve());

        } catch (GmoPgApiException e) {
            log.error("Authorization failed: orderId={}, error={}", orderId, e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public PaymentTransaction capture(Long transactionId) {
        PaymentTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.AUTHORIZED) {
            throw new IllegalStateException("Cannot capture transaction with status: " + transaction.getStatus());
        }

        log.info("AlterTran(SALES): orderId={}", transaction.getOrderId());
        AlterTranResponse response = gmoPgApiClient.alterTran(
                transaction.getAccessId(),
                transaction.getAccessPass(),
                "SALES",
                transaction.getAmount()
        );

        transaction.setStatus(TransactionStatus.CAPTURED);
        transaction.setTranId(response.tranId());
        transaction.setApprove(response.approve());

        log.info("Capture successful: orderId={}, tranId={}", transaction.getOrderId(), response.tranId());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public PaymentTransaction cancel(Long transactionId) {
        PaymentTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.AUTHORIZED
                && transaction.getStatus() != TransactionStatus.CAPTURED) {
            throw new IllegalStateException("Cannot cancel transaction with status: " + transaction.getStatus());
        }

        String jobCd = transaction.getStatus() == TransactionStatus.AUTHORIZED ? "VOID" : "RETURN";
        log.info("AlterTran({}): orderId={}", jobCd, transaction.getOrderId());

        AlterTranResponse response = gmoPgApiClient.alterTran(
                transaction.getAccessId(),
                transaction.getAccessPass(),
                jobCd,
                transaction.getAmount()
        );

        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction.setTranId(response.tranId());

        log.info("Cancel successful: orderId={}, jobCd={}", transaction.getOrderId(), jobCd);
        return transactionRepository.save(transaction);
    }

    public List<PaymentTransaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 27);
    }
}
