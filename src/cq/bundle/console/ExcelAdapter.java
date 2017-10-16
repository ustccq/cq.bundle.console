package cq.bundle.console;

import java.io.File;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

import com.graphbuilder.struc.LinkedList;


/*

 * Classname
 *
 * Version info
 *
 * Copyright notice
 */
public class ExcelAdapter {
	
	private static ExcelAdapter instance;
	public static synchronized ExcelAdapter getInstance(){
		if (null == instance)
			instance = new ExcelAdapter();
		
		return instance;
	}
	
	private ExcelAdapter() {
		
	}
			    	
	public String convertRowColnumToRefernce(int RowNum, int ColNum){
		
		String sCellReference = null;
		
		//CellReference cellReference  = new CellReference(RowNum, ColNum); 
		sCellReference = CellReference.convertNumToColString(ColNum) + Integer.toString(RowNum);
		
		return sCellReference;		
	}
	
	public int getMAXColNum(Sheet sheet){
						 
		//Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = sheet.iterator();
		int maxCol = 0;
		while (rowIterator.hasNext()){
			//get the current row
			Row row = (Row)rowIterator.next();	
			//Get iterator to all cells of current row
			int currentColNum = row.getLastCellNum();
			maxCol = Math.max(maxCol, currentColNum);
			}
		return maxCol;	
	}
	
	//get a cell's data from the given sheet
	public Object getCellData(Sheet sheet, int ColNum, int RowNum) throws IOException{
		Object returnValue = null;
		Row row = sheet.getRow(RowNum);
		if (row != null){
			//locate the cell from the row
			Cell cell = row.getCell(ColNum);
			
			if (cell!=null) {
				returnValue = this.getCellValue(cell);
			}else{
	        	returnValue = "";
			}
		}else{
			return "";
		}
		return returnValue;
	}
	

	public Boolean printSheet(String fileName, String sheetName) throws IOException, InvalidFormatException{
		
		String cellValue = null;
					
		File file = new File(fileName);
		FileInputStream fin = null;		
		Workbook workbook = null;
		Sheet sheet = null;
		
		if (!file.exists()) {
			throw new FileNotFoundException();
		}else{
			fin = new FileInputStream(fileName);
			workbook = WorkbookFactory.create(fin);
			sheet = workbook.getSheet(sheetName);
		}
				 
		//Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = sheet.iterator();
		 
		while (rowIterator.hasNext()){
			//get the current row
			Row row = (Row)rowIterator.next();	
			//Get iterator to all cells of current row
			Iterator<Cell> cellIterator = row.cellIterator();
			
			while (cellIterator.hasNext()){
				Cell cell = cellIterator.next();
				cellValue = this.getCellValue(cell);
				System.out.print(cellValue + "\t\t");
			}
			System.out.print("\n");
		}
		return true;		
	}
	
	public Sheet getSheet(File excelFile, String sheetName) throws InvalidFormatException, IOException{							
		FileInputStream fin = null;		
		Workbook workbook = null;
		Sheet sheet = null;
		
		if (!excelFile.exists()) {
			throw new FileNotFoundException();
		}else{
			fin = new FileInputStream(excelFile);
			workbook = WorkbookFactory.create(fin);
			sheet = workbook.getSheet(sheetName);
		}		
		return sheet;				 		
	}
	
	public List<Sheet> getSheets(File excelFile) throws InvalidFormatException, IOException{							
		FileInputStream fin = null;		
		Workbook workbook = null;
		
		List<Sheet> sheets = new ArrayList<Sheet>();
//		Map<String, Sheet> sheets = new HashMap<String, Sheet>();
		if (!excelFile.exists()) {
			throw new FileNotFoundException();
		}else{
			fin = new FileInputStream(excelFile);
			workbook = WorkbookFactory.create(fin);
			
			Iterator<Sheet> iter = workbook.sheetIterator();
			while(iter.hasNext()){
				Sheet sheet = iter.next();
				if (null != sheet && sheet.getLastRowNum() > 0)
				sheets.add(sheet);
			}			
		}		
		return sheets;				 		
	}
	
	public Cell getCell(Sheet sheet, String searchCellValue, boolean enableRegex) throws InvalidFormatException, IOException{							
		Iterator<Row> rowIterator = sheet.iterator();
		Cell returnValue = null; 
		outerLoop:
		while (rowIterator.hasNext()){
			Row row = (Row)rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();			
			while (cellIterator.hasNext()){
				Cell cell = cellIterator.next();
				String cellValue = this.getCellValue(cell);
				if(enableRegex){
					if(cellValue.matches(searchCellValue)){
						returnValue = cell;
						break outerLoop;
					}
				}else{
					if(cellValue.equals(searchCellValue)){
						returnValue = cell;
						break outerLoop;
					}
				}
			}
		}
		return returnValue;
	}
	
	public Cell getCell(Sheet sheet ,Cell cell, int rowOffset, int columnOffset){
		Cell returnCell = null;
		CellReference originCell = new CellReference(cell);
		int newCellRow = originCell.getRow() + rowOffset;
		int newCellColumn = originCell.getCol() + columnOffset;
		CellReference ref = new CellReference(newCellRow,newCellColumn);
		 Row r = sheet.getRow(ref.getRow());
		 if (r != null) {
		    Cell c = r.getCell(ref.getCol());
		    returnCell = c;
		 }
		 return returnCell;		 
	}
	
	public String getCellValue(Cell cell){
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
							cellValue = this.getCellValue(cell);
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
	
	//Get the list of the sheets' name and return in the String ArrayList
	public ArrayList<String> getAllSheetNames(String fileName) throws IOException, InvalidFormatException {
		
		File file = new File(fileName);
		FileInputStream fin = null;		
		Workbook workbook = null;
		
		if (!file.exists()) {
			throw new FileNotFoundException();
		}else{
			fin = new FileInputStream(fileName);
			workbook = WorkbookFactory.create(fin);
		}
		
    	ArrayList<String> alReturn = new ArrayList<String>(); 
        // for each sheet in the workbook 
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {    
		    alReturn.add(workbook.getSheetName(i));  
		}
		return alReturn;  
    }
	
	public ArrayList<String> getAllSheetNames(File ExcelFile) throws IOException, InvalidFormatException {
		
		FileInputStream fin = null;		
		Workbook workbook = null;
		
		if (!ExcelFile.exists()) {
			throw new FileNotFoundException();
		}else{
			fin = new FileInputStream(ExcelFile);
			workbook = WorkbookFactory.create(fin);
		}
		
    	ArrayList<String> alReturn = new ArrayList<String>(); 
        // for each sheet in the workbook 
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {    
		    alReturn.add(workbook.getSheetName(i));  
		}
		return alReturn;  
    }
	
	//Check the Sheet exists in the file
	public boolean sheetExists(String fileName, String sheetName) throws IOException, InvalidFormatException{
		
		File file = new File(fileName);
		
		if (!file.exists()) {
			throw new FileNotFoundException();
		}
		
		ArrayList<String> allSheetNames = new ArrayList<String>();
		allSheetNames = this.getAllSheetNames(fileName);
		if (allSheetNames.contains(sheetName)){
			return true;
		}else{
			return false;
		}	
	}
	
	public Row getRow(Workbook workbook, String sheetName, int rownum){
		Sheet sheet = workbook.getSheet(sheetName);
		return sheet.getRow(rownum);
	}
	
	public Cell getCell(Row row, String cellContent){
		Iterator<Cell> cells = row.cellIterator();
		return this.getCell(cells, cellContent);
	}
	
	public Iterator<Cell> getColoumn(Sheet sheet, int columnID) {
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for(Row r : sheet) {
		   Cell c = r.getCell(columnID);
		   cells.add(c);
		}
		return cells.iterator();
	}
	
	public Cell getCell(Iterator<Cell> cells, String cellContent){
		while(cells.hasNext()){
			Cell cell = cells.next();
			if(cell != null){
				if(this.getCellValue(cell).equals(cellContent)){
					return cell;	
				}
			}
		}
		return null;
	}
}


