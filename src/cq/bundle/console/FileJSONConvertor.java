package cq.bundle.console;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
	
	private static JSONObject columnDataContainer2JSON(ColumnDataContainer cdc){
		JSONObject json = new JSONObject();
		
		JSONObject element = new JSONObject(cdc.getCellNameElementTypeMap());
		JSONObject index = new JSONObject(cdc.getCellNameIndexMap());
		JSONObject locatorString = new JSONObject(cdc.getCellNameLocatorStringMap());
		JSONObject locatorType = new JSONObject(cdc.getCellNameLocatorTypeMap());
		
		//TODO
		json.put("element", element);
		json.put("index", index);
		json.put("locator-value", locatorString);
		json.put("locator-type", locatorType);
		
		return json;
	}
	
	public static JSONObject repoTablePart2JSON(File repoFile){
		RepoXMLLoader loader = new RepoXMLLoader(new RepoLoader());
		JSONObject tableDescriptors = new JSONObject();
		try {
			Map<String, TableDataContainer> tableRepo = loader.loadTableXML(repoFile.toURI().toURL());
//			Map<String, JSONObject> middleTableRepo = new HashMap<String, JSONObject>();
			
			Iterator<Entry<String, TableDataContainer>> iter = tableRepo.entrySet().iterator();
			Entry<String, TableDataContainer> entry;
			while (iter.hasNext()){
				entry = iter.next();
				TableDataContainer container = entry.getValue();
				
				JSONObject tableDataObject = new JSONObject();
				
				JSONObject footer = transferFooter(container);
				tableDataObject.put("footer", footer);
				
				JSONObject header = transferHeader(container);
				tableDataObject.put("header", header);
				
				ColumnDataContainer cdc = container.getBodyColumnData();
				JSONObject columns = columnDataContainer2JSON(cdc);
				tableDataObject.put("columns", columns);
				
				tableDataObject.put("name", container.getName());
				tableDataObject.put("locator-type", container.getLocatorType());
				tableDataObject.put("locator-value", container.getLocatorValue());
				tableDataObject.put("row-locator-type", container.getRowLocatorType());
				tableDataObject.put("row-locator-value", container.getRowLocatorValue());

				tableDescriptors.put(entry.getKey(), tableDataObject);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tableDescriptors;
	}

	private static JSONObject transferHeader(TableDataContainer container) {
		ColumnDataContainer cdc = container.getHeaderColumnData();
		JSONObject columns = columnDataContainer2JSON(cdc);			
		String type = container.getHeaderLocatorType();
		String value = container.getHeaderLocatorValue();
		JSONObject header = new JSONObject();
		header.put("locator-type", type);
		header.put("locator-value", value);
		header.put("columns", columns);
		return header;
	}

	private static JSONObject transferFooter(TableDataContainer container) {
		ColumnDataContainer cdc = container.getFooterColumnData();
		JSONObject columns = columnDataContainer2JSON(cdc);
		String type = container.getFooterLocatorType();
		String value = container.getFooterLocatorValue();
		JSONObject footer = new JSONObject();
		footer.put("locator-type", type);
		footer.put("locator-value", value);
		footer.put("columns", columns);
		return footer;
	}
	
	public static JSONObject excel2JSON(File excelFile){
		ExcelAdapter ea = ExcelAdapter.getInstance();
		JSONObject json = new JSONObject();
		try {
			List<Sheet> sheets = ea.getSheets(excelFile);
			
			for(Sheet sheet : sheets){
				json.put(sheet.getSheetName(), excelSheet2JSON(sheet));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public static JSONArray excelSheet2JSON(Sheet sheet){
		JSONArray sheetJSON = new JSONArray();
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
			
		}catch(Exception exception){
			exception.printStackTrace();
		}
		
		return sheetJSON;
	}
	
	public static JSONArray excelSheet2JSON(File excelFile, String shtName){
		
		String sheetName = (null == shtName || shtName.isEmpty()) ? "Instructions" : shtName;
		ExcelAdapter ea = ExcelAdapter.getInstance();
		JSONArray sheetJSON = new JSONArray();
		try {
			Sheet sheet = ea.getSheet(excelFile, sheetName);
			if (null != sheet)
				sheetJSON = excelSheet2JSON(sheet);
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
