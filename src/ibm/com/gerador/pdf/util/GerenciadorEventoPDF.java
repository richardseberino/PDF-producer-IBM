package ibm.com.gerador.pdf.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementHandler;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.TagProcessorFactory;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.parser.XMLParserListener;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.ElementHandlerPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Classe responsável por suportar a criação de novos documentos PDF usando um template 
 * @author richardmarques
 *
 */
public class GerenciadorEventoPDF extends PdfPageEventHelper {
  protected PdfReader reader;
  protected Rectangle pageSize;
  protected Rectangle body;
  protected float mLeft;
  protected float mRight;
  protected float mTop;
  protected float mBottom;
  protected BaseFont basefont;
  protected Font font;
  protected Font font2;
  protected PdfTemplate background;
  
  /**
   * Construtor da classe que espera o template PDF que será usado (em bytes)
   * @param template
   * @throws IOException
   * @throws DocumentException
   */
  public GerenciadorEventoPDF(byte[] template) throws IOException, DocumentException 
  {
    InputStream isTemplate = new ByteArrayInputStream(template);
    this.reader = new PdfReader(template);
    AcroFields fields = this.reader.getAcroFields();
    this.pageSize = this.reader.getPageSize(1);
    this.body = ((AcroFields.FieldPosition)fields.getFieldPositions("body").get(0)).position;
    this.mLeft = this.body.getLeft() - this.pageSize.getLeft();
    this.mRight = this.pageSize.getRight() - this.body.getRight();
    this.mTop = this.pageSize.getTop() - this.body.getTop();
    this.mBottom = this.body.getBottom() - this.pageSize.getBottom();
    this.basefont = BaseFont.createFont();
    this.font = new Font(this.basefont, 12.0F);
  }
  
  public Rectangle getPageSize() {
    return this.pageSize;
  }
  
  public float getmLeft() {
    return this.mLeft;
  }
  
  public float getmRight() {
    return this.mRight;
  }
  
  public float getmTop() {
    return this.mTop;
  }
  
  public float getmBottom() {
    return this.mBottom;
  }
  
  public Rectangle getBody() {
    return this.body;
  }
  
  public void onOpenDocument(PdfWriter writer, Document document) {
    this.background = (PdfTemplate)writer.getImportedPage(this.reader, 1);
  }
  
  public void onEndPage(PdfWriter writer, Document document) {
    PdfContentByte canvas = writer.getDirectContentUnder();
    canvas.addTemplate(this.background, 0.0F, 0.0F);
   

  }
  
  public void onCloseDocument(PdfWriter writer, Document document) 
  {
	  
  }
  
/**
 * responsável por converter o conteúdo (em bytes) formatado em HTML em elementos que podem ser incluídos em um PDF
 * @param content conteúdo em HTML (em bytes)
 * @param style CSS que define o formato (em bytes)
 * @param tagProcessors
 * @return
 * @throws IOException
 */
  public static ElementList parseHtml(byte[] content, byte[] style, TagProcessorFactory tagProcessors) throws IOException 
  {
    InputStream isStyle = new ByteArrayInputStream(style);
    StyleAttrCSSResolver styleAttrCSSResolver = new StyleAttrCSSResolver();
    CssFile cssFile = XMLWorkerHelper.getCSS(isStyle);
    styleAttrCSSResolver.addCss(cssFile);
    HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
    htmlContext.setTagFactory(tagProcessors);
    htmlContext.autoBookmark(false);
    ElementList elements = new ElementList();
    ElementHandlerPipeline end = new ElementHandlerPipeline((ElementHandler)elements, null);
    HtmlPipeline html = new HtmlPipeline(htmlContext, (Pipeline)end);
    CssResolverPipeline css = new CssResolverPipeline((CSSResolver)styleAttrCSSResolver, (Pipeline)html);
    XMLWorker worker = new XMLWorker((Pipeline)css, true);
    XMLParser p = new XMLParser((XMLParserListener)worker);
    InputStream isContent = new ByteArrayInputStream(content);
    p.parse(isContent, Charset.forName("cp1252"));
    return elements;
  }
}
