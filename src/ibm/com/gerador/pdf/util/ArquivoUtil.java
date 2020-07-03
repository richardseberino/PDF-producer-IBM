package ibm.com.gerador.pdf.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe responsável por manter alguns método genéricos para manipulação de arquivos
 * @author richardmarques
 *
 */
public class ArquivoUtil 
{
	/**
	 * método responsável por recuperar o contéudo de um arquivo e retornar ele em bytes
	 * @param file objeto com o arquivo desejado
	 * @return bytes que representa o conteúdo do arquivo
	 * @throws IOException exceção que será gerada caso o arquivo não exista ou o usuáiro tenha problemas de acesso ao arquivo
	 */
	public static byte[] getFileBytes(File file) throws IOException 
	{
    
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try 
		{
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ((read = ios.read(buffer)) != -1)
			{
				ous.write(buffer, 0, read);
			}
		} 
		finally 
		{
			try 
			{
				if (ous != null)
				{
					ous.close();
				}
			} 
			catch (IOException iOException) {}
			
			try 
			{
				if (ios != null)
				{
					ios.close();
				}
			} catch (IOException iOException) {}
		} 
		return ous.toByteArray();
	}
  
  public static byte[] getFileBytes(String filename) throws IOException {
    File file = new File(filename);
    return getFileBytes(file);
  }
  
  
  /**
   * método responsável por savlar algum contéudo em um arquivo físico 
   * @param dir diretório onde o arquivo será salvo
   * @param content conteúdo em bytes desse arquivo (pode ser qualquer coisa, como é em byte, suporta até binários)
   * @param fileName nome do aruqivo que será criado
   * @return
   * @throws IOException
   */
  public static String salvaArquivo(String dir, byte[] content, String fileName) throws IOException 
  {
	  //verifica se o diretório existe
	  File directory = new File(dir);
	  File file = new File(directory, fileName);
	  //cria o diretório caso necessário
	  file.getParentFile().mkdirs();
    
	  //grava o conteúdo em um buffer
	  FileOutputStream fos = new FileOutputStream(file);
	  fos.write(content);
    
	  //salva o arquivo físico (transferindo os dados do buffer para disco)
	  fos.close();
    
    return fileName;
  }  
}
