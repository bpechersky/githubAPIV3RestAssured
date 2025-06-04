package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GitHubAdvancedTests {

    private String token;
    private String username;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = ConfigReader.get("baseUrl");
        token = ConfigReader.get("token");

        // Fetch username from authenticated user
        username = RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .get("/user")
            .then()
            .statusCode(200)
            .extract().jsonPath().getString("login");
    }

    @Test
    public void createAndManageIssue_shouldSucceed() {
        String repoName = "issue-repo-" + System.currentTimeMillis();

        // Create repo
        RestAssured
            .given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body("{\"name\": \"" + repoName + "\", \"auto_init\": true}")
            .when()
            .post("/user/repos")
            .then()
            .statusCode(201);

        // Create issue
        String issueTitle = "Sample Issue";
        Response issueResp = RestAssured
            .given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body("{\"title\": \"" + issueTitle + "\"}")
            .when()
            .post("/repos/" + username + "/" + repoName + "/issues")
            .then()
            .statusCode(201)
            .extract().response();

        int issueNumber = issueResp.jsonPath().getInt("number");

        // Get issue
        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .get("/repos/" + username + "/" + repoName + "/issues/" + issueNumber)
            .then()
            .statusCode(200)
            .body("title", equalTo(issueTitle));

        // Close issue
        RestAssured
            .given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body("{\"state\": \"closed\"}")
            .when()
            .patch("/repos/" + username + "/" + repoName + "/issues/" + issueNumber)
            .then()
            .statusCode(200)
            .body("state", equalTo("closed"));

        // Delete repo
        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .delete("/repos/" + username + "/" + repoName)
            .then()
            .statusCode(204);
    }

    @Test
    public void listBranches_shouldReturnMasterOrMain() {
        String repo = "Hello-World";

        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .get("/repos/octocat/" + repo + "/branches")
            .then()
            .statusCode(200)
            .body("name", hasItem(anyOf(equalTo("main"), equalTo("master"))));
    }

    @Test
    public void getNonexistentRepo_shouldReturn404() {
        RestAssured
            .given()
            .auth().oauth2(token)
            .when()
            .get("/repos/octocat/nonexistent-repo-xyz")
            .then()
            .statusCode(404);
    }
}