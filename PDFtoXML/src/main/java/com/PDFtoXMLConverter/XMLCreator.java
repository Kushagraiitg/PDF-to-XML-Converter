package com.PDFtoXMLConverter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 * This class creates an XML File of name same as the original PDF <br>
 * and the then date and time and adds the tabular and unstructured<br>
 * component of PDF pagewise to the XML File.
 * 
 * @author KUSHAGRA
 * 
 */
public class XMLCreator {
	
	private String name;													// holds filename for the XML Generated
	private int tableId;													//holds table id for all tables in PDF 
	
	/**
	 * 
	 * This constructor serves to generate XML file <br>
	 * with filename of the required PDF.<br>
	 *
	 * 
	 * @author KUSHAGRA
	 * @param pagesNos ArrayList of pages
	 * @param String fileName of entered PDF
	 * 
	 * 
	 */
	public XMLCreator(ArrayList<Integer> pageNos,String fileName) {	
		tableId=1;
		initialize(pageNos,fileName);
		
	}
	
	
	/**
	 * 
	 * This method serves to generate XML file <br>
	 * with filename of the required PDF.<br>
	 *
	 * 
	 * @author KUSHAGRA
	 * @param pagesNos ArrayList of pages
	 * @param String fileName of entered PDF
	 * 
	 * 
	 */
	public void initialize(ArrayList<Integer> pageNos,String fileName) {
		// Upon Instantiation, a file "CreatedXML.xml" is added to the folder
				try {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.newDocument();
					
					// add elements to Document
					Element rootElement = doc.createElement("DetailFromInvoice");

					// append root element to document
					doc.appendChild(rootElement);
					
					// Required page tags are added

					for(int pageCount=0;pageCount<pageNos.size();pageCount++) {
						Node x = doc.getElementsByTagName("DetailFromInvoice").item(0);

						Element page = doc.createElement("page");
						page.setAttribute("ID", Integer.toString(pageNos.get(pageCount)));
						x.appendChild(page);
					}
					

					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					DOMSource source = new DOMSource(doc);
					
																											//giving name for the XML
					String fileN=fileName;
					fileN=fileN.substring(fileN.lastIndexOf('\\')+1);
					if(fileN.length()==4) {
						fileN="sample";
					}
					
					else {
						fileN=fileN.substring(0,fileN.lastIndexOf(".pdf"));
					}
					fileN=fileN.replaceAll("[^A-Za-z0-9]", "-");
					
																											
					LocalDateTime myDateObj = LocalDateTime.now();
					DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
					name=fileN+myDateObj.format(myFormatObj)+".xml";
					StreamResult file = new StreamResult(new File(name));
					
																											// write data

					transformer.transform(source, file);
					

				} catch (Exception e) {
					
				}
	}
	
	/**
	 * 
	 * This method serves to add unstructured data for the specified page<br>
	 * to the generated XML file <br>
	 *
	 *
	 * 
	 * @author KUSHAGRA
	 * @param arr stores key-values of the unstructured text
	 * @param pages ArrayList of pages
	 * @param pageId for the page to which data is to be added
	 * @throws Exception
	 * 
	 */
	public void addData(HashMap<String, String> arr, ArrayList<Integer> pages,int pageId) throws Exception {

		Logger logger = CacheManager.getLogger();
		String filePath = name;
		File xmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try {
			dBuilder = dbFactory.newDocumentBuilder();

																								// parse xml file and load into document
			Document doc = dBuilder.parse(xmlFile);
			
			doc.getDocumentElement().normalize();

																								// adding data Elements to the page tag
			int index=pages.indexOf(pageId);
			Node page=doc.getElementsByTagName("page").item(index);
			
																								// loop for each data Element
			
			for (Map.Entry<String, String> entry : arr.entrySet()) {

				String id = entry.getKey();
				String value = entry.getValue();
				String tag = "";
				
				
				
				if(id=="") {
					id='_'+id;
				}
				
				
				
				if (!((id.charAt(0) >64  && id.charAt(0) < 91)||(id.charAt(0) >96  && id.charAt(0) < 123))) {
					tag = '-' + id;
				} else {
					tag = id;
				}
				
				String text = tag.replaceAll("[^a-zA-Z0-9_]", "-");
				tag = text;
				if(tag.charAt(0)=='-') {
					tag='_'+tag;
				}
				Element data;
				data = doc.createElement(tag);
				if(tag.length()>4) {
					if("Text".equals(tag.substring(0,4)) ) {
				
					
					data = doc.createElement(tag.substring(0,4));
					}	
					
				}
						data.appendChild(doc.createTextNode(value));
						page.appendChild(data);
						
			}

																								// write the updated document to file
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(name));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			

		} catch (SAXException  e1) {
			logger.error("Error in XML parsing!");
		}
		 catch ( ParserConfigurationException e2) {
			 logger.error("Error in XML parsing!");			}
		 catch ( IOException  e3) {
			 logger.error("Error in reading PDF!");			}
		 catch ( TransformerException e4) {
			 logger.error("Error in transforming XML!");			}
	}
	
	
	
	/**
	 * 
	 * This method serves to add table for the specified page<br>
	 * to the generated XML file <br>
	 *
	 *
	 * 
	 * @author KUSHAGRA
	 * @param arr stores list of rows of the table
	 * @param pages ArrayList of pages
	 * @param pageId for the page to which data is to be added
	 * @throws Exception
	 * 
	 */
	public void addTable(List<HashMap<String, String>> arr, ArrayList<Integer> pages,int pageId) throws Exception {
		
		Logger logger = CacheManager.getLogger();
		
		String filePath = name;
		File xmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();

																							// parse xml file and load into document
			Document doc = dBuilder.parse(xmlFile);

			doc.getDocumentElement().normalize();

																							// adding row elements to the table tags
			
			
			int index=pages.indexOf(pageId);
			Node page=doc.getElementsByTagName("page").item(index);
			
			Element table = doc.createElement("table");
			table.setAttribute("ID", "t"+Integer.toString(tableId));
			tableId++;
			page.appendChild(table);
			
																							// loop for each row
			
			for (int row = 0; row < arr.size(); row++) {
				Element rowi = doc.createElement("row");
				table.appendChild(rowi);
				for (Map.Entry<String, String> entry : arr.get(row).entrySet()) {

					String id = entry.getKey();
					String value = entry.getValue();
					String tag = "";
					if (id.charAt(0) < 58 && id.charAt(0) > 47) {
						tag = '-' + id;
					} else {
						tag = id;
					}
					String text = tag.replaceAll("[^a-zA-Z0-9]", "-");
					tag = text;
					Element rowcol = doc.createElement(tag);
					rowcol.appendChild(doc.createTextNode(value));
					rowi.appendChild(rowcol);

				}
			}

																							// write the updated document to file

			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(name));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			

		} catch (SAXException  e1) {
			logger.error("Error in XML parsing!");
		}
		 catch ( ParserConfigurationException e2) {
			 logger.error("Error in XML parsing!");			}
		 catch ( IOException  e3) {
			 logger.error("Error in reading PDF!");			}
		 catch ( TransformerException e4) {
			 logger.error("Error in transforming XML!");			}
	}

}
