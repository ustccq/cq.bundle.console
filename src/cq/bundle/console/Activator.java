package cq.bundle.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.meowlomo.ci.ems.bundle.interfaces.IWebDriver;
import cq.bundle.console.FileJSONConvertor;

public class Activator implements CommandProvider, BundleActivator {

	private static BundleContext context = null;
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		context.registerService(CommandProvider.class.getName(),  this, null);
		
		System.out.println("Hello World in Console!!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
		System.out.println("Goodbye World in Console!!");
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "\tsay – say what you input\n";  
	}

	public void _go(CommandInterpreter  ci){
//		String command = ci.nextArgument();
		 ServiceReference<?> serviceRef = Activator.context.getServiceReference(IWebDriver.class.getName());  
		 if (null == serviceRef)
	           System.out.println("null web driver object");
		 else{
			IWebDriver iwb = (IWebDriver)context.getService(serviceRef);
			
			File repoFile = new File("D:\\workspace\\eclipse\\jsonProducer\\repo.xml");
			File excelFile = new File("D:\\testcase.xlsm");
			System.out.println(excelFile.exists());
			
			JSONArray excelContent = null;
			boolean bUseInnerJSONString = false;
			if (bUseInnerJSONString){
				String sheetContent = "[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\",\"Use Object From Excel sheet\"],{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"demo01\",\"Object\":\"TestCase.Start\",\"Use Object From Excel sheet\":\"String\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"http://123.206.204.103:8080/softslate/\",\"Object\":\"Engine.Browser.Navigate\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"10\",\"Object\":\"Engine.Browser.Wait\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.MSMain.Account\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123302d11w123456@qmv.com\",\"Object\":\"MeowlomoStore.Store.Register.Email\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123456\",\"Object\":\"MeowlomoStore.Store.Register.password\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123456\",\"Object\":\"MeowlomoStore.Store.Register.RePassword\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.Register.RegisterBtn\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.FirstName\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"feifei\",\"Object\":\"MeowlomoStore.Store.AccountAddress.LastName\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhnaghsi\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Organization\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhangjiazhuang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Address1\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"yuanfang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Address2\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"jingcheng\",\"Object\":\"MeowlomoStore.Store.AccountAddress.City\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"select\",\"Input\":\"Guam\",\"Object\":\"MeowlomoStore.Store.AccountAddress.State\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"wu\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Other\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"1234\",\"Object\":\"MeowlomoStore.Store.AccountAddress.PostalCode\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"select\",\"Input\":\"China\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Country\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"o75561872032\",\"Object\":\"MeowlomoStore.Store.AccountAddress.DaytimePhone\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123219527@qq.com\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Email\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.AccountAddress.AddAddressBtn\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"demo01\",\"Object\":\"TestCase.End\",\"Use Object From Excel sheet\":\"String\"}]";
				excelContent = new JSONArray(sheetContent);				
			}
			else{
				String sheetName = "Instructions";
				FileInputStream fin = null;
				Workbook workbook = null;
				try {
					fin = new FileInputStream(excelFile);
					workbook = WorkbookFactory.create(fin);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
					e.printStackTrace();
				}
				
				Sheet sheet = workbook.getSheet(sheetName);
				excelContent = FileJSONConvertor.sheet2JSON(sheet);
			}
			
//			JSONArray excelContent = FileJSONConvertor.excel2JSON(excelFile);
			JSONObject jsonParams = new JSONObject();
			jsonParams.put("firefoxPath", "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
			jsonParams.put("geckodriverPath", "D:\\workspace\\meowlomo\\selfgen_web_ui\\src\\recources\\driver\\geckodriver-windows-64.exe");
			jsonParams.put("repositoryXML", FileJSONConvertor.repo2JSON(repoFile));
			jsonParams.put("logFolder", "C:\\Users\\meteor\\Desktop\\bundle_log");
			jsonParams.put("workbook", excelContent);
			String jsonTask = jsonParams.toString();
			
			boolean bUseEvent = true;
			if (bUseEvent){
				Dictionary<String, Object> msg = new Hashtable<String, Object>();
				msg.put("params", jsonTask);
				EventAdmin eventAdmin = getEventAdmin();
				Event reportGeneratedEvent = new Event("com/meowlomo/bundle/webdriver/dotest", msg);
				eventAdmin.postEvent(reportGeneratedEvent);
			}
			else{
				boolean bTestResult = iwb.doTestProcess(jsonTask);
				System.out.println(bTestResult);
			}
		 }
	}
	
	public EventAdmin getEventAdmin(){
		if (null == Activator.context)
			return null;
		
		ServiceReference<EventAdmin> ref = Activator.context.getServiceReference(EventAdmin.class);
		if (null == ref)
			return null;
		
		EventAdmin eventAdmin = Activator.context.getService(ref);
		return eventAdmin;
	}
}
