import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;
import java.util.ArrayList;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class TestCases {

//    Test a positive regular scenario
    @Test
    public void positiveTestCase() {
        String countryURL = "https://restcountries.eu/rest/v2/all?fields=name;capital;currencies;latlng";
        Response CountriesResponse = given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                when().get(countryURL).
                then().assertThat().statusCode(200).body(matchesJsonSchemaInClasspath("CountriesSchema.json")).contentType(ContentType.JSON).extract().response();

        ArrayList<Object> jsonObjResponse = CountriesResponse.jsonPath().getJsonObject("$");

//        Select a Random country from the list
        int size = jsonObjResponse.size();
        int randomIndex = (int)(Math.random() * size);

//        Construct the Capital URL
        String Capital = CountriesResponse.jsonPath().getString("["+randomIndex+"].capital");
        String capitalURL = "https://restcountries.eu/rest/v2/capital/"+ Capital +"?fields=name;capital;currencies;latlng;regionalBlocs";


//        Request the Capital URL and validate the response
        given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                when().get(capitalURL).
        then().assertThat().statusCode(200)
                .body(matchesJsonSchemaInClasspath("CapitalsSchema.json"))
//        Validate the currency code
        .body("[0].currencies.symbol", equalTo(CountriesResponse.jsonPath().getJsonObject("["+randomIndex+"].currencies.symbol")));
    }

//    Test an incorrect capital name
    @Test
    public void negativeTestCase() {

//        Construct the Capital URL
        String Capital = "fakeCapitalName";
        String capitalURL = "https://restcountries.eu/rest/v2/capital/"+ Capital +"?fields=name;capital;currencies;latlng;regionalBlocs";


//        Request the Capital URL and validate the response
        given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                when().get(capitalURL).
                then().assertThat().statusCode(404)
                .body(matchesJsonSchemaInClasspath("ErrorMessageSchema.json"));
    }
}