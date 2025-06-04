package tests;

import dto.UserDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GitHubUserTests {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = ConfigReader.get("baseUrl");
    }

    @Test
    public void getUserDetails_shouldReturnCorrectUser() {
        String username = "octocat";

        UserDto user = RestAssured
                .given()
                .auth().oauth2(ConfigReader.get("token"))
                .contentType(ContentType.JSON)
                .when()
                .get("/users/" + username)
                .then()
                .statusCode(200)
                .extract().as(UserDto.class);

        assertThat(user.getLogin(), equalTo(username));
        assertThat(user.getId(), greaterThan(0));
    }
}