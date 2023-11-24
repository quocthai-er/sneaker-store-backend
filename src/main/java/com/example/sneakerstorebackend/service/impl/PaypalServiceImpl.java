package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.config.paypal.PaypalPaymentIntent;
import com.example.sneakerstorebackend.config.paypal.PaypalPaymentMethod;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.service.MailService;
import com.example.sneakerstorebackend.service.PaymentFactory;
import com.example.sneakerstorebackend.service.PaymentService;
import com.example.sneakerstorebackend.util.MailUtils;
import com.example.sneakerstorebackend.util.MoneyUtils;
import com.example.sneakerstorebackend.util.PaymentValidatorUtils;
import com.example.sneakerstorebackend.util.StringUtils;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class PaypalServiceImpl extends PaymentFactory {
    public static final String URL_PAYPAL_SUCCESS = "/api/checkout/paypal/success";
    public static final String URL_PAYPAL_CANCEL = "/api/checkout/paypal/cancel";
    public static final String PATTERN = "&token=";

    private APIContext apiContext;
    private PaymentUtils paymentUtils;
    private final OrderRepository orderRepository;
    private final PaymentValidatorUtils paymentValidatorUtils;
    private final TaskScheduler taskScheduler;
    private final MailUtils mailUtils;
    private final MailService mailService;


    @Override
    public ResponseEntity<?> createPayment(HttpServletRequest request, Order order) {
        String cancelUrl = StringUtils.getBaseURL(request) + URL_PAYPAL_CANCEL;
        String successUrl = StringUtils.getBaseURL(request) + URL_PAYPAL_SUCCESS;
        try {
            Payment payment = createPayPalPayment(
                    order,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    "Thanh toan hoa don "+ order.getId(),
                    cancelUrl,
                    successUrl);
            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    String checkUpdateQuantityProduct = paymentUtils.checkingUpdateQuantityProduct(order, true);
                    if (checkUpdateQuantityProduct == null) {
                        if (!payment.getTransactions().isEmpty())
                            order.getPaymentDetail().getPaymentInfo().put("amount", payment.getTransactions().get(0).getAmount());
                        order.getPaymentDetail().setPaymentId(payment.getId());
                        order.getPaymentDetail().setPaymentToken((links.getHref().split(PATTERN)[1]));
                        order.getPaymentDetail().getPaymentInfo().put("isPaid", false);
                        orderRepository.save(order);
                        paymentValidatorUtils.setOrderId(order.getId());
                        paymentValidatorUtils.setOrderRepository(orderRepository);
                        paymentValidatorUtils.setPaymentUtils(paymentUtils);
                        taskScheduler.schedule(paymentValidatorUtils, new Date(System.currentTimeMillis() + ConstantsConfig.PAYMENT_TIMEOUT)) ;
                        return ResponseEntity.status(HttpStatus.OK).body(
                                new ResponseObject(true, "Payment init complete", links.getHref()));
                    }
                }
            }
        } catch (PayPalRESTException | IOException e) {
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
        }
        return null;
    }

    @SneakyThrows
    @Override
    public ResponseEntity<?> executePayment(String paymentId, String payerId, String responseCode, String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            Payment payment= execute(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                String paymentToken = "EC-" + payment.getCart();
                Optional<Order> order = orderRepository.findOrderByPaymentDetail_PaymentTokenAndState(
                        paymentToken, ConstantsConfig.ORDER_STATE_PROCESS);
                if (order.isPresent()) {
                    order.get().getPaymentDetail().getPaymentInfo().put("payer", payment.getPayer().getPayerInfo());
                    order.get().getPaymentDetail().getPaymentInfo().put("paymentMethod", payment.getPayer().getPaymentMethod());
                    order.get().getPaymentDetail().getPaymentInfo().put("isPaid", true);
                    order.get().setState(ConstantsConfig.ORDER_STATE_PREPARE);
                    orderRepository.save(order.get());
                    mailUtils.setOrder(order.get());
                    mailUtils.setMailService(mailService);
                    taskScheduler.schedule(mailUtils, new Date(System.currentTimeMillis())) ;
                } else {
                    response.sendRedirect(PaymentServiceImpl.CLIENT_REDIRECT + "false&cancel=false");
                    throw new NotFoundException("Can not found order with id: " + id);
                }
                response.sendRedirect(PaymentServiceImpl.CLIENT_REDIRECT + "true&cancel=false");
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Payment with Paypal complete", "")
                );
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        response.sendRedirect(PaymentServiceImpl.CLIENT_REDIRECT + "false&cancel=false");
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(
                new ResponseObject(false, "Payment with Paypal failed", "")
        );
    }

    public Payment execute(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecute);
    }

    public Payment createPayPalPayment(Order order, String currency, PaypalPaymentMethod method,
                                       PaypalPaymentIntent intent, String description, String cancelUrl,
                                       String successUrl) throws PayPalRESTException, IOException {
        double total = MoneyUtils.exchange(order.getTotalPrice().add(new BigDecimal(order.getDeliveryDetail().getDeliveryInfo().get("fee").toString())));
        Amount amount = new Amount(currency, String.format("%.2f", total));
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method.toString());

        Payment payment = new Payment(intent.toString(),payer);
        payment.setTransactions(transactions);
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);
        apiContext.setMaskRequestId(true);
        return payment.create(apiContext);
    }
}
