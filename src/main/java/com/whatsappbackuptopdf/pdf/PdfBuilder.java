package com.whatsappbackuptopdf.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.whatsappbackuptopdf.model.MessageModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PdfBuilder {

    private final HtmlGenerator htmlGenerator;

    public PdfBuilder(String nomeRemetente) {
        this.htmlGenerator = new HtmlGenerator(nomeRemetente);
    }

    public void gerarPdf(List<MessageModel> mensagens, String caminhoSaida) throws IOException {
        String html = htmlGenerator.gerarHtml(mensagens);

        try (OutputStream os = new FileOutputStream(caminhoSaida)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
        }

        System.out.println("PDF gerado: " + caminhoSaida);
    }
}