package com.whatsappbackuptopdf;
import com.whatsappbackuptopdf.extractor.ZipExtractor;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ZipExtractor extractor = new ZipExtractor();

        String zipPath = "C:\\Users\\felip\\IdeaProjects\\whatsapp-backup-to-pdf\\whatsapp.zip";
        String destPath = "./output_folder";
    try {
        System.out.println("Starting");
        extractor.extract(zipPath, destPath);
        System.out.println("Process finished");
    } catch (IOException e) {
        System.err.println("Error " + e.getMessage());
    }

    }
}