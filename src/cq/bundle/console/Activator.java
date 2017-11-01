package cq.bundle.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meowlomo.ci.ems.bundle.interfaces.BaseBundleActivator;
import com.meowlomo.ci.ems.bundle.interfaces.IHttpUtil;
import com.meowlomo.ci.ems.bundle.interfaces.IWebDriver;
import com.meowlomo.ci.ems.bundle.interfaces.IHttpUtil.MethodType;

import cq.bundle.console.FileJSONConvertor;

public class Activator extends BaseBundleActivator  implements CommandProvider  {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
	private static RequestConfig requestConfig = null;
	public Activator() {
		super(logger);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		setContext(context);
		context.registerService(CommandProvider.class.getName(),  this, null);
		
		System.out.println("Hello World in Console!!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		stopMass(context);
		System.out.println("Goodbye World in Console!!");
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "\tsay – say what you input\n";  
	}

	private void innerProcess(ServiceReference<?> serviceRef, String repoPath, String excelPath){
		IWebDriver iwb = (IWebDriver)_context.getService(serviceRef);
		
//		File repoFile = new File("D:\\workspace\\eclipse\\jsonProducer\\repo.xml");
//		File excelFile = new File("D:\\testcase.xlsm");
		
		File repoFile = new File(repoPath);//new File("F:\\_testcase\\repo.xml");
		File excelFile = new File(excelPath);//new File("F:\\_testcase\\testcase.xlsm");
		System.out.println(excelFile.exists());
		
//		JSONArray excelContent = null;
		JSONObject excelContent = null;
		boolean bUseInnerJSONString = true;
		if (bUseInnerJSONString){
//			String sheetContent = "[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\",\"Use Object From Excel sheet\"],{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"demo01\",\"Object\":\"TestCase.Start\",\"Use Object From Excel sheet\":\"String\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"http://123.206.204.103:8080/softslate/\",\"Object\":\"Engine.Browser.Navigate\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"10\",\"Object\":\"Engine.Browser.Wait\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.MSMain.Account\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123302d11w12345678@qmv.com\",\"Object\":\"MeowlomoStore.Store.Register.Email\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123456\",\"Object\":\"MeowlomoStore.Store.Register.password\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123456\",\"Object\":\"MeowlomoStore.Store.Register.RePassword\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.Register.RegisterBtn\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.FirstName\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"feifei\",\"Object\":\"MeowlomoStore.Store.AccountAddress.LastName\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhnaghsi\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Organization\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhangjiazhuang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Address1\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"yuanfang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Address2\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"jingcheng\",\"Object\":\"MeowlomoStore.Store.AccountAddress.City\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"select\",\"Input\":\"Guam\",\"Object\":\"MeowlomoStore.Store.AccountAddress.State\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"wu\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Other\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"1234\",\"Object\":\"MeowlomoStore.Store.AccountAddress.PostalCode\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"select\",\"Input\":\"China\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Country\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"o75561872032\",\"Object\":\"MeowlomoStore.Store.AccountAddress.DaytimePhone\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123219527@qq.com\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Email\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.AccountAddress.AddAddressBtn\",\"Use Object From Excel sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"demo01\",\"Object\":\"TestCase.End\",\"Use Object From Excel sheet\":\"String\"}]";
//			String sheetContent = "";
//			excelContent = new JSONObject();
//			excelContent.put("Instructions", new JSONArray(sheetContent));				
		}
		else{
			
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
			
			excelContent = FileJSONConvertor.excel2JSON(excelFile);
			
//			String sheetName = "Instructions";
//			Sheet sheet = workbook.getSheet(sheetName);
//			JSONArray excelSheetContent = FileJSONConvertor.excelSheet2JSON(sheet);
//			JSONArray excelSheetContentNew = excelContent.getJSONArray("Instructions");
//			if (excelSheetContent.similar(excelSheetContentNew))
//				System.out.println(1);
		}
		
		
		String jsonTask = "";
		if (bUseInnerJSONString){
			jsonTask = 
//"{\"taskData\":{\"id\":160},\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Wait\",\"Input\":\"10\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"76\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"test1@qmv.com\",\"Object\":\"77\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Object\":\"78\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"79\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"chen\",\"Object\":\"80\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"chen\",\"Object\":\"81\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"Meowlomo\",\"Object\":\"82\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"shenzheng\",\"Object\":\"83\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"nanshan\",\"Object\":\"84\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"shenzhen\",\"Object\":\"85\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"ce\",\"Object\":\"87\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"55555\",\"Object\":\"88\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Select\",\"Input\":\"Fiji\",\"Object\":\"89\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"15002090639\",\"Object\":\"90\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"809155@qq.com\",\"Object\":\"91\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"92\"}]},\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"77\":{\"tmpName\":\"Email\",\"77.locator-value\":\"loginUserName\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.type\":\"textbox\"},\"88\":{\"tmpName\":\"BillingPostalCode\",\"88.name\":\"BillingPostalCode\",\"88.type\":\"textbox\",\"88.locator-value\":\"billingPostalCode\",\"88.locator-type\":\"id\"},\"78\":{\"tmpName\":\"Password\",\"78.type\":\"textbox\",\"78.locator-type\":\"id\",\"78.name\":\"Password\",\"78.locator-value\":\"loginDecryptedPassword\"},\"89\":{\"tmpName\":\"BillingCountry\",\"89.name\":\"BillingCountry\",\"89.type\":\"dropdown\",\"89.locator-value\":\"billingCountry\",\"89.locator-type\":\"id\"},\"79\":{\"tmpName\":\"LoginBtn\",\"79.name\":\"LoginBtn\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"79.locator-type\":\"xpath\",\"79.type\":\"button\"},\"102\":{\"102.type\":\"browser\",\"tmpName\":\"Firefox\",\"102.name\":\"Firefox\",\"102.locator-value\":\"\",\"102.locator-type\":\"\"},\"90\":{\"tmpName\":\"BillingPhone1\",\"90.name\":\"BillingPhone1\",\"90.locator-type\":\"id\",\"90.type\":\"textbox\",\"90.locator-value\":\"billingPhone1\"},\"80\":{\"80.name\":\"Firstname\",\"tmpName\":\"Firstname\",\"80.locator-type\":\"id\",\"80.type\":\"textbox\",\"80.locator-value\":\"billingFirstName\"},\"91\":{\"tmpName\":\"BillingEmail1\",\"91.locator-type\":\"id\",\"91.locator-value\":\"billingEmail1\",\"91.name\":\"BillingEmail1\",\"91.type\":\"textbox\"},\"81\":{\"tmpName\":\"Lastname\",\"81.locator-value\":\"billingLastName\",\"81.locator-type\":\"id\",\"81.type\":\"textbox\",\"81.name\":\"Lastname\"},\"92\":{\"tmpName\":\"AddressesBillingButton\",\"92.name\":\"AddressesBillingButton\",\"92.locator-value\":\"accountAddressesBillingButton\",\"92.locator-type\":\"id\",\"92.type\":\"button\"},\"82\":{\"82.type\":\"textbox\",\"tmpName\":\"BillingOrganization\",\"82.locator-type\":\"id\",\"82.name\":\"BillingOrganization\",\"82.locator-value\":\"billingOrganization\"},\"83\":{\"tmpName\":\"BillingAddress1\",\"83.name\":\"BillingAddress1\",\"83.locator-type\":\"id\",\"83.locator-value\":\"billingAddress1\",\"83.type\":\"textbox\"},\"84\":{\"84.locator-value\":\"billingAddress2\",\"tmpName\":\"BillingAddress2\",\"84.name\":\"BillingAddress2\",\"84.type\":\"textbox\",\"84.locator-type\":\"id\"},\"85\":{\"tmpName\":\"BillingCity\",\"85.name\":\"BillingCity\",\"85.type\":\"textbox\",\"85.locator-type\":\"id\",\"85.locator-value\":\"billingCity\"},\"76\":{\"tmpName\":\"Account\",\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"76.locator-type\":\"xpath\"},\"87\":{\"tmpName\":\"BillingOther\",\"87.locator-value\":\"billingOther\",\"87.locator-type\":\"id\",\"87.name\":\"BillingOther\",\"87.type\":\"textbox\"}},\"logFolder\":\"F:\",\"name\":\"AddAddress\",\"firefoxPath\":\"C:/Program Files (x86)/Mozilla Firefox/firefox.exe\",\"parameters\":{\"finishInstructionResult\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}\",\"content\":{\"finished\":true}},\"addInstructionResult\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}/instructionResults\"},\"finishRun\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}\",\"content\":{\"finished\":true}},\"addStepLog\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}/stepLogs\"},\"addRun\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/testCases/{testCaseId}/runs\"}}}";
//"{\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"???????\",\"Options\":\"\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Wait\",\"Input\":\"10\",\"Object\":\"102\"},{\"Comment\":\"your count\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"76\"},{\"Comment\":\"????????\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"test1@qmv.com\",\"Object\":\"77\"},{\"Comment\":\"???????\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Object\":\"78\"},{\"Comment\":\"log in\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"79\"}]},\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"77\":{\"tmpName\":\"Email\",\"77.locator-value\":\"loginUserName\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.type\":\"link\"},\"78\":{\"tmpName\":\"Password\",\"78.type\":\"textbox\",\"78.locator-type\":\"id\",\"78.name\":\"Password\",\"78.locator-value\":\"loginDecryptedPassword\"},\"79\":{\"tmpName\":\"LoginBtn\",\"79.name\":\"LoginBtn\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"79.locator-type\":\"xpath\",\"79.type\":\"button\"},\"102\":{\"102.type\":\"browser\",\"tmpName\":\"Firefox\",\"102.name\":\"Firefox\",\"102.locator-value\":\"\",\"102.locator-type\":\"\"},\"76\":{\"tmpName\":\"Account\",\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"76.locator-type\":\"xpath\"}},\"logFolder\":\"F:\",\"name\":\"MSLogin\",\"taskData\":{\"storages\":null,\"instructions\":[{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304555888,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Navigate\",\"orderIndex\":0,\"comment\":\"???????\",\"id\":161,\"updatedAt\":1505304555888,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"10\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304586174,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Wait\",\"orderIndex\":1,\"comment\":\"\",\"id\":162,\"updatedAt\":1505304586174,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":false,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Account\",\"createdAt\":1505296879837,\"stepOptions\":[],\"targetElementId\":76,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":2,\"comment\":\"your count\",\"id\":122,\"updatedAt\":1505304586177,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='accountLink']/a\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"136\",\"htmlPositionY\":\"91\",\"createdAt\":1505284903251,\"locatorType\":\"xpath\",\"name\":\"Account\",\"comment\":\"????\",\"elementTypeId\":2,\"id\":76,\"updatedAt\":1505385617106}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":false,\"targetApplicationId\":31,\"content\":\"test1@qmv.com\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Email\",\"createdAt\":1505287404370,\"stepOptions\":[],\"targetElementId\":77,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":3,\"comment\":\"????????\",\"id\":107,\"updatedAt\":1505304586178,\"element\":{\"creator\":null,\"locatorValue\":\"loginUserName\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"633\",\"htmlPositionY\":\"54\",\"createdAt\":1505284984112,\"locatorType\":\"id\",\"name\":\"Email\",\"comment\":\"????,\",\"elementTypeId\":2,\"id\":77,\"updatedAt\":1505386315184}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":false,\"targetApplicationId\":31,\"content\":\"123456\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Password\",\"createdAt\":1505287433141,\"stepOptions\":[],\"targetElementId\":78,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":4,\"comment\":\"???????\",\"id\":108,\"updatedAt\":1505304586179,\"element\":{\"creator\":null,\"locatorValue\":\"loginDecryptedPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"629\",\"htmlPositionY\":\"192\",\"createdAt\":1505285054947,\"locatorType\":\"id\",\"name\":\"Password\",\"comment\":\"??\",\"elementTypeId\":null,\"id\":78,\"updatedAt\":1505286662007}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.LoginBtn\",\"createdAt\":1505300146898,\"stepOptions\":[],\"targetElementId\":79,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":5,\"comment\":\"log in\",\"id\":149,\"updatedAt\":1505304586180,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='loginButton']/input\",\"log\":null,\"active\":true,\"type\":\"button\",\"htmlPositionX\":\"636\",\"htmlPositionY\":\"335\",\"createdAt\":1505285112520,\"locatorType\":\"xpath\",\"name\":\"LoginBtn\",\"comment\":\"Log in\",\"elementTypeId\":1,\"id\":79,\"updatedAt\":1505385652393}}],\"creator\":null,\"flag\":null,\"log\":null,\"environments\":[{\"name\":\"environment1\",\"id\":1,\"value\":\"1111\"},{\"name\":\"environment2\",\"id\":2,\"value\":\"1111\"}],\"active\":true,\"message\":null,\"resultStatus\":null,\"createdAt\":1505276957754,\"engines\":[{\"createdAt\":1504870418208,\"log\":null,\"name\":\"AAAA\",\"active\":true,\"comment\":\"AAAAAAAAAAAAAAAAAA\",\"id\":1,\"updatedAt\":1504870418208},{\"createdAt\":1504870424577,\"log\":null,\"name\":\"BBBBBB\",\"active\":true,\"comment\":\"BBBBBBBBBBBBBBBBBBBB\",\"id\":2,\"updatedAt\":1504870424577}],\"name\":\"MSLogin\",\"comment\":\"????\",\"id\":155,\"updatedAt\":1505276957754,\"status\":null},\"firefoxPath\":\"C:/Program Files (x86)/Mozilla Firefox/firefox.exe\",\"parameters\":{\"finishInstructionResult\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}\",\"content\":{\"finished\":true}},\"finishRun\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}\",\"content\":{\"finished\":true}},\"addStepLog\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}/stepLogs\"},\"addInstructionResult\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}/instructionResults\"},\"addRun\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/testCases/{testCaseId}/runs\"}}}";
				"{\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Wait\",\"Input\":\"10\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"76\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"809155@qq.com\",\"Object\":\"77\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Object\":\"78\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"79\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"96\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"809155@qq.com\",\"Object\":\"298\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Modify\",\"Input\":\"123456\",\"Object\":\"299\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Modify\",\"Input\":\"123456\",\"Object\":\"300\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"301\"}]},\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"77\":{\"tmpName\":\"Email\",\"77.locator-value\":\"loginUserName\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.type\":\"textbox\"},\"78\":{\"tmpName\":\"Password\",\"78.type\":\"textbox\",\"78.locator-type\":\"id\",\"78.name\":\"Password\",\"78.locator-value\":\"loginDecryptedPassword\"},\"298\":{\"tmpName\":\"ChangeEmail\",\"298.name\":\"ChangeEmail\",\"298.locator-type\":\"id\",\"298.locator-value\":\"loginUserName\",\"298.type\":\"textbox\"},\"79\":{\"tmpName\":\"LoginBtn\",\"79.name\":\"LoginBtn\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"79.locator-type\":\"xpath\",\"79.type\":\"button\"},\"299\":{\"tmpName\":\"ChangePwd\",\"299.type\":\"textbox\",\"299.locator-type\":\"id\",\"299.name\":\"ChangePwd\",\"299.locator-value\":\"loginDecryptedPassword\"},\"102\":{\"102.type\":\"browser\",\"tmpName\":\"Firefox\",\"102.name\":\"Firefox\",\"102.locator-value\":\"\",\"102.locator-type\":\"\"},\"300\":{\"tmpName\":\"ComfirePwd\",\"300.locator-type\":\"id\",\"300.type\":\"textbox\",\"300.locator-value\":\"confirmPassword\",\"300.name\":\"ComfirePwd\"},\"301\":{\"tmpName\":\"UpdateBtn\",\"301.name\":\"UpdateBtn\",\"301.type\":\"button\",\"301.locator-type\":\"xpath\",\"301.locator-value\":\".//*[@id='accountPasswordButton']/input\"},\"96\":{\"tmpName\":\"Reset_pwd\",\"96.locator-type\":\"xpath\",\"96.name\":\"Reset_pwd\",\"96.locator-value\":\"html/body/main/ul/li[2]/a\",\"96.type\":\"link\"},\"76\":{\"tmpName\":\"Account\",\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"76.locator-type\":\"xpath\"}},\"logFolder\":\"F:\",\"name\":\"ChangePwd\",\"taskData\":{\"storages\":null,\"instructions\":[{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304686829,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Navigate\",\"orderIndex\":0,\"comment\":\"\",\"id\":165,\"updatedAt\":1505304686829,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"10\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304705374,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Wait\",\"orderIndex\":1,\"comment\":\"\",\"id\":166,\"updatedAt\":1505304705374,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Account\",\"createdAt\":1505302467438,\"stepOptions\":[],\"targetElementId\":76,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":2,\"comment\":\"\",\"id\":150,\"updatedAt\":1507627129732,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='accountLink']/a\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"136\",\"htmlPositionY\":\"91\",\"createdAt\":1505284903251,\"locatorType\":\"xpath\",\"name\":\"Account\",\"comment\":\"????\",\"elementTypeId\":2,\"id\":76,\"updatedAt\":1505385617106}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"809155@qq.com\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Email\",\"createdAt\":1505302502339,\"stepOptions\":[],\"targetElementId\":77,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":3,\"comment\":\"\",\"id\":151,\"updatedAt\":1507627129733,\"element\":{\"creator\":null,\"locatorValue\":\"loginUserName\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"633\",\"htmlPositionY\":\"54\",\"createdAt\":1505284984112,\"locatorType\":\"id\",\"name\":\"Email\",\"comment\":\"????,\",\"elementTypeId\":null,\"id\":77,\"updatedAt\":1505404470625}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"123456\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Password\",\"createdAt\":1505302523762,\"stepOptions\":[],\"targetElementId\":78,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":4,\"comment\":\"\",\"id\":152,\"updatedAt\":1507627129734,\"element\":{\"creator\":null,\"locatorValue\":\"loginDecryptedPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"629\",\"htmlPositionY\":\"192\",\"createdAt\":1505285054947,\"locatorType\":\"id\",\"name\":\"Password\",\"comment\":\"??\",\"elementTypeId\":null,\"id\":78,\"updatedAt\":1505286662007}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.LoginBtn\",\"createdAt\":1505302572021,\"stepOptions\":[],\"targetElementId\":79,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":5,\"comment\":\"\",\"id\":153,\"updatedAt\":1507627129734,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='loginButton']/input\",\"log\":null,\"active\":true,\"type\":\"button\",\"htmlPositionX\":\"636\",\"htmlPositionY\":\"335\",\"createdAt\":1505285112520,\"locatorType\":\"xpath\",\"name\":\"LoginBtn\",\"comment\":\"Log in\",\"elementTypeId\":1,\"id\":79,\"updatedAt\":1505385652393}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"yes\",\"target\":\"MeowlomoStore.PwdReset.PwdReset.Reset_pwd\",\"createdAt\":1505302635249,\"stepOptions\":[],\"targetElementId\":96,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":6,\"comment\":\"\",\"id\":155,\"updatedAt\":1507627129735,\"element\":{\"creator\":null,\"locatorValue\":\"html/body/main/ul/li[2]/a\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"60\",\"htmlPositionY\":\"34\",\"createdAt\":1505301904210,\"locatorType\":\"xpath\",\"name\":\"Reset_pwd\",\"comment\":\"Change Password\",\"elementTypeId\":2,\"id\":96,\"updatedAt\":1505301907829}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"809155@qq.com\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.ChangeEmail\",\"createdAt\":1507627129729,\"stepOptions\":[],\"targetElementId\":298,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":7,\"comment\":\"\",\"id\":246,\"updatedAt\":1507627129729,\"element\":{\"creator\":null,\"locatorValue\":\"loginUserName\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"371\",\"htmlPositionY\":\"107\",\"createdAt\":1507626980794,\"locatorType\":\"id\",\"name\":\"ChangeEmail\",\"comment\":\"\",\"elementTypeId\":null,\"id\":298,\"updatedAt\":1507627048618}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"123456\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.ChangePwd\",\"createdAt\":1507627155515,\"stepOptions\":[],\"targetElementId\":299,\"testCaseOptions\":[],\"action\":\"Modify\",\"orderIndex\":8,\"comment\":\"\",\"id\":247,\"updatedAt\":1507627155515,\"element\":{\"creator\":null,\"locatorValue\":\"loginDecryptedPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"361\",\"htmlPositionY\":\"10\",\"createdAt\":1507627010876,\"locatorType\":\"id\",\"name\":\"ChangePwd\",\"comment\":\"\",\"elementTypeId\":null,\"id\":299,\"updatedAt\":1507627012688}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"123456\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.ComfirePwd\",\"createdAt\":1507627177270,\"stepOptions\":[],\"targetElementId\":300,\"testCaseOptions\":[],\"action\":\"Modify\",\"orderIndex\":9,\"comment\":\"\",\"id\":248,\"updatedAt\":1507627177270,\"element\":{\"creator\":null,\"locatorValue\":\"confirmPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"354\",\"htmlPositionY\":\"189\",\"createdAt\":1507627042214,\"locatorType\":\"id\",\"name\":\"ComfirePwd\",\"comment\":\"\",\"elementTypeId\":null,\"id\":300,\"updatedAt\":1507627046670}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"yes\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.UpdateBtn\",\"createdAt\":1507627191314,\"stepOptions\":[],\"targetElementId\":301,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":10,\"comment\":\"\",\"id\":249,\"updatedAt\":1507627191314,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='accountPasswordButton']/input\",\"log\":null,\"active\":true,\"type\":\"button\",\"htmlPositionX\":\"356\",\"htmlPositionY\":\"294\",\"createdAt\":1507627079510,\"locatorType\":\"xpath\",\"name\":\"UpdateBtn\",\"comment\":\"\",\"elementTypeId\":1,\"id\":301,\"updatedAt\":1507627082340}}],\"creator\":null,\"flag\":null,\"log\":null,\"environments\":[],\"active\":true,\"message\":null,\"resultStatus\":null,\"createdAt\":1505302429449,\"engines\":[],\"name\":\"ChangePwd\",\"comment\":\"????\",\"id\":168,\"updatedAt\":1505302429449,\"status\":null},\"firefoxPath\":\"C:/Program Files (x86)/Mozilla Firefox/firefox.exe\",\"parameters\":{\"finishInstructionResult\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}\",\"content\":{\"finished\":true}},\"finishRun\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}\",\"content\":{\"finished\":true}},\"addStepLog\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}/stepLogs\"},\"addInstructionResult\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}/instructionResults\"},\"addRun\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/testCases/{testCaseId}/runs\"}}}";
		}else{
		
	//		JSONArray excelContent = FileJSONConvertor.excel2JSON(excelFile);
			JSONObject jsonParams = new JSONObject();
			jsonParams.put("firefoxPath", "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
			jsonParams.put("geckodriverPath", "D:\\workspace\\meowlomo\\selfgen_web_ui\\src\\recources\\driver\\geckodriver-windows-64.exe");
			
			JSONObject xml = FileJSONConvertor.repo2JSON(repoFile);
			xml.put("tables", FileJSONConvertor.repoTablePart2JSON(repoFile));
			
			jsonParams.put("repositoryXML", xml);
			jsonParams.put("logFolder", "C:\\Users\\meteor\\Desktop\\bundle_log");
			jsonParams.put("workbook", excelContent);
			jsonTask = jsonParams.toString();
			JSONArray inString = excelContent.getJSONArray("Instructions");
			String insss = inString.toString();
		}
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
	
	public void _utf(CommandInterpreter ci){
//		String param = ci.nextArgument();
//		System.out.println(param);
		
		IHttpUtil http = null;
		if (null == http){
			try {
				BaseBundleActivator bba = Activator.getBundleActivator("com.meowlomo.ci.ems.bundle.curl");
				//TODO
				if (null != bba){
					http = bba.getServiceObject(IHttpUtil.class);				
				}
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		if (null != http){
			String url = "http://10.0.100.185:8080/EMS/rest/agent/llog";
			String params = "{\"中国人\":\"美国人\"}";
			
			IHttpUtil.MethodType methodType = IHttpUtil.MethodType.POST;
			http.request(url, params, methodType);
		}
	}

	public void _run(CommandInterpreter ci){
		ServiceReference<?> serviceRef = _context.getServiceReference(IWebDriver.class.getName());  
		 if (null == serviceRef)
	           System.out.println("null web driver object");
		 else{
			 innerProcess(serviceRef, "F:\\_testcase\\repoTable.xml", "F:\\_testcase\\testcaseTable.xlsm");
		 }
	}
	
	public void _go(CommandInterpreter  ci){
//		String command = ci.nextArgument();
		 ServiceReference<?> serviceRef = _context.getServiceReference(IWebDriver.class.getName());
		 if (null == serviceRef)
	           System.out.println("null web driver object");
		 else{
			 innerProcess(serviceRef, "F:\\_testcase\\repo1.xml", "F:\\_testcase\\testcase1.xlsm");
		 }
	}
	
	public EventAdmin getEventAdmin(){
		if (null == _context)
			return null;
		
		ServiceReference<EventAdmin> ref = _context.getServiceReference(EventAdmin.class);
		if (null == ref)
			return null;
		
		EventAdmin eventAdmin = _context.getService(ref);
		return eventAdmin;
	}
}
