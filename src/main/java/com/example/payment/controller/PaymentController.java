package com.example.payment.controller;

import com.example.payment.dto.PaymentForm;
import com.example.payment.exception.GmoPgApiException;
import com.example.payment.model.PaymentTransaction;
import com.example.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment/authorize")
    public String authorize(@Valid PaymentForm form, BindingResult bindingResult,
                            Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "入力内容に不備があります。");
            return "redirect:/checkout/" + form.getProductId();
        }

        try {
            PaymentTransaction transaction = paymentService.authorize(form.getProductId(), form.getToken());
            model.addAttribute("transaction", transaction);

            if (transaction.getStatus().name().equals("FAILED")) {
                model.addAttribute("error", "決済に失敗しました。カード情報をご確認ください。");
            }

            return "result";
        } catch (GmoPgApiException e) {
            log.error("Payment authorization failed", e);
            model.addAttribute("error", "決済処理でエラーが発生しました: " + e.getMessage());
            return "result";
        } catch (Exception e) {
            log.error("Unexpected error during payment authorization", e);
            model.addAttribute("error", "予期しないエラーが発生しました。");
            return "result";
        }
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("transactions", paymentService.getAllTransactions());
        return "admin";
    }

    @PostMapping("/admin/capture/{id}")
    public String capture(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.capture(id);
            redirectAttributes.addFlashAttribute("success", "売上確定が完了しました。");
        } catch (GmoPgApiException e) {
            log.error("Capture failed", e);
            redirectAttributes.addFlashAttribute("error", "売上確定に失敗しました: " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "取消が完了しました。");
        } catch (GmoPgApiException e) {
            log.error("Cancel failed", e);
            redirectAttributes.addFlashAttribute("error", "取消に失敗しました: " + e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}
