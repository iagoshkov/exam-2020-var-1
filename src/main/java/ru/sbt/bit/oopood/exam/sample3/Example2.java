package ru.sbt.bit.oopood.exam.sample3;

import java.sql.*;
import java.util.Properties;

public class Example2 {
    public void checkTrades() throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", "admin");
        connectionProps.put("password", "pass");
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://quotes-db.sberbank.ru:1515", connectionProps);
        Trade trade = readNextTrade();
        while (trade != null) {
            if ("Equity".equals(trade.getType())) {
                PreparedStatement statement = conn.prepareStatement("select min(bid)as b, max(ask)as a from market_quotes" +
                        "where timestamp < " + (trade.getDateTime().getTime() + 300 * 1000)
                        + " and timestamp > " + (trade.getDateTime().getTime() - 300 * 1000)
                        + " instrument = " + trade.getInstrument());
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    double loValue = rs.getDouble("b");
                    double hiValue = rs.getDouble("a");
                    if (trade.getAmount() < loValue || trade.getPrice() > hiValue) {
                        PreparedStatement insertAlert = conn.prepareStatement("insert into trade_alerts(tradeId, alert_message)" +
                                " values (" + trade.getId() + ",'Тест провален. Значение вне границ коридора" +
                                "[" + loValue + "-" + hiValue + "]");
                        insertAlert.execute();
                    }
                }
            }
            if ("Bond".equals(trade.getType())) {
                PreparedStatement statement = conn.prepareStatement("select avg(bid + ask)as avg_ba from market_quotes" +
                        "where timestamp < " + (trade.getDateTime().getTime() + 100 * 1000)
                        + " and timestamp > " + (trade.getDateTime().getTime() - 100 * 1000)
                        + " instrument = " + trade.getInstrument());
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    double loValue = rs.getDouble("avg_ba") - 100;
                    double hiValue = rs.getDouble("avg_ba") + 100;
                    if (trade.getAmount() < loValue || trade.getPrice() > hiValue) {
                        PreparedStatement insertAlert = conn.prepareStatement("insert into trade_alerts(tradeId, alert_message)" +
                                " values (" + trade.getId() + ",'Тест провален. Значение вне границ коридора" +
                                "[" + loValue + "-" + hiValue + "]");
                        insertAlert.execute();
                    }
                }
            }
            trade = readNextTrade();
        }
    }

    private Trade readNextTrade() {
// считываем сделку из очереди-потока сделок
        return null;
    }
}