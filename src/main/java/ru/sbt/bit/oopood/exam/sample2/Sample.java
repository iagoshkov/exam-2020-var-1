package ru.sbt.bit.oopood.exam.sample2;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Sample {
    public void remindPasswordExpiration() throws IOException, SQLException, MessagingException {
        Properties prop = new Properties(); // for password policy properties
        prop.load( new FileInputStream( "config.properties") );
        Properties connectionProps = new Properties(); // for SQL connection properties
        connectionProps.put( "user" , "admin" );connectionProps.put(" password" , "pass" );
        Connection conn = DriverManager.getConnection (
                "jdbc:mysql://server1.sberbank.ru:1515" , connectionProps);
        PreparedStatement statement = conn.prepareStatement( "select * from users") ;
        ResultSet rs = statement.executeQuery(); // select all users
        while (rs.next()) { // iterate over users
            String username = rs.getString( "username" );
            String useremail = rs.getString( "email" );
            String lastPasswordChangeDate = rs.getString(" last_password_change_date") ;
            // calculate how many days passed since last password change
            long daysCountFromNow = TimeUnit.DAYS.convert(System. currentTimeMillis() -
                    Date.valueOf(lastPasswordChangeDate).getTime(), TimeUnit. MILLISECONDS );
            // read password expiration days count setting
            int passwordExpirationDays = Integer.parseInt(prop.getProperty("pwd-expire-days" ));
            if ((daysCountFromNow < passwordExpirationDays) && (passwordExpirationDays -
                    daysCountFromNow < 7 )) {
                // send notification e-mail that password will expire soon
                Session session = Session.getInstance ( new Properties());
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(" robot@sbertech.ru" ));
                message.setRecipients(Message.RecipientType. TO ,
                        InternetAddress.parse(useremail));
                message.setSubject("Your password has expired" );
                message.setText("Dear " + username + " ,"
                        + "\n Your password expires in " + (passwordExpirationDays -
                        daysCountFromNow) + " days." );
                Transport.send(message);
            }
        }
    }
}
