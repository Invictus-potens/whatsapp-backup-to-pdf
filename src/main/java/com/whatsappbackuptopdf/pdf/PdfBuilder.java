package com.whatsappbackuptopdf.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.whatsappbackuptopdf.model.MessageModel;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfBuilder {

    public void generate(List<MessageModel> mensagens, String outputPath) {
        if (mensagens == null || mensagens.isEmpty()) {
            System.out.println("No messages found");
            return;
        }
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            Font fontSender = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLUE);
            Font fontText = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font fontDetails = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            for (MessageModel msg : mensagens) {

                Paragraph details = new Paragraph(msg.getDate() + " - " + msg.getTime(), fontDetails);
                document.add(details);
                Paragraph sender = new Paragraph(msg.getSender(), fontSender);
                document.add(sender);
                Paragraph content = new Paragraph(msg.getContent(), fontText);
                content.setSpacingAfter(8f);
                document.add(content);
                document.add(new Paragraph(" "));
            }

            System.out.println("PDF created: " + outputPath);
        } catch (DocumentException | IOException e) {
            System.err.println("Error when creating the pdf: " + e.getMessage());
        } finally {
            document.close();
        }
    }
}