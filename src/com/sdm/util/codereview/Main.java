package com.sdm.util.codereview;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Math.class.getName());

    private static final String regEx = "([\\s\\S]*)(</style>[\\s\\S]*)(<table>[\\s\\S]*)";

    private static final String commentStyle =
            "\n" +
                    "h1 { color: blue; font-family: verdana; } " +
                    "p  {color: red; font-family: courier; font-size: 100%;}" +
                    "\n";

    public static void main(String[] args) {
        // write your code here
        displayIt(new File("./data"));
    }

    private static void displayIt(File node) {
        if (node.isDirectory()) {
            LOGGER.info("Processing Directory : " + node.getAbsoluteFile());
            String[] subNote = node.list();
            for (String filename : subNote) {
                displayIt(new File(node, filename));
            }
        } else {
            String fileName = node.getAbsoluteFile().toString();
            if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
                LOGGER.info("Processing HTML file: " + fileName);
                insertComments(fileName);

            }
        }
    }

    private static void insertComments(String htmlFileName) {
        try {
            Path htmlFile = Paths.get(htmlFileName);
            String annotatedHtmlFileName = htmlFileName.replaceAll("(.*)(\\.html)", "$1-annotated$2");

            Path annotatedHtmlFile = Paths.get(annotatedHtmlFileName);

            //read all bytes from htmlFile (they will include bytes representing used line separators)
            byte[] bytesFromHtmlFile = Files.readAllBytes(htmlFile);
            //convert them to string
            String strFromHtmlFile = new String(bytesFromHtmlFile, StandardCharsets.UTF_8);//use proper charset

            Path textFile = Paths.get(htmlFileName.replaceAll("(.*\\.)html", "$1txt"));
            StringBuilder devComments = new StringBuilder((int) Files.size(textFile) + 100);
            devComments.append("<br /><h1><---Start of Development Team's comments---></h1><br />");
            //read all bytes from htmlFile (they will include bytes representing used line separators)
            byte[] bytesFromTextFile = Files.readAllBytes(textFile);
            //convert them to string
            String strFromTextFile = new String(bytesFromTextFile, StandardCharsets.UTF_8);//use proper charset
            devComments.append("<p>" + strFromTextFile + "</p>");
            devComments.append("<br /><h1><---End of Development Team's comments---></h1><br />");

            //replace what you need (line separators will stay the same)
            strFromHtmlFile = strFromHtmlFile.replaceAll(regEx, "$1" + commentStyle + "$2" + devComments + "$3");
            //write back data to annotatedHtmlFile
            Files.write(annotatedHtmlFile, strFromHtmlFile.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
