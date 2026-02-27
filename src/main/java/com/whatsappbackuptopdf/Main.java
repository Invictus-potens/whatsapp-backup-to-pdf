package com.whatsappbackuptopdf;

import com.whatsappbackuptopdf.extractor.ZipExtractor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import com.whatsappbackuptopdf.model.MessageModel;
import com.whatsappbackuptopdf.parser.ChatParser;

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

                    extractor.extract(zip.getAbsolutePath(), destPath);

                    File pastaSaida = new File(destPath);
                    File[] arquivosTxt = pastaSaida.listFiles((dir, name) -> name.endsWith(".txt"));

                    if (arquivosTxt != null) {
                        for (File txt : arquivosTxt) {

                            List<MessageModel> mensagens = parser.parse(txt.getAbsolutePath());
                            System.out.println("Parsed " + mensagens.size() + " messages from " + txt.getName());
                            com.whatsappbackuptopdf.pdf.PdfBuilder pdfBuilder = new com.whatsappbackuptopdf.pdf.PdfBuilder();
                            String nomePdf = txt.getName().toLowerCase().replace(".txt", ".pdf");
                            pdfBuilder.generate(mensagens, destPath + "/" + nomePdf);
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
