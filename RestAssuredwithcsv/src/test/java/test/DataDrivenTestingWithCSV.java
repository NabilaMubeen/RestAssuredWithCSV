package test;

import static org.testng.Assert.assertEquals;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import Utils.FileNameConstants;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Epic("Epic-01")
@Feature("Create Get Delete users")
public class DataDrivenTestingWithCSV {
	
  @Story("Story 1")
  @Test(dataProvider = "CSVTestData")
  @Description("Data Driven Testing using CSV File")
  @Severity(SeverityLevel.CRITICAL)
  
  public void DataDrivenTesting(Map<String,String> testData) throws Exception {
	  // Prepare the body using testData values
	    Map<String, Object> requestBody = new HashMap<>();
	    requestBody.put("id", testData.get("id"));
	    requestBody.put("firstname", testData.get("firstname"));
	    requestBody.put("lastname", testData.get("lastname"));
	    requestBody.put("email", testData.get("email"));
	  
	  // Convert Map to JSON string using Jackson ObjectMapper
	    ObjectMapper objectMapper = new ObjectMapper();
	    String jsonBody = objectMapper.writeValueAsString(requestBody);
	 	
	  Response response = RestAssured.given().filter(new AllureRestAssured()).log().all()
				.contentType(ContentType.JSON)
				.body(jsonBody).log().all()
				.baseUri("https://reqres.in/api/users")
				.when()
				.post()
				.then()
				.log().all()
				.extract()
				.response();
	  assertEquals(response.statusCode(), 201, "Expected status code is not matching!");
	  
	  getuser(testData);
	
  }
  
  @Test(dataProvider = "CSVTestData")
  public void getuser(Map<String,String> testData) {
	String Userid = testData.get("id");
	  Response response = RestAssured.given().filter(new AllureRestAssured())
			  .pathParam("id", Userid)
	            .when()
	            .get("https://reqres.in/api/users/{id}")
	            .then()
	            .statusCode(200) // Assert the status code is 200 for valid user ID
	            .extract().response();
	  System.out.println(response);
	  
	  if (Integer.parseInt(Userid) == 1) {
		  Deleteuser(Userid);
      }
  }
  public void Deleteuser(String Userid) {

	  Response deleteResponse = RestAssured.given().filter(new AllureRestAssured())
			  .pathParam("id", Userid)
	          .when()
	          .delete("https://reqres.in/api/users/{id}")
	          .then()
	          .statusCode(204) // Assert the delete operation was successful (204 No Content)
	          .extract().response();
	  System.out.println("User with ID: " + Userid + " has been deleted.");
	  
  }
  
  @DataProvider(name = "CSVTestData")
  public Object [][] getTestData(){
	  Object [][] objArray = null;
	  Map<String,String> map = null;
	  List<Map<String,String>> testDataList = null;
	  try {
		CSVReader csvReader = new CSVReader(new FileReader(FileNameConstants.CSV_TEST_DATA));
		testDataList = new ArrayList<Map<String,String>>();
		String[] line = null;
		int count = 0;
		while((line = csvReader.readNext())!=null) {
			if(count == 0 ) {
				count++;
				continue;
			}
			map = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
			map.put("id", line[0]);
			map.put("firstname", line[1]);
			map.put("lastname", line[2]);
			map.put("email", line[3]);
			testDataList.add(map);
		}
		
		objArray = new Object[testDataList.size()][1];
		for(int i = 0; i < testDataList.size(); i++) {
			objArray[i][0] = testDataList.get(i);
		}
		
		
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (CsvValidationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  
	  return objArray;
  }
}


//Commands to run project on cmd
//cd path\to\your\project
//mvn test
//Run specified test class: mvn -Dtest=DataDrivenTestingWithCSV test
//Run a specific method within a test class: mvn -Dtest=TestClassName#testMethodName test

