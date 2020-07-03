package ibm.com.gerador.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.html.Tags;

import ibm.com.gerador.pdf.util.ArquivoUtil;
import ibm.com.gerador.pdf.util.GerenciadorEventoPDF;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GeradorPDF {
  
	
	/**
	 * método principal para ser chamado pelo BPM, recebe todos os parametros como string. Este método usa um template já em PDF, o conteúdo em HTML e o formato em CSS e gerado um novo PDF com ajunçào desse material 
	 * @param pathTemplate caminho onde o template esta (por exmeplo /tmp/template)
	 * @param pathFileTemp caminho onde o arquivo PDF será gerado (por exemplo /tmp/resultado)
	 * @param templateName nome do template pdf, nome do arquivo dentro da pasta do primeiro parametro (por exemplo meutemplate.pdf)
	 * @param cssFileName nome do arquivo css que será usado na formatação do conteúdo, deve estar na mesma pasta do template
	 * @param htmlString conteúdo em formato HTML / String que será populado no PDF
	 * @param orientation Port ou Landscape
	 * @param fileName  nome do PDF que será gerado
	 * @return retorna o nome do PDF gerado ao final do processo
	 * @throws IOException essa exceção precisa ser tratada caso algum dos arquivos informados não seja encontrado
	 * @throws DocumentException essa exceção trata qualquer problema ao usar o template PDF ou mesmo para criar o Documento PDF usando o conteúdo HTML
	 */
	public static String createPdf(String pathTemplate, String pathFileTemp, String templateName, String cssFileName, String htmlString, String orientation, String fileName) throws IOException, DocumentException 
	{
    
		//verifica se o caminho do template foi informado com a barra no final, caso contrario inclui
		if (pathTemplate != null && !pathTemplate.endsWith("/"))
		{
			pathTemplate = String.valueOf(pathTemplate) + "/";
		}
		//verifica se o caminho do arquivo que será gerado foi informado com a barra no final, caso contrario inclui
		if (pathFileTemp != null && !pathFileTemp.endsWith("/"))
		{
			pathFileTemp = String.valueOf(pathFileTemp) + "/";
		}
    
		//cria objetos com o caminho completo do template e do CSS
		String fullPathTemplate = String.valueOf(pathTemplate) + templateName;
		File templateFile = new File(fullPathTemplate);
		File ccsFile = new File(String.valueOf(pathTemplate) + cssFileName);

		//converte o conteúdo dos arquivos (template / html / css) em array de bytes
		byte[] template = ArquivoUtil.getFileBytes(templateFile);
		byte[] htmlBytes = htmlString.getBytes();
		byte[] cssBytes = ArquivoUtil.getFileBytes(ccsFile);
		
		//gera o conteúdo do PDF e salva em um array de byte
		byte[] result = createPdf(template, htmlBytes, cssBytes, orientation, fileName);
		
		//grava o conteúdo do array de Byte com os dados PDF em um arquivo temporário 
		ArquivoUtil.salvaArquivo(pathFileTemp, result, fileName + ".tmp");
    
		//abre o arquivo gerado e inclui a informação de paginação nele geradno o arquivo final
		inserePaginacao(pathFileTemp + fileName + ".tmp", pathFileTemp + fileName);
    
		//remove o arquivo temporário (a versão sem paginação)
		File arquivoTmp = new File(pathFileTemp + fileName + ".tmp");
		arquivoTmp.delete();
    
    return fileName;
  }
	
/**
 * método responsável por abrir um PDF, inserir a informação de paginação nele e salvar com outro nome	
 * @param src caminho e nome do PDF original
 * @param dest caminho e nome do PDF que será gerado com a informação de paginação
 * @throws IOException Exceção que precsa ser tratada caso algum dos caminhos e nome de arquivo informados não forem válidos
 * @throws DocumentException
 */
 public static void inserePaginacao(String src, String dest) throws IOException, DocumentException 
 {
	 //le o arquivo de origem
	 PdfReader reader = new PdfReader(src);
	 //recupera o número total de páginas do pdf original
	 int n = reader.getNumberOfPages();
	 
     //seta a font que será usada para escrever o texto "Página X de Y"
	 Font fontRodape = new Font(BaseFont.createFont(), 9.0F);
        
     //Cria um PDF do tipo Stamper que é quando queremos escrever em cima de outro PDF 
	 PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
     
	 PdfContentByte pagecontent;
     //varre todas as páginas do PDF e escreve no rodapé a informação de paginação
	 for (int i = 0; i < n; ) 
	 {
		 //cria a área onde será escrita o texto, e garente que ela fique por cima do conteúdo existente
		 pagecontent = stamper.getOverContent(++i);
		 
		 //escreve o texto 
         ColumnText.showTextAligned(pagecontent, Element.ALIGN_RIGHT,
                    new Phrase(String.format("página %s de %s", i, n),fontRodape), 559, 20, 0);
	 }
     //fecha o PDF (que é o momento em que ele salva o documento
	 stamper.close();
     
	 //fecha o documento original
	 reader.close();
    }
  
 /**
  * este é o método que cria o PDF em si
  * @param template conteúdo (em bytes) do PDF que será usado como template para o novo documento 
  * @param html conteúdo (em bytes) do html que tem as inforações variáveis que serão incluidas no PDF
  * @param css conteúdo (em bytes) do CSS que vai definir o formato (fontes, tamanho, cores) com que o conteúdo HTML será gravado no PDF
  * @param orientation Orientação do documento que pode ser paisagem (Landspace) ou Retrato (Port)
  * @param fileName nome do arquivo que será gerado (este método não grava o arquivo físico em si, é apenas uma informação que será gravada nos metadados do PDF
  * @return conteúdo do PDF em bytes 
  * @throws IOException
  * @throws DocumentException
  */
  private static byte[] createPdf(byte[] template, byte[] html, byte[] css, String orientation, String fileName) throws IOException, DocumentException 
  {
	  //recupera uma instancia da classe que monitra os eventos da criação do PDF e que recebe o template como base
	  GerenciadorEventoPDF templateHelper = new GerenciadorEventoPDF(template);
	  
	  //cria a instancia do novo documento, até estre momento vazia
	  Document document = new Document();
	  
	  //verifica o parametro orientação e configura o documento com essa informação
	  if (orientation.equals("Landscape")) 
	  {
		  document = new Document(templateHelper.getPageSize().rotate());
	  } else 
	  {
		  document = new Document(templateHelper.getPageSize());
	  }
	  
	  //Cria os objetos para o buffer de gravação de conteúdo
	  OutputStream output = new ByteArrayOutputStream();
	  PdfWriter writer = PdfWriter.getInstance(document, output);
    
	  //define o noss gerenciador de eventos como o montior dos eventos da criação do documento
	  writer.setPageEvent((PdfPageEvent)templateHelper);
	  //abre o documento para começar a construção dele
	  document.open();
    
	  //cria a primeira área para escrita no documento
	  ColumnText ct = new ColumnText(writer.getDirectContent());
	  //escreve nela o conteúdo que estiver no documento de template
	  ct.setSimpleColumn(templateHelper.getBody());
    
	  //contever o conteúdo html considerando a formatação desejada definida no css em uma lista de Elementos 
	  ElementList elements = GerenciadorEventoPDF.parseHtml(html, css, Tags.getHtmlTagProcessorFactory());
	  
	  //varre alista de elementos gerada a partir do HTML com o conteúdo variável
	  for (Element e : elements) 
	  {
		  //valida se a área atual permite a gravação do tipo de elemento em questão
		  if (!ColumnText.isAllowedElement(e))
		  {
			  continue;
		  }
		  //verifica se é uma tabela e seta o header
		  if (e instanceof PdfPTable)
		  {
			  ((PdfPTable)e).setHeaderRows(1);
		  }
		  //recupera a linha atual de escrita no PDF e adiciona o elemento
		  float yLine = ct.getYLine();
		  ct.addElement(e);

		  //verifica se há mais texto para adicionar no mesmo elemento
		  int status = ct.go(true);
		  if (!ColumnText.hasMoreText(status)) 
		  {
			  ct.setYLine(yLine);
			  ct.addElement(e);
			  ct.go();
			  continue;
		  } 
      
		  int rowsDrawn = ct.getRowsDrawn();
		  ct.setText(null);
		  ct.addElement(e);
		  ct.setSimpleColumn(templateHelper.getBody());
		  status = ct.go(true);
      
		  if (ColumnText.hasMoreText(status) && rowsDrawn > 2) 
		  {
			  ct.setYLine(yLine);
			  ct.setText(null);
			  ct.addElement(e);
			  ct.go();
		  } 
		  else 
		  {
			  ct.setText(null);
			  ct.addElement(e);
		  } 
      
		  //vai criando quantas páginas forem necessárias para acomodar o conteúdo do elemento atual
		  document.newPage();
		  ct.setSimpleColumn(templateHelper.getBody());
		  status = ct.go();
		  while (ColumnText.hasMoreText(status)) 
		  {
			  document.newPage();
			  ct.setSimpleColumn(templateHelper.getBody());	
			  status = ct.go();
		  } 
    } 
   
	//salva o documento
    document.close();

    return ((ByteArrayOutputStream)output).toByteArray();
  }


}
