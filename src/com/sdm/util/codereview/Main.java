package com.sdm.util.codereview;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Math.class.getName());

    private static final String regEx = "([\\s\\S]*)(</style>[\\s\\S]*)(<table[\\s\\S]*)";
    private static final String cmtLnNumRegEx = "\\s*\\{\\s*line\\s*:\\s*(\\d+)([LR])\\}\\s-\\s(.*)";
    private static final Pattern cmtLnNumRegExPattern = Pattern.compile(cmtLnNumRegEx);
    private static final String cmtLnNumSub = "<a href=\"#Line$1$2\">Line#:$1$2</a> -> $3 <br />";

    private static final String commentStyle =
            "\n" +
                    "h1 {color: blue; font-family: verdana; font-size: 100%;}\n" +
                    "p  {color: red;  font-family: courier; font-size: 100%;}\n" +
                    ".tooltip {\n" +
                    "    position: relative;\n" +
                    "    display: inline-block;\n" +
                    "    color: red; \n" +
                    "    border-bottom: 2px dotted blue;\n" +
                    "}\n" +
                    "\n" +
                    ".tooltip .tooltiptext {\n" +
                    "    visibility: hidden;\n" +
                    "    width: 120px;\n" +
                    "    background-color: black;\n" +
                    "    color: #fff;\n" +
                    "    text-align: center;\n" +
                    "    border-radius: 6px;\n" +
                    "    padding: 5px 0;\n" +
                    "\n" +
                    "    /* Position the tooltip */\n" +
                    "    position: absolute;\n" +
                    "    z-index: 1;\n" +
                    "}\n" +
                    "\n" +
                    ".tooltip:hover .tooltiptext {\n" +
                    "    visibility: visible;\n" +
                    "}" +
                    "\n";
    private static final String anchorFormatterStr =
            "<a name=\"Line{0}{1}\" >" +
            "<div class=\"tooltip\">{0}\n"+
            "  <span class=\"tooltiptext\">{2}</span>\n"+
            "</div></a>";

    public static void main(String[] args) {

        if (args.length == 0){
            LOGGER.info("\nExecution syntax:\n\t" +
                    "java -jar <path to code review report jar>/CodeReviewReport.jar " +
                    "<list of root directories containing code merge reports separated by spaces.>\n");
            return;
        }

        for (String arg: args){
            LOGGER.info("Processing report directory: " + arg + ".....");
            // write your code here
            processReports(new File(arg));
        }
    }

    private static void processReports(File node) {
        if (node.isDirectory()) {
            LOGGER.info("Processing Directory : " + node.getAbsoluteFile());
            String[] subNote = node.list();
            for (String filename : subNote) {
                processReports(new File(node, filename));
            }
        } else {
            String fileName = node.getAbsoluteFile().toString();
            if (!fileName.endsWith("-annotated.html") && fileName.endsWith(".html")) {
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

            int devCommentsSize = (int) Files.size(htmlFile) + 100;
            if (Files.exists(textFile)) {
                devCommentsSize += Files.size(textFile);
            }
            StringBuilder devComments = new StringBuilder(devCommentsSize);

            devComments.append("<br /><h1><---Start of Development Team's comments---></h1><br />");

            if (Files.exists(textFile)) {
                //read all bytes from htmlFile (they will include bytes representing used line separators)
                byte[] bytesFromTextFile = Files.readAllBytes(textFile);
                //convert them to string
                String strFromTextFile = new String(bytesFromTextFile, StandardCharsets.UTF_8);//use proper charset
                if (cmtLnNumRegExPattern.matcher(strFromTextFile).find()) {
                    strFromHtmlFile = insertLineComments(strFromHtmlFile, strFromTextFile);
                    strFromTextFile =
                            strFromTextFile.replaceAll(cmtLnNumRegEx, cmtLnNumSub);
                }
                strFromTextFile =
                        strFromTextFile.replaceAll(System.getProperty("line.separator"), "<br />");
                devComments.append("<p>" + strFromTextFile + "</p>");
            }

            devComments.append("<br /><h1><---End of Development Team's comments---></h1><br />");

            //replace what you need (line separators will stay the same)
            strFromHtmlFile = strFromHtmlFile.replaceAll(regEx, "$1"
                    + commentStyle + "$2"
                    + devComments + "$3");
            //write back data to annotatedHtmlFile
            Files.write(annotatedHtmlFile, strFromHtmlFile.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private static String insertLineComments(String strFromHtmlFile, String strFromTextFile) {
        Matcher m = Pattern.compile(cmtLnNumRegEx).matcher(strFromTextFile);
        while (m.find()) {
            String lineNum = m.group(1);
            String side = m.group(2);
            String comment = m.group(3);

            Object[] params = new Object[]{lineNum, side, comment};
            String msg = MessageFormat.format(anchorFormatterStr, params);

            strFromHtmlFile = strFromHtmlFile
                    .replaceAll("([\\s]*<td class=\"LineNum\">)(" + lineNum + ")(</td>[\\s]*)",
                            "$1" + msg + "$3");
        }
        return strFromHtmlFile;
    }
}

