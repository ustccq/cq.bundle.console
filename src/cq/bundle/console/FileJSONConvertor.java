package cq.bundle.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileJSONConvertor {
	
    private static Map<String,String> processElementAttributes(String name, String upperName, Element element){
    	Map<String,String> collectionMap = new HashMap<String,String>();
    	List<Attribute> attributes = element.attributes();
    	for(Attribute attr : attributes){
    		String key = (upperName.isEmpty() ? "" : upperName+".")+name+"."+attr.getName();
    		String value = attr.getValue();
    		System.out.println("key=["+key+"] value=["+value+"]");
    		collectionMap.put(key.toLowerCase(),value);
    	}
    	return collectionMap;
    }
	
	private static Map<String,String> processElementMap(String upperName, Element element){
    	Map<String,String> collectionMap = null;
    	String name = element.attributeValue("name");
    	Iterator<Element> subElements = element.elementIterator();
    	//case 1, has name and ended
    	//case 3, no name and ended
    	if (!subElements.hasNext()){
    		System.out.println((null == name ? "no" : "has") + " name and ended");
    		collectionMap = processElementAttributes(null == name ? element.getName() : name, upperName, element);
    	}
    	//case 2, has name but not ended
    	//case 4, no name but not ended
    	else if (subElements.hasNext()){
    		System.out.println((null == name ? "no" : "has") + " name but not ended");
    		collectionMap = processElementAttributes(null == name ? element.getName() : name, upperName, element);
        	//process the child node and add the child node's name to the upper name
        	while(subElements.hasNext()){
        		Element subElement = subElements.next();
        		collectionMap.putAll(processElementMap((upperName.isEmpty() ? "" : upperName+".")+name, subElement));
        	}
    	}
    	//never here
    	else{
    		System.out.println("else");
    		collectionMap = new HashMap<String,String>();
    		while(subElements.hasNext()){
    			System.out.println("#");
    			collectionMap.putAll(processElementMap(element.getName(), subElements.next()));
    		}
    	}
    	return collectionMap;
    }
	
	private static Sheet getSheet(File excelFile, String sheetName) throws InvalidFormatException, IOException{							
		FileInputStream fin = null;		
		Workbook workbook = null;
		Sheet sheet = null;
		
		if (!excelFile.exists()) {
			throw new FileNotFoundException();
		}else{
			try{
				fin = new FileInputStream(excelFile);
				workbook = WorkbookFactory.create(fin);
				sheet = workbook.getSheet(sheetName);
			}catch(IOException e){
				e.printStackTrace();
			}catch(InvalidFormatException e){
				e.printStackTrace();
			}catch(EncryptedDocumentException e){
				e.printStackTrace();
			}
		}
		return sheet;
	}
	
	public static JSONObject repo2JSON(File repoFile){
		
		Map<String,Map<String,String>> repoMap = new HashMap<String,Map<String,String>>();
		try {
			SAXReader reader = new SAXReader();
			URL url = repoFile.toURI().toURL();

			Document doc = reader.read(url);
	        Iterator<Element> projects = doc.getRootElement().element("projects").elementIterator("project");
			 while(projects.hasNext()){
				 Element project = projects.next();
				 String projectName = project.attributeValue("name");
				 Iterator<Element> applications = project.element("applications").elementIterator("application");
				 while(applications.hasNext()){
					 Element application = applications.next();
					 String applicationName = application.attributeValue("name");
					 Iterator<Element> sections = application.element("sections").elementIterator("section");
					 while(sections.hasNext()){
						 Element section = sections.next();
						 String sectionName = section.attributeValue("name");
						 //the key ends to the section. the value is the content under the key
						 Iterator<Element> elements = section.elementIterator();
						 while(elements.hasNext()){
							 Element element = elements.next();
							 Map<String,String> elementContentMap = new HashMap<String,String>();
							 String elementName = element.attributeValue("name");
							 String key = (projectName+"."+applicationName+"."+sectionName+"."+elementName).toLowerCase();
							 elementContentMap.putAll(processElementMap("", element));
							 repoMap.put(key, elementContentMap);
						 }						 
					 }
				 }
			 }
			
		} catch (DocumentException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONObject(repoMap);
	}
	
	public static JSONArray sheet2JSON(Sheet sheet){
		JSONArray sheetJSON = new JSONArray();
		if (null == sheet)
			return sheetJSON;
		
		try {
			Row headerRow = sheet.getRow(0);
			int nFirstCol = headerRow.getFirstCellNum();
			int nLastCol = headerRow.getLastCellNum();
			Map<Integer, String> keyMap = new HashMap<Integer, String>();
			
			JSONArray headerJSON = new JSONArray(); 
			for(int j = nFirstCol; j < nLastCol; ++j){
				Cell cell = headerRow.getCell(j);
				String headerCellContent = getCellValue(cell);
				headerJSON.put(headerCellContent);
				keyMap.put(j, headerCellContent);
			}
			sheetJSON.put(headerJSON);

			int n = 0;
			for(Row r : sheet){
				++n;
				if (1 == n)
					continue;
				
				JSONObject rowJSON = new JSONObject();
				for(int i = nFirstCol; i < nLastCol; ++i){
					Cell c = r.getCell(i);
					if (null == c){
						rowJSON.put(keyMap.get(i), "");
					}
					else{
						String cellContent = getCellValue(c);
						rowJSON.put(keyMap.get(i), cellContent);
					}
				}
				sheetJSON.put(rowJSON);
			}
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return sheetJSON;
	}
	
	public static JSONArray excel2JSON(File excelFile){
		String sheetName = "Instructions";
		JSONArray sheetJSON = new JSONArray();
		try {
			Sheet sheet = getSheet(excelFile, sheetName);
			Row headerRow = sheet.getRow(0);

			int nFirstCol = headerRow.getFirstCellNum();
			int nLastCol = headerRow.getLastCellNum();
			Map<Integer, String> keyMap = new HashMap<Integer, String>();
			
			JSONArray headerJSON = new JSONArray(); 
			for(int j = nFirstCol; j < nLastCol; ++j){
				Cell cell = headerRow.getCell(j);
				String headerCellContent = getCellValue(cell);
				headerJSON.put(headerCellContent);
				keyMap.put(j, headerCellContent);
			}
			sheetJSON.put(headerJSON);

			int n = 0;
			for(Row r : sheet){
				++n;
				if (1 == n)
					continue;
				
				JSONObject rowJSON = new JSONObject();
				for(int i = nFirstCol; i < nLastCol; ++i){
					Cell c = r.getCell(i);
					if (null == c){
						rowJSON.put(keyMap.get(i), "");
					}
					else{
						String cellContent = getCellValue(c);
						rowJSON.put(keyMap.get(i), cellContent);
					}
				}
				sheetJSON.put(rowJSON);
			}
			
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sheetJSON;
	}
	
	private static String getCellValue(Cell cell){
		String cellValue = "";
		if(cell != null){
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BOOLEAN:
					if( cell.getBooleanCellValue()){
						cellValue = "TRUE";
					} else {
						cellValue = "FALSE";
					}							
					break;
				case Cell.CELL_TYPE_NUMERIC:
					if(DateUtil.isCellDateFormatted(cell)) {
						double dv = cell.getNumericCellValue();
						if(DateUtil.isValidExcelDate(dv)) {
							Date cellDate = DateUtil.getJavaDate(dv);
							SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
							String sCellDate = dateFormatter.format(cellDate);
							cell.setCellType(Cell.CELL_TYPE_STRING);
							cell.setCellValue(sCellDate);	
							cellValue = getCellValue(cell);
						}
					}else{
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cellValue = cell.getStringCellValue();
						//cellValue = Double.toString(cell.getNumericCellValue());
					}
					break;
				case  Cell.CELL_TYPE_STRING:
					cellValue = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_BLANK:
					break;
				case Cell.CELL_TYPE_ERROR:
					cellValue =  Byte.toString(cell.getErrorCellValue());
					break;
			}
			return cellValue;
		}else{
			return null;
		}

	}
}
