package ibm.com.gerador.pdf.test;

import com.itextpdf.text.DocumentException;

import ibm.com.gerador.pdf.GeradorPDF;
import ibm.com.gerador.pdf.util.ArquivoUtil;

import java.io.IOException;

public class TestaGeracaoPDF {
  public static void main(String[] args) throws IOException, DocumentException {
    String DEST = "results";
    String TEMPLATE = "template-ata.pdf";
    String HTML = new String(ArquivoUtil.getFileBytes("resources/ata.html"));
    String CSS = "style2.css";
    String orientation = "Port";
    String fileName = "AtaReunicao123.pdf";
    
    GeradorPDF.createPdf("resources", DEST, TEMPLATE, CSS, HTML, orientation, fileName);
    System.out.println("File " + fileName + " has been created!");
  }
}
