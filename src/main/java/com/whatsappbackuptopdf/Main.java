package com.whatsappbackuptopdf;

import com.whatsappbackuptopdf.extractor.ZipExtractor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import com.whatsappbackuptopdf.model.MessageModel;
import com.whatsappbackuptopdf.parser.ChatParser;
import com.whatsappbackuptopdf.pdf.PdfBuilder;
import com.whatsappbackuptopdf.pdf.HtmlGenerator;


public class Main {
    public static void main(String[] args) {
        ZipExtractor extractor = new ZipExtractor();
        ChatParser parser = new ChatParser();
        File currentDir = new File("./");
        String destPath = "./output_folder";

        File[] zipFiles = currentDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));

        if (zipFiles != null && zipFiles.length > 0) {
            System.out.println("Found " + zipFiles.length + " zip files.");

            for (File zip : zipFiles) {
                try {

                    String nomeContato = zip.getName()
                            .replaceAll("(?i)whatsapp chat with ", "")
                            .replaceAll("\\.zip$", "")
                            .trim();

                    System.out.println("Contact found: " + nomeContato);
                    extractor.extract(zip.getAbsolutePath(), destPath);

                    File pastaSaida = new File(destPath);
                    File[] arquivosTxt = pastaSaida.listFiles((dir, name) -> name.endsWith(".txt"));

                    if (arquivosTxt != null) {
                        for (File txt : arquivosTxt) {

                            List<MessageModel> mensagens = parser.parse(txt.getAbsolutePath());
                            System.out.println("Parsed " + mensagens.size() + " messages from " + txt.getName());
                            if (mensagens.isEmpty()){
                                System.out.println("No messages found");
                                continue;
                            }

                            String nomeRemetente = parser.detectarRemetente(mensagens, nomeContato);
                            System.out.println("Remetente detectado: " + nomeRemetente);
                            String nomePdf = txt.getName().toLowerCase().replace(".txt", ".pdf");
                            PdfBuilder pdfBuilder = new PdfBuilder(nomeRemetente, destPath);
                            pdfBuilder.gerarPdf(mensagens, "./" + nomePdf);
                        }
                    }

                    System.out.println("Finished processing zip: " + zip.getName());

                } catch (IOException e) {
                    System.err.println("Error extracting " + zip.getName() + ": " + e.getMessage());
                }
            }
            System.out.println("Process finished. ");
        } else {
            System.out.println("No files found.");
        }
    }
}
