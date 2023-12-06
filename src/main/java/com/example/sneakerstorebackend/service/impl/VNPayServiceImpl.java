package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.order.Order;
import com.example.sneakerstorebackend.repository.OrderRepository;
import com.example.sneakerstorebackend.service.MailService;
import com.example.sneakerstorebackend.service.PaymentFactory;
import com.example.sneakerstorebackend.util.MailUtils;
import com.example.sneakerstorebackend.util.PaymentValidatorUtils;
import com.example.sneakerstorebackend.util.StringUtils;
import com.example.sneakerstorebackend.util.VNPayUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl extends PaymentFactory {
    private final OrderRepository orderRepository;
    private final PaymentUtils paymentUtils;
    private final PaymentValidatorUtils paymentValidatorUtils;
    private final TaskScheduler taskScheduler;
    private final MailUtils mailUtils;

    private final MailService mailService;
    @SneakyThrows
    @Override
    public ResponseEntity<?> createPayment(HttpServletRequest request, Order order) {
        order.setState(ConstantsConfig.ORDER_STATE_PROCESS);
        order.getPaymentDetail().getPaymentInfo().put("isPaid", false);
        orderRepository.save(order);
        Map<String, Object> vnp_Params = mapVnPayParam(order, request);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName) + "";
            if (!fieldValue.isBlank() && fieldValue.length() > 0) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtils.hmacSHA512(VNPayUtils.vnp_HashSecret, hashData.toString());
        queryUrl += VNPayUtils.vnp_SecureHash + vnp_SecureHash;
        String paymentUrl = VNPayUtils.vnp_PayUrl + "?" + queryUrl;
        String checkUpdateQuantityProduct = paymentUtils.checkingUpdateQuantityProduct(order, true);
        if (checkUpdateQuantityProduct == null) {
            paymentValidatorUtils.setOrderId(order.getId());
            paymentValidatorUtils.setOrderRepository(orderRepository);
            paymentValidatorUtils.setPaymentUtils(paymentUtils);
            taskScheduler.schedule(paymentValidatorUtils, new Date(System.currentTimeMillis() + ConstantsConfig.PAYMENT_TIMEOUT)) ;
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Payment Complete", paymentUrl));
        } else throw new AppException(HttpStatus.CONFLICT.value(), "Quantity exceeds the available stock!");
    }

    public Map<String, Object> mapVnPayParam(Order order, HttpServletRequest request) {
        String vnp_IpAddr = VNPayUtils.getIpAddress(request);
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String total = String.valueOf((order.getTotalPrice().add(new BigDecimal(order.getDeliveryDetail().getDeliveryInfo().get("fee").toString())))
                .multiply(BigDecimal.valueOf(100)));

        Map<String, Object> vnp_Params = new HashMap<>();
        vnp_Params.put(VNPayUtils.vnp_Version_k, VNPayUtils.vnp_Version);
        vnp_Params.put(VNPayUtils.vnp_Command_k, VNPayUtils.vnp_Command);
        vnp_Params.put(VNPayUtils.vnp_TmnCode_k, VNPayUtils.vnp_TmnCode);
        vnp_Params.put(VNPayUtils.vnp_CurrCode, VNPayUtils.vnp_currCode);
        vnp_Params.put(VNPayUtils.vnp_TxnRef_k, vnp_TxnRef);
        vnp_Params.put(VNPayUtils.vnp_OrderInfo_k, order.getId());
        vnp_Params.put(VNPayUtils.vnp_OrderType, VNPayUtils.vnp_orderType);
        vnp_Params.put(VNPayUtils.vnp_Locale, VNPayUtils.vn);
        vnp_Params.put(VNPayUtils.vnp_ReturnUrl, StringUtils.getBaseURL(request) + VNPayUtils.vnp_Returnurl);
        vnp_Params.put(VNPayUtils.vnp_IpAddr, vnp_IpAddr);
        vnp_Params.put(VNPayUtils.vnp_Amount, total);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(VNPayUtils.GMT));
        SimpleDateFormat formatter = new SimpleDateFormat(VNPayUtils.yyyyMMddHHmmss);
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put(VNPayUtils.vnp_CreateDate, vnp_CreateDate);
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put(VNPayUtils.vnp_ExpireDate, vnp_ExpireDate);

        String fullName = order.getUser().getName();
        if (fullName != null && !fullName.isEmpty()) {
            int idx = fullName.indexOf(' ');
            if (idx != -1) {
                String firstName = fullName.substring(0, idx);
                String lastName = fullName.substring(fullName.lastIndexOf(' ') + 1);
                vnp_Params.put(VNPayUtils.vnp_Bill_FirstName, firstName);
                vnp_Params.put(VNPayUtils.vnp_Bill_LastName, lastName);
            } else {
                vnp_Params.put(VNPayUtils.vnp_Bill_FirstName, fullName);
                vnp_Params.put(VNPayUtils.vnp_Bill_LastName, fullName);
            }
        }
        return vnp_Params;
    }


    @SneakyThrows
    @Override
    public ResponseEntity<?> executePayment(String paymentId, String payerId, String responseCode, String id, HttpServletRequest request, HttpServletResponse response) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty() || !order.get().getState().equals(ConstantsConfig.ORDER_STATE_PROCESS)) {
            response.sendRedirect(PaymentServiceImpl.CLIENT_REDIRECT + "false&cancel=false");
            throw new NotFoundException("Can not found order with id: " + id);
        }
        if (responseCode.equals(VNPayUtils.responseSuccessCode)) {
            order.get().getPaymentDetail().getPaymentInfo().put("amount", request.getParameter(VNPayUtils.vnp_Amount));
            order.get().getPaymentDetail().getPaymentInfo().put("bankCode", request.getParameter("vnp_BankCode"));
            order.get().getPaymentDetail().getPaymentInfo().put("transactionNo", request.getParameter("vnp_TransactionNo"));
            order.get().getPaymentDetail().getPaymentInfo().put("isPaid", true);
            order.get().setState(ConstantsConfig.ORDER_STATE_PREPARE);
            orderRepository.save(order.get());
            mailUtils.setOrder(order.get());
            mailUtils.setMailService(mailService);
            taskScheduler.schedule(mailUtils, new Date(System.currentTimeMillis())) ;
            response.sendRedirect(PaymentServiceImpl.CLIENT_REDIRECT + "true&cancel=false");
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Payment Completed", "")
            );
        } else {
            order.get().setState(ConstantsConfig.ORDER_STATE_CANCEL);
            orderRepository.save(order.get());
            String checkUpdateQuantityProduct = paymentUtils.checkingUpdateQuantityProduct(order.get(), false);
            if (responseCode.equals(VNPayUtils.responseCancelCode) && checkUpdateQuantityProduct == null) {
                response.sendRedirect(PaymentServiceImpl.CLIENT_REDIRECT + "true&cancel=true");
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Payment cancel complete", ""));
            } else response.sendRedirect(PaymentServiceImpl.CLIENT_REDIRECT + "false&cancel=false");
            throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Failed when payment");
        }
    }

    @Override
    public ResponseEntity<?> cancelPayment(String id, String responseCode, HttpServletResponse response) {
        return null;
    }
}
