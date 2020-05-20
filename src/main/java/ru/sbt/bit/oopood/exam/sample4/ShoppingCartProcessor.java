package ru.sbt.bit.oopood.exam.sample4;

import ru.sbt.bit.oopood.exam.sample1.User;

import java.sql.*;

public class ShoppingCartProcessor {
    private static final String USER = "carduser";
    private static final String PASS = "1235$%#asdbg12g";
    private PayPalInfoProvider payPalInfoProvider;

    public String processItems(Basket basket, User user, String paymentType) throws SQLException {
        double totalOrderSum = 0;
        for (Item item : basket.getItems()) {
            totalOrderSum += item.getQuantity() * item.getPrice();
        }
        if (paymentType == "CREDIT_CARD") {
            Connection conn = null;
            Statement stmt = null;
            conn = DriverManager.getConnection("jdbc:mysql://localhost/u_crds", USER, PASS);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT cardType, cardNumber, validTo, holderName, " +
                    "cvc FROM user_cards WHERE user_id = " + user.getId());
            if (rs.next()) {
                String cardNumber = rs.getString("cardNumber");
                String validTo = rs.getString("validTo");
                String holderName = rs.getString("holderName");
                String cvc = rs.getString("cvc");
                String cardType = rs.getString("cardType");
                if ("cardType".equals("VISA")) {
                    VisaPaymentService.processVisaPayment(cardNumber, validTo,
                            holderName, cvc);
                } else if ("cardType".equals("MASTERCARD")) {
                    MasterCard cardInfo = new MasterCard(cardNumber, validTo, holderName);
                    cardInfo.setCvc(cvc);
                    MasterCardPaymentSvc.processPayment(cardInfo);
                }
            }
            rs.close();
            stmt.close();
            conn.close();
        } else if (paymentType == "PayPal") {
            PayPalUserAccount ppUserAcount = payPalInfoProvider.getUserAccountInfo(user.getId());
            PayPalProcessor.transferMoney(ppUserAcount);
        }
        String notificationEmailText = "<html><title>Your order #" + basket.getOrderNumber() + " payment details</title >";
        notificationEmailText += "<body>" +
                "Your order has been successfully processed. Total amount is " +
                totalOrderSum + " charged from your account" +
                "Thank you for your purchase!" + "</body></html>";
        EmailSenderService.sendMail(user.getEmail(), notificationEmailText);
        return notificationEmailText;
    }
}
