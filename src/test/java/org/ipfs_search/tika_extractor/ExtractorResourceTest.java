package org.ipfs_search.tika_extractor;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ExtractorResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/extract")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy"));
    }

}