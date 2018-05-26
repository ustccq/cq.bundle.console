package cq.bundle.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import javax.sql.DataSource;

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
import org.apache.poi.util.TempFile;
import org.eclipse.core.runtime.FileLocator;
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
import com.meowlomo.ci.ems.bundle.interfaces.IDataSource;
import com.meowlomo.ci.ems.bundle.interfaces.IHttpUtil;
import com.meowlomo.ci.ems.bundle.interfaces.ISchemaValidator;
import com.meowlomo.ci.ems.bundle.interfaces.ISchemaValidator.ValidateResult;
import com.meowlomo.ci.ems.bundle.interfaces.IWebDriver;
import com.meowlomo.ci.ems.bundle.interfaces.IHttpUtil.CompositeRequestResult;
import com.meowlomo.ci.ems.bundle.interfaces.IHttpUtil.MethodType;
import com.meowlomo.ci.ems.bundle.utils.StringUtil;

import cq.bundle.console.FileJSONConvertor;

public class Activator extends BaseBundleActivator implements CommandProvider {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
	private static RequestConfig requestConfig = null;

	public Activator() {
		super(logger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		setContext(context);
		context.registerService(CommandProvider.class.getName(), this, null);

		System.out.println("Hello World in Console!!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		stopMass(context);
		System.out.println("Goodbye World in Console!!");
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "\tsay – say what you input\n";
	}

	private static String readFileContent(String filePath) throws IOException {
		// 对一串字符进行操作
		StringBuffer fileData = new StringBuffer();
		//
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[4096];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		// 缓冲区使用完必须关掉
		reader.close();
		return fileData.toString();
	}

	private void innerProcess(ServiceReference<?> serviceRef, String repoPath, String excelPath) {
		IWebDriver iwb = (IWebDriver) _context.getService(serviceRef);

		// File repoFile = new
		// File("D:\\workspace\\eclipse\\jsonProducer\\repo.xml");
		// File excelFile = new File("D:\\testcase.xlsm");

		File repoFile = new File(repoPath);// new
											// File("F:\\_testcase\\repo.xml");
		File excelFile = new File(excelPath);// new
												// File("F:\\_testcase\\testcase.xlsm");
		System.out.println(excelFile.exists());

		// JSONArray excelContent = null;
		JSONObject excelContent = null;
		boolean bUseInnerJSONString = true;
		if (bUseInnerJSONString) {
			// String sheetContent =
			// "[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\",\"Use
			// Object From Excel
			// sheet\"],{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"demo01\",\"Object\":\"TestCase.Start\",\"Use
			// Object From Excel
			// sheet\":\"String\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"http://123.206.204.103:8080/softslate/\",\"Object\":\"Engine.Browser.Navigate\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"10\",\"Object\":\"Engine.Browser.Wait\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.MSMain.Account\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123302d11w12345678@qmv.com\",\"Object\":\"MeowlomoStore.Store.Register.Email\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123456\",\"Object\":\"MeowlomoStore.Store.Register.password\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123456\",\"Object\":\"MeowlomoStore.Store.Register.RePassword\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.Register.RegisterBtn\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.FirstName\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"feifei\",\"Object\":\"MeowlomoStore.Store.AccountAddress.LastName\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhnaghsi\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Organization\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"zhangjiazhuang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Address1\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"yuanfang\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Address2\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"jingcheng\",\"Object\":\"MeowlomoStore.Store.AccountAddress.City\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"select\",\"Input\":\"Guam\",\"Object\":\"MeowlomoStore.Store.AccountAddress.State\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"wu\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Other\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"1234\",\"Object\":\"MeowlomoStore.Store.AccountAddress.PostalCode\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"select\",\"Input\":\"China\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Country\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"o75561872032\",\"Object\":\"MeowlomoStore.Store.AccountAddress.DaytimePhone\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"enter\",\"Input\":\"123219527@qq.com\",\"Object\":\"MeowlomoStore.Store.AccountAddress.Email\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"click\",\"Input\":\"yes\",\"Object\":\"MeowlomoStore.Store.AccountAddress.AddAddressBtn\",\"Use
			// Object From Excel
			// sheet\":\"\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"\",\"Input\":\"demo01\",\"Object\":\"TestCase.End\",\"Use
			// Object From Excel sheet\":\"String\"}]";
			// String sheetContent = "";
			// excelContent = new JSONObject();
			// excelContent.put("Instructions", new JSONArray(sheetContent));
		} else {

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

			// String sheetName = "Instructions";
			// Sheet sheet = workbook.getSheet(sheetName);
			// JSONArray excelSheetContent =
			// FileJSONConvertor.excelSheet2JSON(sheet);
			// JSONArray excelSheetContentNew =
			// excelContent.getJSONArray("Instructions");
			// if (excelSheetContent.similar(excelSheetContentNew))
			// System.out.println(1);
		}

		String jsonTask = "";
		if (bUseInnerJSONString) {

			try {
				jsonTask = readFileContent("D:\\commandToBundle.txt");
				JSONObject tmp = new JSONObject(jsonTask);
//				logger.info(tmp.getBoolean("standSingleton") ? "OK" : "false");
//				tmp.put("standSingleton", "true");
//				logger.info(tmp.getBoolean("standSingleton") ? "OK" : "false");
				tmp.put("geckodriverPath", System.getProperty("user.home") + "\\Desktop\\geckodriver-windows-64.exe");//TODO
				jsonTask = tmp.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				jsonTask = "";
				e.printStackTrace();
			}
			// "{\"taskData\":{\"id\":160},\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Wait\",\"Input\":\"10\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"76\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"test1@qmv.com\",\"Object\":\"77\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Object\":\"78\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"79\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"chen\",\"Object\":\"80\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"chen\",\"Object\":\"81\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"Meowlomo\",\"Object\":\"82\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"shenzheng\",\"Object\":\"83\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"nanshan\",\"Object\":\"84\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"shenzhen\",\"Object\":\"85\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"ce\",\"Object\":\"87\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"55555\",\"Object\":\"88\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Select\",\"Input\":\"Fiji\",\"Object\":\"89\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"15002090639\",\"Object\":\"90\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"809155@qq.com\",\"Object\":\"91\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"92\"}]},\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"77\":{\"tmpName\":\"Email\",\"77.locator-value\":\"loginUserName\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.type\":\"textbox\"},\"88\":{\"tmpName\":\"BillingPostalCode\",\"88.name\":\"BillingPostalCode\",\"88.type\":\"textbox\",\"88.locator-value\":\"billingPostalCode\",\"88.locator-type\":\"id\"},\"78\":{\"tmpName\":\"Password\",\"78.type\":\"textbox\",\"78.locator-type\":\"id\",\"78.name\":\"Password\",\"78.locator-value\":\"loginDecryptedPassword\"},\"89\":{\"tmpName\":\"BillingCountry\",\"89.name\":\"BillingCountry\",\"89.type\":\"dropdown\",\"89.locator-value\":\"billingCountry\",\"89.locator-type\":\"id\"},\"79\":{\"tmpName\":\"LoginBtn\",\"79.name\":\"LoginBtn\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"79.locator-type\":\"xpath\",\"79.type\":\"button\"},\"102\":{\"102.type\":\"browser\",\"tmpName\":\"Firefox\",\"102.name\":\"Firefox\",\"102.locator-value\":\"\",\"102.locator-type\":\"\"},\"90\":{\"tmpName\":\"BillingPhone1\",\"90.name\":\"BillingPhone1\",\"90.locator-type\":\"id\",\"90.type\":\"textbox\",\"90.locator-value\":\"billingPhone1\"},\"80\":{\"80.name\":\"Firstname\",\"tmpName\":\"Firstname\",\"80.locator-type\":\"id\",\"80.type\":\"textbox\",\"80.locator-value\":\"billingFirstName\"},\"91\":{\"tmpName\":\"BillingEmail1\",\"91.locator-type\":\"id\",\"91.locator-value\":\"billingEmail1\",\"91.name\":\"BillingEmail1\",\"91.type\":\"textbox\"},\"81\":{\"tmpName\":\"Lastname\",\"81.locator-value\":\"billingLastName\",\"81.locator-type\":\"id\",\"81.type\":\"textbox\",\"81.name\":\"Lastname\"},\"92\":{\"tmpName\":\"AddressesBillingButton\",\"92.name\":\"AddressesBillingButton\",\"92.locator-value\":\"accountAddressesBillingButton\",\"92.locator-type\":\"id\",\"92.type\":\"button\"},\"82\":{\"82.type\":\"textbox\",\"tmpName\":\"BillingOrganization\",\"82.locator-type\":\"id\",\"82.name\":\"BillingOrganization\",\"82.locator-value\":\"billingOrganization\"},\"83\":{\"tmpName\":\"BillingAddress1\",\"83.name\":\"BillingAddress1\",\"83.locator-type\":\"id\",\"83.locator-value\":\"billingAddress1\",\"83.type\":\"textbox\"},\"84\":{\"84.locator-value\":\"billingAddress2\",\"tmpName\":\"BillingAddress2\",\"84.name\":\"BillingAddress2\",\"84.type\":\"textbox\",\"84.locator-type\":\"id\"},\"85\":{\"tmpName\":\"BillingCity\",\"85.name\":\"BillingCity\",\"85.type\":\"textbox\",\"85.locator-type\":\"id\",\"85.locator-value\":\"billingCity\"},\"76\":{\"tmpName\":\"Account\",\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"76.locator-type\":\"xpath\"},\"87\":{\"tmpName\":\"BillingOther\",\"87.locator-value\":\"billingOther\",\"87.locator-type\":\"id\",\"87.name\":\"BillingOther\",\"87.type\":\"textbox\"}},\"logFolder\":\"F:\",\"name\":\"AddAddress\",\"firefoxPath\":\"C:/Program
			// Files (x86)/Mozilla
			// Firefox/firefox.exe\",\"parameters\":{\"finishInstructionResult\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}\",\"content\":{\"finished\":true}},\"addInstructionResult\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}/instructionResults\"},\"finishRun\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}\",\"content\":{\"finished\":true}},\"addStepLog\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}/stepLogs\"},\"addRun\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/testCases/{testCaseId}/runs\"}}}";
			// "{\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"???????\",\"Options\":\"\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Wait\",\"Input\":\"10\",\"Object\":\"102\"},{\"Comment\":\"your
			// count\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"76\"},{\"Comment\":\"????????\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"test1@qmv.com\",\"Object\":\"77\"},{\"Comment\":\"???????\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Object\":\"78\"},{\"Comment\":\"log
			// in\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"79\"}]},\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"77\":{\"tmpName\":\"Email\",\"77.locator-value\":\"loginUserName\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.type\":\"link\"},\"78\":{\"tmpName\":\"Password\",\"78.type\":\"textbox\",\"78.locator-type\":\"id\",\"78.name\":\"Password\",\"78.locator-value\":\"loginDecryptedPassword\"},\"79\":{\"tmpName\":\"LoginBtn\",\"79.name\":\"LoginBtn\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"79.locator-type\":\"xpath\",\"79.type\":\"button\"},\"102\":{\"102.type\":\"browser\",\"tmpName\":\"Firefox\",\"102.name\":\"Firefox\",\"102.locator-value\":\"\",\"102.locator-type\":\"\"},\"76\":{\"tmpName\":\"Account\",\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"76.locator-type\":\"xpath\"}},\"logFolder\":\"F:\",\"name\":\"MSLogin\",\"taskData\":{\"storages\":null,\"instructions\":[{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304555888,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Navigate\",\"orderIndex\":0,\"comment\":\"???????\",\"id\":161,\"updatedAt\":1505304555888,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"10\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304586174,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Wait\",\"orderIndex\":1,\"comment\":\"\",\"id\":162,\"updatedAt\":1505304586174,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":false,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Account\",\"createdAt\":1505296879837,\"stepOptions\":[],\"targetElementId\":76,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":2,\"comment\":\"your
			// count\",\"id\":122,\"updatedAt\":1505304586177,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='accountLink']/a\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"136\",\"htmlPositionY\":\"91\",\"createdAt\":1505284903251,\"locatorType\":\"xpath\",\"name\":\"Account\",\"comment\":\"????\",\"elementTypeId\":2,\"id\":76,\"updatedAt\":1505385617106}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":false,\"targetApplicationId\":31,\"content\":\"test1@qmv.com\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Email\",\"createdAt\":1505287404370,\"stepOptions\":[],\"targetElementId\":77,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":3,\"comment\":\"????????\",\"id\":107,\"updatedAt\":1505304586178,\"element\":{\"creator\":null,\"locatorValue\":\"loginUserName\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"633\",\"htmlPositionY\":\"54\",\"createdAt\":1505284984112,\"locatorType\":\"id\",\"name\":\"Email\",\"comment\":\"????,\",\"elementTypeId\":2,\"id\":77,\"updatedAt\":1505386315184}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":false,\"targetApplicationId\":31,\"content\":\"123456\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Password\",\"createdAt\":1505287433141,\"stepOptions\":[],\"targetElementId\":78,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":4,\"comment\":\"???????\",\"id\":108,\"updatedAt\":1505304586179,\"element\":{\"creator\":null,\"locatorValue\":\"loginDecryptedPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"629\",\"htmlPositionY\":\"192\",\"createdAt\":1505285054947,\"locatorType\":\"id\",\"name\":\"Password\",\"comment\":\"??\",\"elementTypeId\":null,\"id\":78,\"updatedAt\":1505286662007}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.LoginBtn\",\"createdAt\":1505300146898,\"stepOptions\":[],\"targetElementId\":79,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":5,\"comment\":\"log
			// in\",\"id\":149,\"updatedAt\":1505304586180,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='loginButton']/input\",\"log\":null,\"active\":true,\"type\":\"button\",\"htmlPositionX\":\"636\",\"htmlPositionY\":\"335\",\"createdAt\":1505285112520,\"locatorType\":\"xpath\",\"name\":\"LoginBtn\",\"comment\":\"Log
			// in\",\"elementTypeId\":1,\"id\":79,\"updatedAt\":1505385652393}}],\"creator\":null,\"flag\":null,\"log\":null,\"environments\":[{\"name\":\"environment1\",\"id\":1,\"value\":\"1111\"},{\"name\":\"environment2\",\"id\":2,\"value\":\"1111\"}],\"active\":true,\"message\":null,\"resultStatus\":null,\"createdAt\":1505276957754,\"engines\":[{\"createdAt\":1504870418208,\"log\":null,\"name\":\"AAAA\",\"active\":true,\"comment\":\"AAAAAAAAAAAAAAAAAA\",\"id\":1,\"updatedAt\":1504870418208},{\"createdAt\":1504870424577,\"log\":null,\"name\":\"BBBBBB\",\"active\":true,\"comment\":\"BBBBBBBBBBBBBBBBBBBB\",\"id\":2,\"updatedAt\":1504870424577}],\"name\":\"MSLogin\",\"comment\":\"????\",\"id\":155,\"updatedAt\":1505276957754,\"status\":null},\"firefoxPath\":\"C:/Program
			// Files (x86)/Mozilla
			// Firefox/firefox.exe\",\"parameters\":{\"finishInstructionResult\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}\",\"content\":{\"finished\":true}},\"finishRun\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}\",\"content\":{\"finished\":true}},\"addStepLog\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}/stepLogs\"},\"addInstructionResult\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}/instructionResults\"},\"addRun\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/testCases/{testCaseId}/runs\"}}}";
			// "{\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Wait\",\"Input\":\"10\",\"Object\":\"102\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"76\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"809155@qq.com\",\"Object\":\"77\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Object\":\"78\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"79\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"96\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Enter\",\"Input\":\"809155@qq.com\",\"Object\":\"298\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Modify\",\"Input\":\"123456\",\"Object\":\"299\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Modify\",\"Input\":\"123456\",\"Object\":\"300\"},{\"Comment\":\"\",\"Options\":\"\",\"Action\":\"Click\",\"Input\":\"yes\",\"Object\":\"301\"}]},\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"77\":{\"tmpName\":\"Email\",\"77.locator-value\":\"loginUserName\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.type\":\"textbox\"},\"78\":{\"tmpName\":\"Password\",\"78.type\":\"textbox\",\"78.locator-type\":\"id\",\"78.name\":\"Password\",\"78.locator-value\":\"loginDecryptedPassword\"},\"298\":{\"tmpName\":\"ChangeEmail\",\"298.name\":\"ChangeEmail\",\"298.locator-type\":\"id\",\"298.locator-value\":\"loginUserName\",\"298.type\":\"textbox\"},\"79\":{\"tmpName\":\"LoginBtn\",\"79.name\":\"LoginBtn\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"79.locator-type\":\"xpath\",\"79.type\":\"button\"},\"299\":{\"tmpName\":\"ChangePwd\",\"299.type\":\"textbox\",\"299.locator-type\":\"id\",\"299.name\":\"ChangePwd\",\"299.locator-value\":\"loginDecryptedPassword\"},\"102\":{\"102.type\":\"browser\",\"tmpName\":\"Firefox\",\"102.name\":\"Firefox\",\"102.locator-value\":\"\",\"102.locator-type\":\"\"},\"300\":{\"tmpName\":\"ComfirePwd\",\"300.locator-type\":\"id\",\"300.type\":\"textbox\",\"300.locator-value\":\"confirmPassword\",\"300.name\":\"ComfirePwd\"},\"301\":{\"tmpName\":\"UpdateBtn\",\"301.name\":\"UpdateBtn\",\"301.type\":\"button\",\"301.locator-type\":\"xpath\",\"301.locator-value\":\".//*[@id='accountPasswordButton']/input\"},\"96\":{\"tmpName\":\"Reset_pwd\",\"96.locator-type\":\"xpath\",\"96.name\":\"Reset_pwd\",\"96.locator-value\":\"html/body/main/ul/li[2]/a\",\"96.type\":\"link\"},\"76\":{\"tmpName\":\"Account\",\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"76.locator-type\":\"xpath\"}},\"logFolder\":\"F:\",\"name\":\"ChangePwd\",\"taskData\":{\"storages\":null,\"instructions\":[{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304686829,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Navigate\",\"orderIndex\":0,\"comment\":\"\",\"id\":165,\"updatedAt\":1505304686829,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":35,\"active\":true,\"targetApplicationId\":33,\"content\":\"10\",\"target\":\"MeowlomoStore.Normal.Engine.Firefox\",\"createdAt\":1505304705374,\"stepOptions\":[],\"targetElementId\":102,\"testCaseOptions\":[],\"action\":\"Wait\",\"orderIndex\":1,\"comment\":\"\",\"id\":166,\"updatedAt\":1505304705374,\"element\":{\"creator\":null,\"locatorValue\":\"\",\"log\":null,\"active\":true,\"type\":\"browser\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"createdAt\":1505304046892,\"locatorType\":\"\",\"name\":\"Firefox\",\"comment\":\"\",\"elementTypeId\":null,\"id\":102,\"updatedAt\":1505357363818}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Account\",\"createdAt\":1505302467438,\"stepOptions\":[],\"targetElementId\":76,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":2,\"comment\":\"\",\"id\":150,\"updatedAt\":1507627129732,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='accountLink']/a\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"136\",\"htmlPositionY\":\"91\",\"createdAt\":1505284903251,\"locatorType\":\"xpath\",\"name\":\"Account\",\"comment\":\"????\",\"elementTypeId\":2,\"id\":76,\"updatedAt\":1505385617106}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"809155@qq.com\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Email\",\"createdAt\":1505302502339,\"stepOptions\":[],\"targetElementId\":77,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":3,\"comment\":\"\",\"id\":151,\"updatedAt\":1507627129733,\"element\":{\"creator\":null,\"locatorValue\":\"loginUserName\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"633\",\"htmlPositionY\":\"54\",\"createdAt\":1505284984112,\"locatorType\":\"id\",\"name\":\"Email\",\"comment\":\"????,\",\"elementTypeId\":null,\"id\":77,\"updatedAt\":1505404470625}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"123456\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.Password\",\"createdAt\":1505302523762,\"stepOptions\":[],\"targetElementId\":78,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":4,\"comment\":\"\",\"id\":152,\"updatedAt\":1507627129734,\"element\":{\"creator\":null,\"locatorValue\":\"loginDecryptedPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"629\",\"htmlPositionY\":\"192\",\"createdAt\":1505285054947,\"locatorType\":\"id\",\"name\":\"Password\",\"comment\":\"??\",\"elementTypeId\":null,\"id\":78,\"updatedAt\":1505286662007}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":33,\"active\":true,\"targetApplicationId\":31,\"content\":\"yes\",\"target\":\"MeowlomoStore.MeowlomoStore.MSLogin.LoginBtn\",\"createdAt\":1505302572021,\"stepOptions\":[],\"targetElementId\":79,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":5,\"comment\":\"\",\"id\":153,\"updatedAt\":1507627129734,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='loginButton']/input\",\"log\":null,\"active\":true,\"type\":\"button\",\"htmlPositionX\":\"636\",\"htmlPositionY\":\"335\",\"createdAt\":1505285112520,\"locatorType\":\"xpath\",\"name\":\"LoginBtn\",\"comment\":\"Log
			// in\",\"elementTypeId\":1,\"id\":79,\"updatedAt\":1505385652393}},{\"data\":null,\"log\":null,\"targetProjectId\":151,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"yes\",\"target\":\"MeowlomoStore.PwdReset.PwdReset.Reset_pwd\",\"createdAt\":1505302635249,\"stepOptions\":[],\"targetElementId\":96,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":6,\"comment\":\"\",\"id\":155,\"updatedAt\":1507627129735,\"element\":{\"creator\":null,\"locatorValue\":\"html/body/main/ul/li[2]/a\",\"log\":null,\"active\":true,\"type\":\"link\",\"htmlPositionX\":\"60\",\"htmlPositionY\":\"34\",\"createdAt\":1505301904210,\"locatorType\":\"xpath\",\"name\":\"Reset_pwd\",\"comment\":\"Change
			// Password\",\"elementTypeId\":2,\"id\":96,\"updatedAt\":1505301907829}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"809155@qq.com\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.ChangeEmail\",\"createdAt\":1507627129729,\"stepOptions\":[],\"targetElementId\":298,\"testCaseOptions\":[],\"action\":\"Enter\",\"orderIndex\":7,\"comment\":\"\",\"id\":246,\"updatedAt\":1507627129729,\"element\":{\"creator\":null,\"locatorValue\":\"loginUserName\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"371\",\"htmlPositionY\":\"107\",\"createdAt\":1507626980794,\"locatorType\":\"id\",\"name\":\"ChangeEmail\",\"comment\":\"\",\"elementTypeId\":null,\"id\":298,\"updatedAt\":1507627048618}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"123456\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.ChangePwd\",\"createdAt\":1507627155515,\"stepOptions\":[],\"targetElementId\":299,\"testCaseOptions\":[],\"action\":\"Modify\",\"orderIndex\":8,\"comment\":\"\",\"id\":247,\"updatedAt\":1507627155515,\"element\":{\"creator\":null,\"locatorValue\":\"loginDecryptedPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"361\",\"htmlPositionY\":\"10\",\"createdAt\":1507627010876,\"locatorType\":\"id\",\"name\":\"ChangePwd\",\"comment\":\"\",\"elementTypeId\":null,\"id\":299,\"updatedAt\":1507627012688}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"123456\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.ComfirePwd\",\"createdAt\":1507627177270,\"stepOptions\":[],\"targetElementId\":300,\"testCaseOptions\":[],\"action\":\"Modify\",\"orderIndex\":9,\"comment\":\"\",\"id\":248,\"updatedAt\":1507627177270,\"element\":{\"creator\":null,\"locatorValue\":\"confirmPassword\",\"log\":null,\"active\":true,\"type\":\"textbox\",\"htmlPositionX\":\"354\",\"htmlPositionY\":\"189\",\"createdAt\":1507627042214,\"locatorType\":\"id\",\"name\":\"ComfirePwd\",\"comment\":\"\",\"elementTypeId\":null,\"id\":300,\"updatedAt\":1507627046670}},{\"data\":null,\"log\":null,\"targetProjectId\":220,\"targetSectionId\":41,\"active\":true,\"targetApplicationId\":37,\"content\":\"yes\",\"target\":\"MeowlomoStoreTest_copy.PwdReset.PwdReset.UpdateBtn\",\"createdAt\":1507627191314,\"stepOptions\":[],\"targetElementId\":301,\"testCaseOptions\":[],\"action\":\"Click\",\"orderIndex\":10,\"comment\":\"\",\"id\":249,\"updatedAt\":1507627191314,\"element\":{\"creator\":null,\"locatorValue\":\".//*[@id='accountPasswordButton']/input\",\"log\":null,\"active\":true,\"type\":\"button\",\"htmlPositionX\":\"356\",\"htmlPositionY\":\"294\",\"createdAt\":1507627079510,\"locatorType\":\"xpath\",\"name\":\"UpdateBtn\",\"comment\":\"\",\"elementTypeId\":1,\"id\":301,\"updatedAt\":1507627082340}}],\"creator\":null,\"flag\":null,\"log\":null,\"environments\":[],\"active\":true,\"message\":null,\"resultStatus\":null,\"createdAt\":1505302429449,\"engines\":[],\"name\":\"ChangePwd\",\"comment\":\"????\",\"id\":168,\"updatedAt\":1505302429449,\"status\":null},\"firefoxPath\":\"C:/Program
			// Files (x86)/Mozilla
			// Firefox/firefox.exe\",\"parameters\":{\"finishInstructionResult\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}\",\"content\":{\"finished\":true}},\"finishRun\":{\"method\":\"patch\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}\",\"content\":{\"finished\":true}},\"addStepLog\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/instructionResults/{instructionResultId}/stepLogs\"},\"addInstructionResult\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/runs/{runId}/instructionResults\"},\"addRun\":{\"method\":\"post\",\"url\":\"http://atm.meowlomo.com:8080/atm/testCases/{testCaseId}/runs\"}}}";
			// "{\"firefoxPath\":\"C:/Program Files (x86)/Mozilla
			// Firefox/firefox.exe\",\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"102\":{\"102.type\":\"browser\",\"102.name\":\"Firefox\",\"102.locator-type\":\"name\",\"102.locator-value\":\"\",\"tmpName\":\"Firefox\"},\"78\":{\"78.type\":\"textbox\",\"78.name\":\"Password\",\"78.locator-type\":\"id\",\"78.locator-value\":\"loginDecryptedPassword\",\"tmpName\":\"Password\"},\"76\":{\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-type\":\"xpath\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"tmpName\":\"Account\"},\"77\":{\"77.type\":\"textbox\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.locator-value\":\"loginUserName\",\"tmpName\":\"Email\"},\"79\":{\"79.type\":\"link\",\"79.name\":\"LoginBtn\",\"79.locator-type\":\"xpath\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"tmpName\":\"LoginBtn\"},\"96\":{\"96.type\":\"link\",\"96.name\":\"Reset_pwd\",\"96.locator-type\":\"xpath\",\"96.locator-value\":\"html/body/main/ul/li[2]/a\",\"tmpName\":\"Reset_pwd\"},\"97\":{\"97.type\":\"textbox\",\"97.name\":\"LoginUserName\",\"97.locator-type\":\"id\",\"97.locator-value\":\"loginUserName\",\"tmpName\":\"LoginUserName\"},\"98\":{\"98.type\":\"textbox\",\"98.name\":\"Password\",\"98.locator-type\":\"id\",\"98.locator-value\":\"loginDecryptedPassword\",\"tmpName\":\"Password\"},\"99\":{\"99.type\":\"textbox\",\"99.name\":\"ConfirmPassword\",\"99.locator-type\":\"id\",\"99.locator-value\":\"confirmPassword\",\"tmpName\":\"ConfirmPassword\"},\"100\":{\"100.type\":\"link\",\"100.name\":\"Update\",\"100.locator-type\":\"css\",\"100.locator-value\":\".btn.btn-primary\",\"tmpName\":\"Update\"}},\"logFolder\":\"D:/MVC_log\",\"workbook\":{\"Instructions\":[{\"Comment\":\"\",\"Object\":\"102\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"102\",\"Action\":\"Wait\",\"Input\":\"10\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"78\",\"Action\":\"Clear\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"76\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"77\",\"Action\":\"Enter\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"78\",\"Action\":\"Enter\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"79\",\"Action\":\"Click\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"96\",\"Action\":\"Click\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"97\",\"Action\":\"Enter\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"98\",\"Action\":\"Enter\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"99\",\"Action\":\"Enter\",\"Input\":\"\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"100\",\"Action\":\"Click\",\"Input\":\"\",\"Options\":\"\"}]},\"taskData\":{\"id\":228,\"name\":\"ChangePwd\",\"createdAt\":\"2017-09-13T07:33:49.049+0800\",\"updatedAt\":\"2017-11-01T07:09:48.048+0800\",\"flag\":null,\"resultStatus\":null,\"comment\":\"修改账户\",\"log\":null,\"status\":null,\"active\":true,\"ownerId\":null,\"group\":null,\"instructions\":[{\"id\":165,\"comment\":\"\",\"action\":\"Navigate\",\"updatedAt\":\"2017-09-13T08:11:26.026+0800\",\"createdAt\":\"2017-09-13T08:11:26.026+0800\",\"input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"elementId\":102,\"active\":true,\"projectId\":151,\"applicationId\":33,\"sectionId\":35,\"orderIndex\":0,\"log\":null,\"data\":null,\"target\":\"Normal.Engine.Firefox\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":102,\"name\":\"Firefox\",\"comment\":\"\",\"locatorValue\":\"\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-09-13T08:00:46.046+0800\",\"updatedAt\":\"2017-10-18T08:24:58.058+0800\",\"log\":null,\"elementType\":\"browser\",\"elementLocatorType\":\"name\",\"ownerId\":null}},{\"id\":166,\"comment\":\"\",\"action\":\"Wait\",\"updatedAt\":\"2017-09-13T08:11:45.045+0800\",\"createdAt\":\"2017-09-13T08:11:45.045+0800\",\"input\":\"10\",\"elementId\":102,\"active\":true,\"projectId\":151,\"applicationId\":33,\"sectionId\":35,\"orderIndex\":1,\"log\":null,\"data\":null,\"target\":\"Normal.Engine.Firefox\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":102,\"name\":\"Firefox\",\"comment\":\"\",\"locatorValue\":\"\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-09-13T08:00:46.046+0800\",\"updatedAt\":\"2017-10-18T08:24:58.058+0800\",\"log\":null,\"elementType\":\"browser\",\"elementLocatorType\":\"name\",\"ownerId\":null}},{\"id\":187,\"comment\":\"\",\"action\":\"Clear\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-15T11:17:45.045+0800\",\"input\":null,\"elementId\":78,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":2,\"log\":null,\"data\":null,\"target\":\"MeowlomoStore.MSLogin.Password\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":78,\"name\":\"Password\",\"comment\":\"密码\",\"locatorValue\":\"loginDecryptedPassword\",\"htmlPositionX\":\"468\",\"htmlPositionY\":\"60\",\"active\":true,\"createdAt\":\"2017-09-13T02:44:14.014+0800\",\"updatedAt\":\"2017-11-03T03:03:30.030+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":150,\"comment\":\"\",\"action\":\"Click\",\"updatedAt\":\"2017-10-28T02:08:20.020+0800\",\"createdAt\":\"2017-09-13T07:34:27.027+0800\",\"input\":\"yes\",\"elementId\":76,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":3,\"log\":null,\"data\":{},\"target\":\"MeowlomoStore.MSLogin.Account\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":76,\"name\":\"Account\",\"comment\":\"我的账户\",\"locatorValue\":\".//*[@id='accountLink']/a\",\"htmlPositionX\":\"164\",\"htmlPositionY\":\"210\",\"active\":true,\"createdAt\":\"2017-09-13T02:41:43.043+0800\",\"updatedAt\":\"2017-11-03T12:06:53.053+0800\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":151,\"comment\":\"\",\"action\":\"Enter\",\"updatedAt\":\"2017-10-28T02:08:20.020+0800\",\"createdAt\":\"2017-09-13T07:35:02.002+0800\",\"input\":\"809155@qq.com\",\"elementId\":77,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":4,\"log\":null,\"data\":{},\"target\":\"MeowlomoStore.MSLogin.Email\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":77,\"name\":\"Email\",\"comment\":\"邮箱账号,\",\"locatorValue\":\"loginUserName\",\"htmlPositionX\":\"338\",\"htmlPositionY\":\"187\",\"active\":true,\"createdAt\":\"2017-09-13T02:43:04.004+0800\",\"updatedAt\":\"2017-11-03T03:05:51.051+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":152,\"comment\":\"\",\"action\":\"Enter\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:35:23.023+0800\",\"input\":\"123456\",\"elementId\":78,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":5,\"log\":null,\"data\":null,\"target\":\"MeowlomoStore.MSLogin.Password\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":78,\"name\":\"Password\",\"comment\":\"密码\",\"locatorValue\":\"loginDecryptedPassword\",\"htmlPositionX\":\"468\",\"htmlPositionY\":\"60\",\"active\":true,\"createdAt\":\"2017-09-13T02:44:14.014+0800\",\"updatedAt\":\"2017-11-03T03:03:30.030+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":153,\"comment\":\"\",\"action\":\"Click\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:36:12.012+0800\",\"input\":\"yes\",\"elementId\":79,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":6,\"log\":null,\"data\":null,\"target\":\"MeowlomoStore.MSLogin.LoginBtn\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":79,\"name\":\"LoginBtn\",\"comment\":\"Log
			// in\",\"locatorValue\":\".//*[@id='loginButton']/input\",\"htmlPositionX\":\"636\",\"htmlPositionY\":\"335\",\"active\":true,\"createdAt\":\"2017-09-13T02:45:12.012+0800\",\"updatedAt\":\"2017-10-18T08:27:00.000+0800\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":155,\"comment\":\"\",\"action\":\"Click\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:37:15.015+0800\",\"input\":\"yes\",\"elementId\":96,\"active\":true,\"projectId\":151,\"applicationId\":37,\"sectionId\":41,\"orderIndex\":7,\"log\":null,\"data\":null,\"target\":\"PwdReset.PwdReset.Reset_pwd\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":96,\"name\":\"Reset_pwd\",\"comment\":\"Change
			// Password\",\"locatorValue\":\"html/body/main/ul/li[2]/a\",\"htmlPositionX\":\"60\",\"htmlPositionY\":\"34\",\"active\":true,\"createdAt\":\"2017-09-13T07:25:04.004+0800\",\"updatedAt\":\"2017-10-18T08:25:45.045+0800\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":156,\"comment\":\"\",\"action\":\"Enter\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:37:55.055+0800\",\"input\":\"809155@qq.com\",\"elementId\":97,\"active\":true,\"projectId\":151,\"applicationId\":37,\"sectionId\":41,\"orderIndex\":8,\"log\":null,\"data\":null,\"target\":\"PwdReset.PwdReset.LoginUserName\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":97,\"name\":\"LoginUserName\",\"comment\":\"Email\",\"locatorValue\":\"loginUserName\",\"htmlPositionX\":\"385\",\"htmlPositionY\":\"42\",\"active\":true,\"createdAt\":\"2017-09-13T07:26:23.023+0800\",\"updatedAt\":\"2017-10-18T08:25:43.043+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":157,\"comment\":\"\",\"action\":\"Enter\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:38:17.017+0800\",\"input\":\"123456\",\"elementId\":98,\"active\":true,\"projectId\":151,\"applicationId\":37,\"sectionId\":41,\"orderIndex\":9,\"log\":null,\"data\":null,\"target\":\"PwdReset.PwdReset.Password\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":98,\"name\":\"Password\",\"comment\":\"Password\",\"locatorValue\":\"loginDecryptedPassword\",\"htmlPositionX\":\"396\",\"htmlPositionY\":\"145\",\"active\":true,\"createdAt\":\"2017-09-13T07:27:09.009+0800\",\"updatedAt\":\"2017-10-18T08:25:42.042+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":158,\"comment\":\"\",\"action\":\"Enter\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:38:34.034+0800\",\"input\":\"123456\",\"elementId\":99,\"active\":true,\"projectId\":151,\"applicationId\":37,\"sectionId\":41,\"orderIndex\":10,\"log\":null,\"data\":null,\"target\":\"PwdReset.PwdReset.ConfirmPassword\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":99,\"name\":\"ConfirmPassword\",\"comment\":\"\",\"locatorValue\":\"confirmPassword\",\"htmlPositionX\":\"386\",\"htmlPositionY\":\"286\",\"active\":true,\"createdAt\":\"2017-09-13T07:27:41.041+0800\",\"updatedAt\":\"2017-10-18T08:25:40.040+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":159,\"comment\":\"\",\"action\":\"Click\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:38:48.048+0800\",\"input\":\"yes\",\"elementId\":100,\"active\":true,\"projectId\":151,\"applicationId\":37,\"sectionId\":41,\"orderIndex\":11,\"log\":null,\"data\":null,\"target\":\"PwdReset.PwdReset.Update\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":100,\"name\":\"Update\",\"comment\":\"Update\",\"locatorValue\":\".btn.btn-primary\",\"htmlPositionX\":\"386\",\"htmlPositionY\":\"385\",\"active\":true,\"createdAt\":\"2017-09-13T07:28:22.022+0800\",\"updatedAt\":\"2017-10-18T08:25:31.031+0800\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"css\",\"ownerId\":null}}],\"environments\":[],\"engines\":[],\"priority\":10,\"type\":null},\"name\":\"ChangePwd\",\"parameters\":{\"addStepLog\":{\"url\":\"http://10.0.100.177:8080/api/instructionResults/{instructionResultId}/stepLogs\",\"method\":\"post\"},\"addInstructionResult\":{\"url\":\"http://10.0.100.177:8080/api/runs/{runId}/instructionResults\",\"method\":\"post\"},\"addRun\":{\"url\":\"http://10.0.100.177:8080/api/testCases/{testCaseId}/runs\",\"method\":\"post\"},\"finishRun\":{\"url\":\"http://10.0.100.177:8080/api/runs/{runId}\",\"method\":\"patch\",\"content\":{\"finished\":true}},\"finishInstructionResult\":{\"url\":\"http://10.0.100.177/api/instructionResults/{instructionResultId}\",\"method\":\"patch\",\"content\":{\"finished\":true}}}}";
			// "{\"standSingleton\":true,\"firefoxPath\":\"C:/Program Files
			// (x86)/Mozilla
			// Firefox/firefox.exe\",\"geckodriverPath\":\"C:/Users/meteor/Desktop/geckodriver-windows-64.exe\",\"repositoryXML\":{\"102\":{\"102.type\":\"browser\",\"102.name\":\"Firefox\",\"102.locator-type\":\"name\",\"102.locator-value\":\"\",\"tmpName\":\"Firefox\"},\"76\":{\"76.type\":\"link\",\"76.name\":\"Account\",\"76.locator-type\":\"xpath\",\"76.locator-value\":\".//*[@id='accountLink']/a\",\"tmpName\":\"Account\"},\"77\":{\"77.type\":\"textbox\",\"77.name\":\"Email\",\"77.locator-type\":\"id\",\"77.locator-value\":\"loginUserName\",\"tmpName\":\"Email\"},\"78\":{\"78.type\":\"textbox\",\"78.name\":\"Password\",\"78.locator-type\":\"id\",\"78.locator-value\":\"loginDecryptedPassword\",\"tmpName\":\"Password\"},\"79\":{\"79.type\":\"link\",\"79.name\":\"LoginBtn\",\"79.locator-type\":\"xpath\",\"79.locator-value\":\".//*[@id='loginButton']/input\",\"tmpName\":\"LoginBtn\"}},\"logFolder\":\"D:/MVC_log\",\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"\",\"Object\":\"102\",\"Action\":\"Navigate\",\"Input\":\"http://123.206.204.103:8080/softslate/do/welcome?${lettermix.random(5)}\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"102\",\"Action\":\"Wait\",\"Input\":\"10\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"76\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"77\",\"Action\":\"Enter\",\"Input\":\"809155@qq.com\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"78\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"79\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"}]},\"taskData\":{\"id\":168,\"name\":\"ChangePwd\",\"createdAt\":\"2017-09-13T07:33:49.049+0800\",\"updatedAt\":\"2017-11-01T07:09:48.048+0800\",\"flag\":null,\"resultStatus\":null,\"comment\":\"修改账户\",\"log\":null,\"status\":null,\"active\":true,\"ownerId\":null,\"group\":null,\"instructions\":[{\"id\":165,\"comment\":\"\",\"action\":\"Navigate\",\"updatedAt\":\"2017-09-13T08:11:26.026+0800\",\"createdAt\":\"2017-09-13T08:11:26.026+0800\",\"input\":\"http://123.206.204.103:8080/softslate/do/welcome\",\"elementId\":102,\"active\":true,\"projectId\":151,\"applicationId\":33,\"sectionId\":35,\"orderIndex\":0,\"log\":null,\"data\":null,\"target\":\"Normal.Engine.Firefox\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":102,\"name\":\"Firefox\",\"comment\":\"\",\"locatorValue\":\"\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-09-13T08:00:46.046+0800\",\"updatedAt\":\"2017-10-18T08:24:58.058+0800\",\"log\":null,\"elementType\":\"browser\",\"elementLocatorType\":\"name\",\"ownerId\":null}},{\"id\":166,\"comment\":\"\",\"action\":\"Wait\",\"updatedAt\":\"2017-09-13T08:11:45.045+0800\",\"createdAt\":\"2017-09-13T08:11:45.045+0800\",\"input\":\"10\",\"elementId\":102,\"active\":true,\"projectId\":151,\"applicationId\":33,\"sectionId\":35,\"orderIndex\":1,\"log\":null,\"data\":null,\"target\":\"Normal.Engine.Firefox\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":102,\"name\":\"Firefox\",\"comment\":\"\",\"locatorValue\":\"\",\"htmlPositionX\":\"331\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-09-13T08:00:46.046+0800\",\"updatedAt\":\"2017-10-18T08:24:58.058+0800\",\"log\":null,\"elementType\":\"browser\",\"elementLocatorType\":\"name\",\"ownerId\":null}},{\"id\":150,\"comment\":\"\",\"action\":\"Click\",\"updatedAt\":\"2017-10-28T02:08:20.020+0800\",\"createdAt\":\"2017-09-13T07:34:27.027+0800\",\"input\":\"yes\",\"elementId\":76,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":3,\"log\":null,\"data\":{},\"target\":\"MeowlomoStore.MSLogin.Account\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":76,\"name\":\"Account\",\"comment\":\"我的账户\",\"locatorValue\":\".//*[@id='accountLink']/a\",\"htmlPositionX\":\"164\",\"htmlPositionY\":\"210\",\"active\":true,\"createdAt\":\"2017-09-13T02:41:43.043+0800\",\"updatedAt\":\"2017-11-03T12:06:53.053+0800\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":151,\"comment\":\"\",\"action\":\"Enter\",\"updatedAt\":\"2017-10-28T02:08:20.020+0800\",\"createdAt\":\"2017-09-13T07:35:02.002+0800\",\"input\":\"809155@qq.com\",\"elementId\":77,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":4,\"log\":null,\"data\":{},\"target\":\"MeowlomoStore.MSLogin.Email\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":77,\"name\":\"Email\",\"comment\":\"邮箱账号,\",\"locatorValue\":\"loginUserName\",\"htmlPositionX\":\"338\",\"htmlPositionY\":\"187\",\"active\":true,\"createdAt\":\"2017-09-13T02:43:04.004+0800\",\"updatedAt\":\"2017-11-03T03:05:51.051+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":152,\"comment\":\"\",\"action\":\"Enter\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:35:23.023+0800\",\"input\":\"123456\",\"elementId\":78,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":5,\"log\":null,\"data\":null,\"target\":\"MeowlomoStore.MSLogin.Password\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":78,\"name\":\"Password\",\"comment\":\"密码\",\"locatorValue\":\"loginDecryptedPassword\",\"htmlPositionX\":\"468\",\"htmlPositionY\":\"60\",\"active\":true,\"createdAt\":\"2017-09-13T02:44:14.014+0800\",\"updatedAt\":\"2017-11-03T03:03:30.030+0800\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":153,\"comment\":\"\",\"action\":\"Click\",\"updatedAt\":\"2017-09-15T11:17:45.045+0800\",\"createdAt\":\"2017-09-13T07:36:12.012+0800\",\"input\":\"yes\",\"elementId\":79,\"active\":true,\"projectId\":151,\"applicationId\":31,\"sectionId\":33,\"orderIndex\":6,\"log\":null,\"data\":null,\"target\":\"MeowlomoStore.MSLogin.LoginBtn\",\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":79,\"name\":\"LoginBtn\",\"comment\":\"Log
			// in\",\"locatorValue\":\".//*[@id='loginButton']/input\",\"htmlPositionX\":\"636\",\"htmlPositionY\":\"335\",\"active\":true,\"createdAt\":\"2017-09-13T02:45:12.012+0800\",\"updatedAt\":\"2017-10-18T08:27:00.000+0800\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}}],\"environments\":[],\"engines\":[],\"priority\":10,\"type\":null},\"name\":\"ChangePwd\",\"parameters\":{\"addStepLog\":{\"url\":\"http://10.0.100.177:8080/api/instructionResults/{instructionResultId}/stepLogs\",\"method\":\"post\"},\"addInstructionResult\":{\"url\":\"http://10.0.100.177:8080/api/runs/{runId}/instructionResults\",\"method\":\"post\"},\"addRun\":{\"url\":\"http://10.0.100.177:8080/api/testCases/{testCaseId}/runs\",\"method\":\"post\"},\"finishRun\":{\"url\":\"http://10.0.100.177:8080/api/runs/{runId}\",\"method\":\"patch\",\"content\":{\"finished\":true}},\"finishInstructionResult\":{\"url\":\"http://10.0.100.177:8080/api/instructionResults/{instructionResultId}\",\"method\":\"patch\",\"content\":{\"finished\":true}}}}";
			// "{\"standSingleton\":true,\"firefoxPath\":\"C:\\Program Files
			// (x86)\\Mozilla
			// Firefox\\firefox.exe\",\"geckodriverPath\":\"C:\\Users\\meteor\\Desktop\\geckodriver-windows-64.exe\",\"repositoryXML\":{\"103\":{\"103.type\":\"browser\",\"103.name\":\"Engine\",\"103.locator-type\":\"name\",\"103.locator-value\":\"FireFox\",\"tmpName\":\"Engine\"},\"104\":{\"104.type\":\"textbox\",\"104.name\":\"UserName\",\"104.locator-type\":\"id\",\"104.locator-value\":\"username\",\"tmpName\":\"UserName\"},\"105\":{\"105.type\":\"textbox\",\"105.name\":\"Password1\",\"105.locator-type\":\"id\",\"105.locator-value\":\"password\",\"tmpName\":\"Password1\"},\"106\":{\"106.type\":\"textbox\",\"106.name\":\"CheckCode\",\"106.locator-type\":\"id\",\"106.locator-value\":\"checkCode\",\"tmpName\":\"CheckCode\"},\"107\":{\"107.type\":\"button\",\"107.name\":\"LoginBtn\",\"107.locator-type\":\"id\",\"107.locator-value\":\"btn_login\",\"tmpName\":\"LoginBtn\"},\"12\":{\"12.type\":\"link\",\"12.name\":\"ICPcasemanage\",\"12.locator-type\":\"linkText\",\"12.locator-value\":\"ICP备案管理\",\"tmpName\":\"ICPcasemanage\"},\"27\":{\"27.type\":\"link\",\"27.name\":\"ICPcheckresult\",\"27.locator-type\":\"linkText\",\"27.locator-value\":\"ICP核查结果\",\"tmpName\":\"ICPcheckresult\"},\"29\":{\"29.type\":\"link\",\"29.name\":\"Jieruhecha\",\"29.locator-type\":\"linkText\",\"29.locator-value\":\"接入核查\",\"tmpName\":\"Jieruhecha\"},\"32\":{\"32.type\":\"dropdown\",\"32.name\":\"Handlestatus\",\"32.locator-type\":\"id\",\"32.locator-value\":\"q_deal_status\",\"tmpName\":\"Handlestatus\"},\"36\":{\"36.type\":\"button\",\"36.name\":\"Chaxun2\",\"36.locator-type\":\"xpath\",\"36.locator-value\":\"//div[1]/div[1]/p[2]/span/input[1]\",\"tmpName\":\"Chaxun2\"},\"140\":{\"140.type\":\"textbox\",\"140.name\":\"DealEd\",\"140.locator-type\":\"xpath\",\"140.locator-value\":\".//*[@id='jrhcDataList']/table/tbody/tr[2]\",\"tmpName\":\"DealEd\"},\"41\":{\"41.type\":\"button\",\"41.name\":\"Tiaojingqingkong\",\"41.locator-type\":\"xpath\",\"41.locator-value\":\"//div[1]/div[1]/p[2]/span/input[2]\",\"tmpName\":\"Tiaojingqingkong\"},\"38\":{\"38.type\":\"link\",\"38.name\":\"Zhankaixuanxiang\",\"38.locator-type\":\"id\",\"38.locator-value\":\"//div/div[4]/div[1]/div[1]/span[1]/a\",\"tmpName\":\"Zhankaixuanxiang\"},\"39\":{\"39.type\":\"textbox\",\"39.name\":\"Beianhao\",\"39.locator-type\":\"id\",\"39.locator-value\":\"q_baxh\",\"tmpName\":\"Beianhao\"},\"40\":{\"40.type\":\"textbox\",\"40.name\":\"Yuming\",\"40.locator-type\":\"id\",\"40.locator-value\":\"q_ym\",\"tmpName\":\"Yuming\"},\"163\":{\"163.type\":\"textbox\",\"163.name\":\"IpStart\",\"163.locator-type\":\"id\",\"163.locator-value\":\"q_qsip\",\"tmpName\":\"IpStart\"},\"164\":{\"164.type\":\"textbox\",\"164.name\":\"IpEnd\",\"164.locator-type\":\"id\",\"164.locator-value\":\"q_jsip\",\"tmpName\":\"IpEnd\"}},\"logFolder\":\"D:\\MVC_log\",\"workbook\":{\"Instructions\":[[\"Comment\",\"Object\",\"Action\",\"Input\",\"Options\"],{\"Comment\":\"火狐浏览器输入网址\",\"Object\":\"103\",\"Action\":\"Navigate\",\"Input\":\"http://192.168.21.106:8181/portal/htgl/index.jsp\",\"Options\":\"\"},{\"Comment\":\"用户名\",\"Object\":\"104\",\"Action\":\"Enter\",\"Input\":\"gd_admin\",\"Options\":\"\"},{\"Comment\":\"密码\",\"Object\":\"105\",\"Action\":\"Enter\",\"Input\":\"123456\",\"Options\":\"\"},{\"Comment\":\"验证码\",\"Object\":\"106\",\"Action\":\"Enter\",\"Input\":\"a\",\"Options\":\"\"},{\"Comment\":\"登录按钮\",\"Object\":\"107\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"ICP备案管理\",\"Object\":\"12\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"ICP核查结果\",\"Object\":\"27\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"接入核查\",\"Object\":\"29\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"处理状态\",\"Object\":\"32\",\"Action\":\"Select\",\"Input\":\"已处理\",\"Options\":\"\"},{\"Comment\":\"查询\",\"Object\":\"36\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"验证页面上查询到的已处理状态信息一共是10条\",\"Object\":\"140\",\"Action\":\"Count\",\"Input\":\"10\",\"Options\":\"\"},{\"Comment\":\"条件清空\",\"Object\":\"41\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"展开更多选项\",\"Object\":\"38\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"备案号\",\"Object\":\"39\",\"Action\":\"Enter\",\"Input\":\"meowlomo2001\",\"Options\":\"\"},{\"Comment\":\"查询\",\"Object\":\"36\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"以备案号作为查询条件，查到的数据是一条\",\"Object\":\"140\",\"Action\":\"Count\",\"Input\":\"1\",\"Options\":\"\"},{\"Comment\":\"条件清空\",\"Object\":\"41\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"域名作为查询条件做模糊查询\",\"Object\":\"40\",\"Action\":\"Enter\",\"Input\":\"meowlomo\",\"Options\":\"\"},{\"Comment\":\"查询\",\"Object\":\"36\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"以域名作为查询条件，查到的数据是10条\",\"Object\":\"140\",\"Action\":\"Count\",\"Input\":\"10\",\"Options\":\"\"},{\"Comment\":\"条件清空\",\"Object\":\"41\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"输入解析IP起始\",\"Object\":\"163\",\"Action\":\"Enter\",\"Input\":\"192.168.95.250\",\"Options\":\"\"},{\"Comment\":\"输入解析IP结束\",\"Object\":\"164\",\"Action\":\"Enter\",\"Input\":\"192.168.95.250\",\"Options\":\"\"},{\"Comment\":\"点击查询按钮\",\"Object\":\"36\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"按照解析IP查询到的数据为10条\",\"Object\":\"140\",\"Action\":\"Count\",\"Input\":\"10\",\"Options\":\"\"},{\"Comment\":\"点击条件清空按钮\",\"Object\":\"41\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"\",\"Object\":\"32\",\"Action\":\"Select\",\"Input\":\"已处理\",\"Options\":\"\"},{\"Comment\":\"域名\",\"Object\":\"40\",\"Action\":\"Enter\",\"Input\":\"meowlomo\",\"Options\":\"\"},{\"Comment\":\"备案号\",\"Object\":\"39\",\"Action\":\"Enter\",\"Input\":\"meowlomo2001\",\"Options\":\"\"},{\"Comment\":\"解析IP起始\",\"Object\":\"163\",\"Action\":\"Enter\",\"Input\":\"192.168.95.250\",\"Options\":\"\"},{\"Comment\":\"解析IP结束\",\"Object\":\"164\",\"Action\":\"Enter\",\"Input\":\"192.168.95.250\",\"Options\":\"\"},{\"Comment\":\"点击查询按钮\",\"Object\":\"36\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"},{\"Comment\":\"条件清空\",\"Object\":\"41\",\"Action\":\"Click\",\"Input\":\"yes\",\"Options\":\"\"}]},\"taskData\":{\"id\":3,\"name\":\"Jieruhecha1\",\"createdAt\":\"2017-11-21T09:27:09.009+0000\",\"updatedAt\":\"2017-11-22T04:58:42.042+0000\",\"flag\":false,\"resultStatus\":null,\"comment\":\"接入核查用例1\",\"log\":null,\"status\":null,\"active\":true,\"ownerId\":null,\"group\":null,\"instructions\":[{\"id\":523,\"comment\":\"火狐浏览器输入网址\",\"action\":\"Navigate\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-22T12:53:17.017+0000\",\"input\":\"http://192.168.21.106:8181/portal/htgl/index.jsp\",\"elementId\":103,\"active\":true,\"projectId\":2,\"applicationId\":8,\"sectionId\":41,\"orderIndex\":0,\"log\":null,\"data\":null,\"target\":\"Menu.Browser.Engine\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":103,\"name\":\"Engine\",\"comment\":\"浏览器动作\",\"locatorValue\":\"FireFox\",\"htmlPositionX\":\"55\",\"htmlPositionY\":\"72\",\"active\":true,\"createdAt\":\"2017-11-21T12:36:08.008+0000\",\"updatedAt\":\"2017-11-21T12:36:16.016+0000\",\"log\":null,\"elementType\":\"browser\",\"elementLocatorType\":\"name\",\"ownerId\":null}},{\"id\":24,\"comment\":\"用户名\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:05:48.048+0000\",\"input\":\"gd_admin\",\"elementId\":104,\"active\":true,\"projectId\":2,\"applicationId\":44,\"sectionId\":43,\"orderIndex\":1,\"log\":null,\"data\":null,\"target\":\"Login.ManageLogin.UserName\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":104,\"name\":\"UserName\",\"comment\":\"登录账号\",\"locatorValue\":\"username\",\"htmlPositionX\":\"10\",\"htmlPositionY\":\"10\",\"active\":true,\"createdAt\":\"2017-11-21T12:57:59.059+0000\",\"updatedAt\":\"2017-11-23T08:29:57.057+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":26,\"comment\":\"密码\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:06:49.049+0000\",\"input\":\"123456\",\"elementId\":105,\"active\":true,\"projectId\":2,\"applicationId\":44,\"sectionId\":43,\"orderIndex\":2,\"log\":null,\"data\":null,\"target\":\"Login.ManageLogin.Password1\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":105,\"name\":\"Password1\",\"comment\":\"密码\",\"locatorValue\":\"password\",\"htmlPositionX\":\"90\",\"htmlPositionY\":\"10\",\"active\":true,\"createdAt\":\"2017-11-21T12:59:22.022+0000\",\"updatedAt\":\"2017-11-23T08:30:12.012+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":28,\"comment\":\"验证码\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:07:38.038+0000\",\"input\":\"a\",\"elementId\":106,\"active\":true,\"projectId\":2,\"applicationId\":44,\"sectionId\":43,\"orderIndex\":3,\"log\":null,\"data\":null,\"target\":\"Login.ManageLogin.CheckCode\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":106,\"name\":\"CheckCode\",\"comment\":\"\",\"locatorValue\":\"checkCode\",\"htmlPositionX\":\"170\",\"htmlPositionY\":\"10\",\"active\":true,\"createdAt\":\"2017-11-21T12:59:53.053+0000\",\"updatedAt\":\"2017-11-23T08:30:36.036+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":29,\"comment\":\"登录按钮\",\"action\":\"Click\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:08:08.008+0000\",\"input\":\"yes\",\"elementId\":107,\"active\":true,\"projectId\":2,\"applicationId\":44,\"sectionId\":43,\"orderIndex\":4,\"log\":null,\"data\":null,\"target\":\"Login.ManageLogin.LoginBtn\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":107,\"name\":\"LoginBtn\",\"comment\":\"登录按钮\",\"locatorValue\":\"btn_login\",\"htmlPositionX\":\"250\",\"htmlPositionY\":\"10\",\"active\":true,\"createdAt\":\"2017-11-21T01:01:15.015+0000\",\"updatedAt\":\"2017-11-23T08:30:50.050+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":31,\"comment\":\"ICP备案管理\",\"action\":\"Click\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:09:53.053+0000\",\"input\":\"yes\",\"elementId\":12,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":5,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.ICPcasemanage\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":12,\"name\":\"ICPcasemanage\",\"comment\":\"ICP备案管理\",\"locatorValue\":\"ICP备案管理\",\"htmlPositionX\":\"452\",\"htmlPositionY\":\"36\",\"active\":true,\"createdAt\":\"2017-11-21T09:24:55.055+0000\",\"updatedAt\":\"2017-11-21T10:05:43.043+0000\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"linkText\",\"ownerId\":null}},{\"id\":32,\"comment\":\"ICP核查结果\",\"action\":\"Click\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:11:04.004+0000\",\"input\":\"yes\",\"elementId\":27,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":6,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.ICPcheckresult\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":27,\"name\":\"ICPcheckresult\",\"comment\":\"ICP核查结果\",\"locatorValue\":\"ICP核查结果\",\"htmlPositionX\":\"415\",\"htmlPositionY\":\"169\",\"active\":true,\"createdAt\":\"2017-11-21T09:41:08.008+0000\",\"updatedAt\":\"2017-11-21T10:42:31.031+0000\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"linkText\",\"ownerId\":null}},{\"id\":33,\"comment\":\"接入核查\",\"action\":\"Click\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:11:56.056+0000\",\"input\":\"yes\",\"elementId\":29,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":7,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Jieruhecha\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":29,\"name\":\"Jieruhecha\",\"comment\":\"接入核查\",\"locatorValue\":\"接入核查\",\"htmlPositionX\":\"550\",\"htmlPositionY\":\"130\",\"active\":true,\"createdAt\":\"2017-11-21T09:42:17.017+0000\",\"updatedAt\":\"2017-11-22T08:01:01.001+0000\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"linkText\",\"ownerId\":null}},{\"id\":34,\"comment\":\"处理状态\",\"action\":\"Select\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-21T01:14:29.029+0000\",\"input\":\"已处理\",\"elementId\":32,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":8,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Handlestatus\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":32,\"name\":\"Handlestatus\",\"comment\":\"处理状态下拉框\",\"locatorValue\":\"q_deal_status\",\"htmlPositionX\":\"726\",\"htmlPositionY\":\"-40\",\"active\":true,\"createdAt\":\"2017-11-21T09:44:35.035+0000\",\"updatedAt\":\"2017-11-21T10:42:38.038+0000\",\"log\":null,\"elementType\":\"dropdown\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":35,\"comment\":\"查询\",\"action\":\"Click\",\"updatedAt\":\"2017-11-22T12:53:17.017+0000\",\"createdAt\":\"2017-11-22T02:47:39.039+0000\",\"input\":\"yes\",\"elementId\":36,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":9,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Chaxun2\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":36,\"name\":\"Chaxun2\",\"comment\":\"查询\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[1]\",\"htmlPositionX\":\"626\",\"htmlPositionY\":\"150\",\"active\":true,\"createdAt\":\"2017-11-21T09:55:59.059+0000\",\"updatedAt\":\"2017-11-23T07:03:40.040+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":538,\"comment\":\"验证页面上查询到的已处理状态信息一共是10条\",\"action\":\"Count\",\"updatedAt\":\"2017-11-23T07:52:16.016+0000\",\"createdAt\":\"2017-11-22T01:17:50.050+0000\",\"input\":\"10\",\"elementId\":140,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":10,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.DealEd\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":140,\"name\":\"DealEd\",\"comment\":\"已处理状态的查询条件验证\",\"locatorValue\":\".//*[@id='jrhcDataList']/table/tbody/tr[2]\",\"htmlPositionX\":\"747\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-11-22T01:12:17.017+0000\",\"updatedAt\":\"2017-11-23T07:48:56.056+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":47,\"comment\":\"条件清空\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T07:57:37.037+0000\",\"createdAt\":\"2017-11-22T04:42:10.010+0000\",\"input\":\"yes\",\"elementId\":41,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":11,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Tiaojingqingkong\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":41,\"name\":\"Tiaojingqingkong\",\"comment\":\"条件清空\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[2]\",\"htmlPositionX\":\"717\",\"htmlPositionY\":\"130\",\"active\":true,\"createdAt\":\"2017-11-21T10:12:59.059+0000\",\"updatedAt\":\"2017-11-21T10:13:01.001+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":37,\"comment\":\"展开更多选项\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T07:57:37.037+0000\",\"createdAt\":\"2017-11-22T02:52:19.019+0000\",\"input\":\"yes\",\"elementId\":38,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":12,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Zhankaixuanxiang\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":38,\"name\":\"Zhankaixuanxiang\",\"comment\":\"展开更多选项\",\"locatorValue\":\"//div/div[4]/div[1]/div[1]/span[1]/a\",\"htmlPositionX\":\"80\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-11-21T10:02:57.057+0000\",\"updatedAt\":\"2017-11-22T02:51:29.029+0000\",\"log\":null,\"elementType\":\"link\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":39,\"comment\":\"备案号\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T07:57:37.037+0000\",\"createdAt\":\"2017-11-22T02:55:11.011+0000\",\"input\":\"meowlomo2001\",\"elementId\":39,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":13,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Beianhao\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":39,\"name\":\"Beianhao\",\"comment\":\"备案号\",\"locatorValue\":\"q_baxh\",\"htmlPositionX\":\"742\",\"htmlPositionY\":\"58\",\"active\":true,\"createdAt\":\"2017-11-21T10:09:02.002+0000\",\"updatedAt\":\"2017-11-23T06:49:45.045+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":41,\"comment\":\"查询\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T07:57:37.037+0000\",\"createdAt\":\"2017-11-22T02:56:11.011+0000\",\"input\":\"yes\",\"elementId\":36,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":14,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Chaxun2\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":36,\"name\":\"Chaxun2\",\"comment\":\"查询\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[1]\",\"htmlPositionX\":\"626\",\"htmlPositionY\":\"150\",\"active\":true,\"createdAt\":\"2017-11-21T09:55:59.059+0000\",\"updatedAt\":\"2017-11-23T07:03:40.040+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":796,\"comment\":\"以备案号作为查询条件，查到的数据是一条\",\"action\":\"Count\",\"updatedAt\":\"2017-11-23T07:58:22.022+0000\",\"createdAt\":\"2017-11-23T07:57:37.037+0000\",\"input\":\"1\",\"elementId\":140,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":15,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.DealEd\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":140,\"name\":\"DealEd\",\"comment\":\"已处理状态的查询条件验证\",\"locatorValue\":\".//*[@id='jrhcDataList']/table/tbody/tr[2]\",\"htmlPositionX\":\"747\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-11-22T01:12:17.017+0000\",\"updatedAt\":\"2017-11-23T07:48:56.056+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":48,\"comment\":\"条件清空\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T07:58:22.022+0000\",\"createdAt\":\"2017-11-22T04:43:04.004+0000\",\"input\":\"yes\",\"elementId\":41,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":16,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Tiaojingqingkong\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":41,\"name\":\"Tiaojingqingkong\",\"comment\":\"条件清空\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[2]\",\"htmlPositionX\":\"717\",\"htmlPositionY\":\"130\",\"active\":true,\"createdAt\":\"2017-11-21T10:12:59.059+0000\",\"updatedAt\":\"2017-11-21T10:13:01.001+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":43,\"comment\":\"域名作为查询条件做模糊查询\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T08:01:55.055+0000\",\"createdAt\":\"2017-11-22T02:58:26.026+0000\",\"input\":\"meowlomo\",\"elementId\":40,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":17,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Yuming\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":40,\"name\":\"Yuming\",\"comment\":\"域名\",\"locatorValue\":\"q_ym\",\"htmlPositionX\":\"910\",\"htmlPositionY\":\"-44\",\"active\":true,\"createdAt\":\"2017-11-21T10:10:13.013+0000\",\"updatedAt\":\"2017-11-21T10:42:15.015+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":44,\"comment\":\"查询\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T07:57:37.037+0000\",\"createdAt\":\"2017-11-22T02:59:15.015+0000\",\"input\":\"yes\",\"elementId\":36,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":18,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Chaxun2\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":36,\"name\":\"Chaxun2\",\"comment\":\"查询\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[1]\",\"htmlPositionX\":\"626\",\"htmlPositionY\":\"150\",\"active\":true,\"createdAt\":\"2017-11-21T09:55:59.059+0000\",\"updatedAt\":\"2017-11-23T07:03:40.040+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":798,\"comment\":\"以域名作为查询条件，查到的数据是10条\",\"action\":\"Count\",\"updatedAt\":\"2017-11-23T08:00:31.031+0000\",\"createdAt\":\"2017-11-23T08:00:31.031+0000\",\"input\":\"10\",\"elementId\":140,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":19,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.DealEd\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":140,\"name\":\"DealEd\",\"comment\":\"已处理状态的查询条件验证\",\"locatorValue\":\".//*[@id='jrhcDataList']/table/tbody/tr[2]\",\"htmlPositionX\":\"747\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-11-22T01:12:17.017+0000\",\"updatedAt\":\"2017-11-23T07:48:56.056+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":49,\"comment\":\"条件清空\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T08:00:31.031+0000\",\"createdAt\":\"2017-11-22T04:44:04.004+0000\",\"input\":\"yes\",\"elementId\":41,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":20,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Tiaojingqingkong\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":41,\"name\":\"Tiaojingqingkong\",\"comment\":\"条件清空\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[2]\",\"htmlPositionX\":\"717\",\"htmlPositionY\":\"130\",\"active\":true,\"createdAt\":\"2017-11-21T10:12:59.059+0000\",\"updatedAt\":\"2017-11-21T10:13:01.001+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":851,\"comment\":\"输入解析IP起始\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T08:43:40.040+0000\",\"createdAt\":\"2017-11-23T08:43:40.040+0000\",\"input\":\"192.168.95.250\",\"elementId\":163,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":21,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.IpStart\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":163,\"name\":\"IpStart\",\"comment\":\"解析IP开始\",\"locatorValue\":\"q_qsip\",\"htmlPositionX\":null,\"htmlPositionY\":null,\"active\":true,\"createdAt\":\"2017-11-23T08:41:40.040+0000\",\"updatedAt\":\"2017-11-23T08:41:40.040+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":852,\"comment\":\"输入解析IP结束\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T08:44:35.035+0000\",\"createdAt\":\"2017-11-23T08:44:35.035+0000\",\"input\":\"192.168.95.250\",\"elementId\":164,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":22,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.IpEnd\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":164,\"name\":\"IpEnd\",\"comment\":\"解析IP结束\",\"locatorValue\":\"q_jsip\",\"htmlPositionX\":null,\"htmlPositionY\":null,\"active\":true,\"createdAt\":\"2017-11-23T08:42:21.021+0000\",\"updatedAt\":\"2017-11-23T08:42:21.021+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":853,\"comment\":\"点击查询按钮\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T08:45:16.016+0000\",\"createdAt\":\"2017-11-23T08:45:16.016+0000\",\"input\":\"yes\",\"elementId\":36,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":23,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Chaxun2\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":36,\"name\":\"Chaxun2\",\"comment\":\"查询\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[1]\",\"htmlPositionX\":\"626\",\"htmlPositionY\":\"150\",\"active\":true,\"createdAt\":\"2017-11-21T09:55:59.059+0000\",\"updatedAt\":\"2017-11-23T07:03:40.040+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":855,\"comment\":\"按照解析IP查询到的数据为10条\",\"action\":\"Count\",\"updatedAt\":\"2017-11-23T08:47:35.035+0000\",\"createdAt\":\"2017-11-23T08:47:35.035+0000\",\"input\":\"10\",\"elementId\":140,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":24,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.DealEd\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":140,\"name\":\"DealEd\",\"comment\":\"已处理状态的查询条件验证\",\"locatorValue\":\".//*[@id='jrhcDataList']/table/tbody/tr[2]\",\"htmlPositionX\":\"747\",\"htmlPositionY\":\"100\",\"active\":true,\"createdAt\":\"2017-11-22T01:12:17.017+0000\",\"updatedAt\":\"2017-11-23T07:48:56.056+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":854,\"comment\":\"点击条件清空按钮\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T08:47:35.035+0000\",\"createdAt\":\"2017-11-23T08:46:20.020+0000\",\"input\":\"yes\",\"elementId\":41,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":25,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Tiaojingqingkong\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":41,\"name\":\"Tiaojingqingkong\",\"comment\":\"条件清空\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[2]\",\"htmlPositionX\":\"717\",\"htmlPositionY\":\"130\",\"active\":true,\"createdAt\":\"2017-11-21T10:12:59.059+0000\",\"updatedAt\":\"2017-11-21T10:13:01.001+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":50,\"comment\":\"\",\"action\":\"Select\",\"updatedAt\":\"2017-11-23T08:47:35.035+0000\",\"createdAt\":\"2017-11-22T04:46:55.055+0000\",\"input\":\"已处理\",\"elementId\":32,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":26,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Handlestatus\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":32,\"name\":\"Handlestatus\",\"comment\":\"处理状态下拉框\",\"locatorValue\":\"q_deal_status\",\"htmlPositionX\":\"726\",\"htmlPositionY\":\"-40\",\"active\":true,\"createdAt\":\"2017-11-21T09:44:35.035+0000\",\"updatedAt\":\"2017-11-21T10:42:38.038+0000\",\"log\":null,\"elementType\":\"dropdown\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":51,\"comment\":\"域名\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T08:48:34.034+0000\",\"createdAt\":\"2017-11-22T04:47:37.037+0000\",\"input\":\"meowlomo\",\"elementId\":40,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":27,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Yuming\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":40,\"name\":\"Yuming\",\"comment\":\"域名\",\"locatorValue\":\"q_ym\",\"htmlPositionX\":\"910\",\"htmlPositionY\":\"-44\",\"active\":true,\"createdAt\":\"2017-11-21T10:10:13.013+0000\",\"updatedAt\":\"2017-11-21T10:42:15.015+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":52,\"comment\":\"备案号\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T08:49:10.010+0000\",\"createdAt\":\"2017-11-22T04:48:24.024+0000\",\"input\":\"meowlomo2001\",\"elementId\":39,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":28,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Beianhao\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":39,\"name\":\"Beianhao\",\"comment\":\"备案号\",\"locatorValue\":\"q_baxh\",\"htmlPositionX\":\"742\",\"htmlPositionY\":\"58\",\"active\":true,\"createdAt\":\"2017-11-21T10:09:02.002+0000\",\"updatedAt\":\"2017-11-23T06:49:45.045+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":856,\"comment\":\"解析IP起始\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T08:50:15.015+0000\",\"createdAt\":\"2017-11-23T08:50:15.015+0000\",\"input\":\"192.168.95.250\",\"elementId\":163,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":29,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.IpStart\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":163,\"name\":\"IpStart\",\"comment\":\"解析IP开始\",\"locatorValue\":\"q_qsip\",\"htmlPositionX\":null,\"htmlPositionY\":null,\"active\":true,\"createdAt\":\"2017-11-23T08:41:40.040+0000\",\"updatedAt\":\"2017-11-23T08:41:40.040+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":857,\"comment\":\"解析IP结束\",\"action\":\"Enter\",\"updatedAt\":\"2017-11-23T08:51:00.000+0000\",\"createdAt\":\"2017-11-23T08:51:00.000+0000\",\"input\":\"192.168.95.250\",\"elementId\":164,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":30,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.IpEnd\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":164,\"name\":\"IpEnd\",\"comment\":\"解析IP结束\",\"locatorValue\":\"q_jsip\",\"htmlPositionX\":null,\"htmlPositionY\":null,\"active\":true,\"createdAt\":\"2017-11-23T08:42:21.021+0000\",\"updatedAt\":\"2017-11-23T08:42:21.021+0000\",\"log\":null,\"elementType\":\"textbox\",\"elementLocatorType\":\"id\",\"ownerId\":null}},{\"id\":858,\"comment\":\"点击查询按钮\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T08:51:47.047+0000\",\"createdAt\":\"2017-11-23T08:51:47.047+0000\",\"input\":\"yes\",\"elementId\":36,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":31,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Chaxun2\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":36,\"name\":\"Chaxun2\",\"comment\":\"查询\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[1]\",\"htmlPositionX\":\"626\",\"htmlPositionY\":\"150\",\"active\":true,\"createdAt\":\"2017-11-21T09:55:59.059+0000\",\"updatedAt\":\"2017-11-23T07:03:40.040+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}},{\"id\":53,\"comment\":\"条件清空\",\"action\":\"Click\",\"updatedAt\":\"2017-11-23T08:51:47.047+0000\",\"createdAt\":\"2017-11-22T04:49:10.010+0000\",\"input\":\"yes\",\"elementId\":41,\"active\":true,\"projectId\":2,\"applicationId\":1,\"sectionId\":1,\"orderIndex\":32,\"log\":null,\"data\":null,\"target\":\"ICPcasemanage.Jieruhecha.Tiaojingqingkong\",\"colorId\":null,\"type\":null,\"stepDescription\":null,\"expectedDescription\":null,\"testCaseOptions\":[],\"stepOptions\":[],\"element\":{\"id\":41,\"name\":\"Tiaojingqingkong\",\"comment\":\"条件清空\",\"locatorValue\":\"//div[1]/div[1]/p[2]/span/input[2]\",\"htmlPositionX\":\"717\",\"htmlPositionY\":\"130\",\"active\":true,\"createdAt\":\"2017-11-21T10:12:59.059+0000\",\"updatedAt\":\"2017-11-21T10:13:01.001+0000\",\"log\":null,\"elementType\":\"button\",\"elementLocatorType\":\"xpath\",\"ownerId\":null}}],\"environments\":[],\"engines\":[],\"priority\":10,\"type\":\"Mixed
			// Test\"},\"name\":\"Jieruhecha1\",\"parameters\":null}";

			if (jsonTask.isEmpty())
				return;
		} else {

			// JSONArray excelContent = FileJSONConvertor.excel2JSON(excelFile);
			JSONObject jsonParams = new JSONObject();
			jsonParams.put("firefoxPath", "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
			jsonParams.put("geckodriverPath",
					"D:\\workspace\\meowlomo\\selfgen_web_ui\\src\\recources\\driver\\geckodriver-windows-64.exe");

			JSONObject xml = FileJSONConvertor.repo2JSON(repoFile);
			xml.put("tables", FileJSONConvertor.repoTablePart2JSON(repoFile));

			jsonParams.put("elementMap", xml);
			jsonParams.put("logFolder", "C:\\Users\\meteor\\Desktop\\bundle_log");
			jsonParams.put("instructionArray", excelContent);
			jsonTask = jsonParams.toString();
			JSONArray inString = excelContent.getJSONArray("Instructions");
			String insss = inString.toString();
		}
		boolean bUseEvent = true;
		if (bUseEvent) {
			Dictionary<String, Object> msg = new Hashtable<String, Object>();
			msg.put("params", jsonTask);
			EventAdmin eventAdmin = getEventAdmin();
			Event reportGeneratedEvent = new Event("com/meowlomo/bundle/webdriver/dotest", msg);
			eventAdmin.postEvent(reportGeneratedEvent);
		} else {
			Integer bTestResult = iwb.doTestProcess(jsonTask);
			System.out.println(bTestResult);
		}
	}

	public void _t(CommandInterpreter ci) {
		
		Activator.getBundleActivator("com.meowlomo.ci.ems.bundle.interfaces.ISchemaValidator");
		BaseBundleActivator bba = Activator.getBundleActivator("com.meowlomo.ci.ems.bundle.curl");

		if (null != bba) {
			try {
				ISchemaValidator schemaValidator = bba.getServiceObject(ISchemaValidator.class);
				
				String json = readFileContent("D://fstab-good.json");
				String schema = readFileContent("D://fstab.json");
				ValidateResult vr = schemaValidator.validateJSONSchema(json, schema);
//				ValidateResult vr = ValidateResult.fromString(vrStr);
				System.out.println(vr);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// System.out.println(InstructionUtils.converMultiFuncInput("${specialsymbol.randomwfewfewfw}"));
		// System.out.println(InstructionUtils.converMultiFuncInput("${specialsymbol.random(5)}---${specialsymbol.random(5)}---"));
		// System.out.println(InstructionUtils.converMultiFuncInput("${letterlowercase.random(5)}---${lettercapital.random(5)}---"
		// + "${letterintmix.random(5)}---"
		// + "${specialsymbol.random(5)}---"
		// + "${lettermix.random(5)}---${specialsymbol.random(5)}"
		// +
		// "-${int.random(7)}-ABC-${chinese.random(10)}-ABC-${int.random(10)}-中国人${int.random(10)}"));
		//
	}

	public void _utf(CommandInterpreter ci) {
		// String param = ci.nextArgument();
		// System.out.println(param);

		IHttpUtil http = null;
		if (null == http) {
			try {
				BaseBundleActivator bba = Activator.getBundleActivator("com.meowlomo.ci.ems.bundle.curl");
				// TODO
				if (null != bba) {
					http = bba.getServiceObject(IHttpUtil.class);
				}
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		if (null != http) {
			String url = "http://10.0.100.185:8080/EMS/rest/agent/llog";
			String params = "{\"中国人\":\"美国人\"}";

			IHttpUtil.MethodType methodType = IHttpUtil.MethodType.POST;
			http.request(url, params, methodType);
		}
	}

	public void _run(CommandInterpreter ci) {
		ServiceReference<?> serviceRef = _context.getServiceReference(IWebDriver.class.getName());
		if (null == serviceRef)
			System.out.println("null web driver object");
		else {
			innerProcess(serviceRef, "F:\\_testcase\\repoTable.xml", "F:\\_testcase\\testcaseTable.xlsm");
		}
	}

	public void _geek(CommandInterpreter ci) {
		String command = ci.nextArgument();
		ServiceReference<?> serviceRef = _context.getServiceReference(IHttpUtil.class.getName());
		if (null == serviceRef)
			System.out.println("null http util object");
		else {
			IHttpUtil iHu = (IHttpUtil) _context.getService(serviceRef);
			
			JSONObject paramsHeadersObj = new JSONObject();
			paramsHeadersObj.put("Cache-Control", "no-cache");
			paramsHeadersObj.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko)");
			paramsHeadersObj.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			JSONArray paramsHeader = new JSONArray();
			paramsHeader.put(paramsHeadersObj);

			JSONObject paramsBody = new JSONObject();
			JSONArray paramsBodyArray = new JSONArray();
	
			if (command.equalsIgnoreCase("get")){
				paramsBody.put("ids", "1,2,3,4");
				paramsBodyArray.put(paramsBody);
				System.out.println(paramsBodyArray.toString());
				
				String url = "http://10.0.100.211:8080/api/cq?ids=1,2,3,4";
				String result = iHu.request(url, "", IHttpUtil.MethodType.GET);System.out.println(result);
//			    String result2 = iHu.requestHeader(url, paramsHeadersObj.toString(), paramsBodyArray.toString(), IHttpUtil.MethodType.GET);System.out.println(result2);
			}else if (command.equalsIgnoreCase("post")){
				paramsHeadersObj.put("Content-Type", "application/json");
				
				paramsBodyArray.put(UUID.randomUUID().toString());
				paramsBodyArray.put(UUID.randomUUID().toString());
				System.out.println(paramsBodyArray.toString());
				String url = "http://10.0.100.211:8080/api/cq/1/tasks";
				System.out.println(paramsHeadersObj.toString());
				CompositeRequestResult result2 = iHu.requestHeader(url, "{\"Content-Type\":\"application/json\"}", paramsBodyArray.toString(), IHttpUtil.MethodType.POST);
//				CompositeRequestResult result2 = CompositeRequestResult.fromString(result2Str);
				System.out.println(result2);
				
			}else if (command.equalsIgnoreCase("getcookie")){		
				String url = "http://atm-mid.oo/api/test/amOK";
				CompositeRequestResult result = iHu.requestHeader(url, "{}", "", IHttpUtil.MethodType.GET);
//				CompositeRequestResult result = CompositeRequestResult.fromString(resultStr);
				System.out.println(result);
//			    String result2 = iHu.requestHeader(url, paramsHeadersObj.toString(), paramsBodyArray.toString(), IHttpUtil.MethodType.GET);System.out.println(result2);
			}else if (command.equalsIgnoreCase("img")){
				String url = "https://cn.bing.com/az/hprichbg/rb/XmasTreeRoad_ZH-CN11556502034_1920x1080.jpg";
				CompositeRequestResult result = iHu.requestHeader(url, "{}", "", IHttpUtil.MethodType.GET);
//				CompositeRequestResult result = CompositeRequestResult.fromString(resultStr);
				System.out.println(result);
				
			}else if (command.equalsIgnoreCase("bd")){
				JSONArray codesArr = new JSONArray();
				codesArr.put(2013);
				codesArr.put("3410");
				
				System.out.println(String.format("%s is array", codesArr));
				System.out.println(String.join(",", codesArr.toList().toArray(new String[0])));
				
				String url = "http://www.baidu.com";
				CompositeRequestResult result = iHu.requestHeader(url, paramsHeadersObj.toString(), "", IHttpUtil.MethodType.GET);
//				CompositeRequestResult result = CompositeRequestResult.fromString(resultStr);
				System.out.println(result);
				System.out.println(result.contentHeader);
			}
		}
	}
	
	public void _go(CommandInterpreter ci) {
		// String command = ci.nextArgument();
		ServiceReference<?> serviceRef = _context.getServiceReference(IWebDriver.class.getName());
		if (null == serviceRef)
			System.out.println("null web driver object");
		else {
			innerProcess(serviceRef, "F:\\_testcase\\repo1.xml", "F:\\_testcase\\testcase1.xlsm");
		}
	}

	private void runDB() {
		IDataSource ds = null;
		logger.info("this is my host.");
		try {
			ds = getServiceObject(IDataSource.class);
			EventAdmin eventAdmin = getEventAdmin();
			if (null != eventAdmin) {
				URL fileURL = FileLocator.toFileURL(getContext().getBundle().getEntry("dbconfig.xml"));
				logger.info(getContext().getBundle().getEntry("dbconfig.xml").getFile());

				Dictionary<String, Object> msg = new Hashtable<String, Object>();
				URL url2 = getContext().getBundle().getEntry("dbconfig.xml");
				String urlPath = getContext().getBundle().getResource("/").getPath();
				// msg.put("path",
				// "C:\\workspace\\com.meowlomo.ci.beavor.bundle.db\\dbconfig.xml");

				// URI uri = new URL(StringUtil.encode(fileURL.toString(),
				// "UTF-8")).toURI();
				msg.put("path", "/dbconfig.xml");
				Event reportGeneratedEvent = new Event("com/meowlomo/bundle/db/init", msg);
				eventAdmin.postEvent(reportGeneratedEvent);
				return;
			}

			if (!ds.inited()) {
				// URL url1 =
				// Activator.class.getResource("META-INF/MANIFEST.MF");
				// URL url2 = Activator.class.getResource("dbconfig.xml");
				// File dataFile =
				// getContext().getBundle().getDataFile("utils.ssa");
				// String dataFileStr = dataFile.getAbsolutePath();
				//
				// String url1 =
				// getContext().getBundle().getEntry("META-INF/MANIFEST.MF").toURI().getPath();
				// URL url2 = getContext().getBundle().getEntry("dbconfig.xml");
				// String urlPath =
				// getContext().getBundle().getResource("/").getPath();
				//
				// logger.info(Thread.currentThread().getContextClassLoader().getResource("").getPath());
				// logger.info(url1.toString());
				// logger.info(url2.toURI().toString());
				// logger.info(url2.toString());
				//
				//// File tttt = new File(url2.toURI());
				// File ttwf2 = new File("C:/Program
				// Files/eclipse/../../workspace/com.meowlomo.ci.beavor.bundle.usedb/dbconfig.xml");
				//
				// URL fileURL =
				// FileLocator.toFileURL(getContext().getBundle().getEntry("dbconfig.xml"));
				// logger.info(fileURL.toString());
				//
				//// File ttwf2232 = new File(encode(fileURL.toString(),
				// "UTF-8"));
				// InputStream is = new URL(encode(fileURL.toString(),
				// "UTF-8")).openStream();
				// printFileContent(is);
				//
				// File tmpFile = new File(fileURL.toURI());
				// logger.info(fileURL.toURI().getPath().toString());
				// logger.info(FileLocator.toFileURL(fileURL).getPath().toString());
				// URI uri = FileLocator.toFileURL(fileURL).toURI();
				// logger.info(uri.toString());

				URL fileURL = FileLocator.toFileURL(getContext().getBundle().getEntry("dbconfig.xml"));
				logger.info(getContext().getBundle().getEntry("dbconfig.xml").getFile());
				logger.info(fileURL.toString());
				// InputStream is = new
				// URL(StringUtil.encode(fileURL.toString(),
				// "UTF-8")).openStream();
				// URI uri = new URL(StringUtil.encode(fileURL.toString(),
				// "UTF-8")).toURI();
				// ds.init(uri);
				// ds.init("dbconfig.xml");
				// ds.init("C:/workspace/com.meowlomo.ci.beavor.bundle.usedb/dbconfig.xml");
				// ds.init("C:\\workspace\\com.meowlomo.ci.beavor.bundle.db\\dbconfig.xml");
				ds.init("D:\\workspace\\eclipse\\cq.bundle.console\\dbconfig.xml");
			}
			if (ds.inited()) {
				DataSource dss = ds.getDataSource("oracleAoTain");
				logger.info("get oracle ok.{}", dss.toString());
			}
		} catch (InstantiationException | IllegalAccessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info(ds.toString());
	}

	public void _runDB(CommandInterpreter ci) {
		runDB();
	}

	// demonstrate how to use the db bundle
	public void _useDB(CommandInterpreter ci) {
		IDataSource ds = null;
		logger.info("this is my host.");
		Connection con = null;
		try {
			String dbName = ci.nextArgument();
			ds = getServiceObject(IDataSource.class);

			String sql = "";
			if (dbName.equalsIgnoreCase("mysql1"))// "tencent_vps_mysql";
				sql = "select count(1) as rows from bundle.logging_event;";
			else if (dbName.equalsIgnoreCase("postgresql1"))
				sql = "select count(1) as totalemployee, sum(salary) as rows from cqtest;";
			// else if (dbName.equalsIgnoreCase("oracle1"))
			// sql = "SELECT COUNT(ARCHITECTURE) AS rows FROM TEM_WORKERS";
			else if (dbName.equalsIgnoreCase("sqlserver1"))
				sql = "select count(distinct(LastName)) as rows from testdb.dbo.Persons;";
			else if (dbName.equalsIgnoreCase("oracleAoTain"))
				// sql = "insert into DAMS_AT_TTSS (name,address,code)
				// values('andrew', '深圳', 1234)";
				sql = "insert all into PUSH_PLATFORM.DOWN_ICP_HCJG_JRHC (WZID,WZBAXH,YM,JXIP,BBISP,HCJG,HCSJ,JRID,ISPID,REMARK,DEAL_FLAG,STATUS,DEAL_TIME,DEAL_REMARK,SHENGID,SHIID,DEAL_USERID) values (2001,'meowlomo_JRHC8','www.meowlomo.com',3232260090,1060,1,'2017-10-26 14:30:30',1008,1060,'meowlomoTest',1,3,to_date('26-11月-17','DD-MON-RR'),'批量未处理',440000,445200,892) into PUSH_PLATFORM.DOWN_ICP_HCJG_JRHC (WZID,WZBAXH,YM,JXIP,BBISP,HCJG,HCSJ,JRID,ISPID,REMARK,DEAL_FLAG,STATUS,DEAL_TIME,DEAL_REMARK,SHENGID,SHIID,DEAL_USERID) values (2002,'meowlomo_JRHC8','www.meowlomo.com   ',3232260090,1060,1,'2017-10-26 14:30:30',1008,1060,'meowlomoTest',1,3,to_date('26-11月-17','DD-MON-RR'),'批量未处理',440000,445200,892) into PUSH_PLATFORM.DOWN_ICP_HCJG_JRHC (WZID,WZBAXH,YM,JXIP,BBISP,HCJG,HCSJ,JRID,ISPID,REMARK,DEAL_FLAG,STATUS,DEAL_TIME,DEAL_REMARK,SHENGID,SHIID,DEAL_USERID) values (2003,'meowlomo_JRHC8','www.meowlomo.com   ',3232260090,1060,1,'2017-10-26 14:30:30',1008,1060,'meowlomoTest',2,3,to_date('26-11月-17','DD-MON-RR'),'批量未处理',440000,445200,892) into PUSH_PLATFORM.DOWN_ICP_HCJG_JRHC (WZID,WZBAXH,YM,JXIP,BBISP,HCJG,HCSJ,JRID,ISPID,REMARK,DEAL_FLAG,STATUS,DEAL_TIME,DEAL_REMARK,SHENGID,SHIID,DEAL_USERID) values (2003,'meowlomo_JRHC8','www.meowlomo.com   ',3232260090,1060,1,'2017-10-26 14:30:30',1008,1060,'meowlomoTest',2,3,to_date('26-11月-17','DD-MON-RR'),'批量未处理',440000,445200,892) select 1 from dual";
			else if (dbName.equalsIgnoreCase("oracle1"))
				sql = String.format("Insert into AT_TEST (name,address,code) values ('andrew', '深圳', %d)",
						System.currentTimeMillis());
			if (sql.isEmpty()) {
				logger.info("db name doesn`t match.");
				return;
			}

			DataSource dsService = ds.getDataSource(dbName);
			con = dsService.getConnection();
			Statement st = con.createStatement();

			if (sql.toLowerCase().contains("insert")) {
				System.out.println(st.executeUpdate(sql));

			} else {
				ResultSet rs = st.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();

				int columnCount = rsmd.getColumnCount();
				boolean bInserted = rs.rowInserted();
				rs.last();
				int rowCount = rs.getRow();
				for (int i = 1; i <= columnCount; ++i) {
					logger.info(rsmd.getColumnName(i));
				}
				if (rs.next()) {
					// int rowcount = rs.getInt("arow");
					int rowcount = rs.getInt(1);
					logger.info(String.format("The row count of target is %d", rowcount));
				}
			}
		} catch (InstantiationException | IllegalAccessException | SQLException e) {
			logger.error("using db error:", e);
		} finally {
			if (null != con)
				try {
					con.close();
				} catch (SQLException e) {
					logger.error("close sql connection error:", e);
				}
		}
	}

	public EventAdmin getEventAdmin() {
		if (null == _context)
			return null;

		ServiceReference<EventAdmin> ref = _context.getServiceReference(EventAdmin.class);
		if (null == ref)
			return null;

		EventAdmin eventAdmin = _context.getService(ref);
		return eventAdmin;
	}
}
