package cq.java.retrofit2.test;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TestApplication {

	public static void main(String[] args) {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("http://atm.meowlomo.com/atm/")
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		
		ATMService atmService = retrofit.create(ATMService.class);
//		Call<List<TestCase>> tcs = atmService.listTestCases("151");
		Call<List<ElementAction>> easCall = atmService.listElementActions("83");
		Response<List<ElementAction>> easResbonse = null;
		try {
			easResbonse = easCall.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (null != easResbonse){
			List<ElementAction> eas = easResbonse.body();
			if (null != eas)
				for(ElementAction ea : eas)
					System.out.println(ea);
		}
	}
}
