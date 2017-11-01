package cq.java.retrofit2.test;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ATMService {
	  @GET("projects/{testId}/testCases")
	  Call<List<TestCase>> listTestCases(@Path("testId") String testId);
	  
	  @GET("elements/{elementId}/elementActions")
	  Call<List<ElementAction>> listElementActions(@Path("elementId") String elementId);
}