package com.whatsappbackuptopdf.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.whatsappbackuptopdf.model.MessageModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PdfBuilder {

    private final HtmlGenerator htmlGenerator;
    private final String outputFolderPath;

    public PdfBuilder(String nomeRemetente, String outputFolderPath) {
        this.outputFolderPath = outputFolderPath;
        this.htmlGenerator = new HtmlGenerator(nomeRemetente, outputFolderPath);
    }

    public void gerarPdf(List<MessageModel> mensagens, String caminhoSaida) throws IOException {
        String html = htmlGenerator.gerarHtml(mensagens);

        Path tempHtml = Paths.get(outputFolderPath).toAbsolutePath().resolve("_temp_chat.html");
        Files.writeString(tempHtml, html, StandardCharsets.UTF_8);

        try (OutputStream os = new FileOutputStream(caminhoSaida)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withUri(tempHtml.toUri().toString());
            builder.toStream(os);
            builder.run();
        } finally {
            Files.deleteIfExists(tempHtml);
        }

        System.out.println("PDF gerado: " + caminhoSaida);
    }
}
