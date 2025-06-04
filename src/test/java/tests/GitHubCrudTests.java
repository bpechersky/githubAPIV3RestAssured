package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GitHubCrudTests {

    private String token;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = ConfigReader.get("baseUrl");
        token = ConfigReader.get("token");
    }

    @Test
    public void getAuthenticatedUser_shouldReturnUserInfo() {
        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .get("/user")
            .then()
            .statusCode(200)
            .body("login", notNullValue());
    }

    @Test
    public void createAndDeleteRepo_shouldSucceed() {
        String repoName = "test-repo-" + System.currentTimeMillis();

        Response createResp = RestAssured
            .given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body("{\"name\": \"" + repoName + "\", \"auto_init\": true}")
            .when()
            .post("/user/repos")
            .then()
            .statusCode(201)
            .extract().response();

        String fullName = createResp.jsonPath().getString("full_name");

        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .get("/repos/" + fullName)
            .then()
            .statusCode(200)
            .body("name", equalTo(repoName));

        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .delete("/repos/" + fullName)
            .then()
            .statusCode(204);
    }

    @Test
    public void createAndDeleteGist_shouldSucceed() {
        String gistPayload = "{\n" +
                "  \"description\": \"Sample Gist\",\n" +
                "  \"public\": true,\n" +
                "  \"files\": {\n" +
                "    \"test.txt\": {\n" +
                "      \"content\": \"Hello, world!\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Response response = RestAssured
            .given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body(gistPayload)
            .when()
            .post("/gists")
            .then()
            .statusCode(201)
            .extract().response();

        String gistId = response.jsonPath().getString("id");

        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .get("/gists/" + gistId)
            .then()
            .statusCode(200)
            .body("id", equalTo(gistId));

        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .delete("/gists/" + gistId)
            .then()
            .statusCode(204);
    }
}