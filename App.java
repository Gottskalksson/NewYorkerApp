package com.raben;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class App extends javax.swing.JFrame {


    private JButton panelButton;
    private JPanel panel1;


    public App() {

        panelButton.setPreferredSize(new Dimension(400, 40));
        panel1.setPreferredSize(new Dimension(200, 200));

        panelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                Path currentDir = Paths.get("");
                File folder = new File(currentDir.toAbsolutePath() + "/Pdf");
                File[] listOfFiles = folder.listFiles();

                int pl = 0, ltVilnius = 0, ltKaunas = 0, lv = 0, ee = 0;

                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(".pdf")) {
                        System.out.println("File " + file.getName());
                        try {
                            PDDocument document;
                            document = PDDocument.load(file);
                            PDFTextStripper pdfTextStripper = new PDFTextStripper();
                            String text = pdfTextStripper.getText(document);
                            document.close();

                            int storageNumber = getStorageNumber(text);
                            if (storageNumber < 39000 || storageNumber > 91000) {
//                                sendEmail(file, getShipmentNumber(text), "user");
                                moveFile(file, currentDir, "PL", Long.parseLong(getShipmentNumber(text)));
                                pl++;
                            } else if (storageNumber > 52000) {  //if shop is in Estonia
                                moveFile(file, currentDir, "EE", storageNumber);
                                ee++;
                            } else if (storageNumber > 51000) { //if shop is in Latvia...
                                moveFile(file, currentDir, "LV", storageNumber);
                                lv++;
                            } else if (storageNumber == 50005 || storageNumber == 50009 || storageNumber == 50011) { // ... or Lithuania Vilno...
                                moveFile(file, currentDir, "LT Vilnius", storageNumber);
                                ltVilnius++;
                            } else if (storageNumber > 50000) { // ... or Lithuania Kaunas
                                moveFile(file, currentDir, "LT Kaunas", storageNumber);
                                ltKaunas++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                JOptionPane.showMessageDialog(null, "Wszystko OK! \n" +
                        "Ilość SSLek do wybranych krajów: \n" +
                        "PL: " + pl +"\n" +
                        "LT Wilno: " + ltVilnius +"\n" +
                        "LT Kowno: " + ltKaunas +"\n" +
                        "LV: " + lv +"\n" +
                        "EE: " + ee +"\n" +
                        "Razem: " + (pl+lv+ee+ltKaunas+ltVilnius));


            }
        });

    }

    private int getStorageNumber(String text) {

        String storeNumberStr = "Store number: ";
        int stringLength = storeNumberStr.length();
        int i = text.indexOf(storeNumberStr);

        String tmp = text.substring(i + stringLength);

        return Integer.parseInt(tmp.substring(0, 5));
    }

    private String getShipmentNumber(String text) {
        String shipmentNumberStr = "Shipment number: ";
        int stringLength = shipmentNumberStr.length();
        int i = text.indexOf(shipmentNumberStr);

        String tmp = text.substring(i + stringLength);

        return tmp.substring(0, 15);
    }

    private void sendEmail(File file, String shipmentNumber, String to) {
        final String username = "user";
        final String password = "pass";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setSubject(shipmentNumber);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("");
            String filename = file.getAbsolutePath();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);


            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);


            //send message
            Transport.send(message);
            System.out.println("message sent successfully");
        } catch (MessagingException e) {throw new RuntimeException(e);}
    }

    private void moveFile (File file, Path currentDir, String country, long storageNumber) {
        file.renameTo(new File(currentDir.toAbsolutePath() + "/Pdf/" + country + "/" + storageNumber + ".pdf"));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("New Yorker");
        frame.setContentPane(new App().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
