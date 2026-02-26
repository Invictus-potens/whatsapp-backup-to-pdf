package com.whatsappbackuptopdf;

import com.whatsappbackuptopdf.extractor.ZipExtractor;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ZipExtractor extractor = new ZipExtractor();
        File currentDir = new File ("./");
        String destPath = "./output_folder";

        File[] zipFiles = currentDir.listFiles((dir,name) -> name.toLowerCase().endsWith(".zip"));

        if (zipFiles != null && zipFiles.length > 0) {
            System.out.println("Found " + zipFiles.length + " zip files.");

            for (File zip : zipFiles) {
                try {
                    System.out.println("Starting the extraction of " + zip.getName());
                    extractor.extract(zip.getAbsolutePath(), destPath);
                    System.out.println("Process finished for " +zip.getName());
                } catch (IOException e) {
                    System.err.println("Error extracting " + zip.getName() + ": " + e.getMessage());
                }
            }
            System.out.println("Process finished. ");
        }else {
            System.out.println("No files found.");
        }
    }


}