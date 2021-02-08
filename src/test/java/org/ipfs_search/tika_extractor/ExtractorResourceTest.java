package org.ipfs_search.tika_extractor;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import org.junit.jupiter.api.Test;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.http.Fault;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
@TestProfile(ExtractorTestProfile.class)
public class ExtractorResourceTest extends MockServer {
    private String makeUrl(String path) {
        // This is real f*cked up, aparently in tests, URL query params are not decoded!?
        // https://stackoverflow.com/questions/13626990/jax-rs-automatic-decode-pathparam#comment18716141_13632413
        return "/extract?url=" + mock.baseUrl() + path;
    }

    @Test
    public void testUnreferencedHTML() {
    	final String cid = "QmehHHRh1a7u66r7fugebp6f6wGNMGCa7eho9cgjwhAcm2";
        final String path = "/ipfs/" + cid;

        // Unreferenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withHeader("Content-Type", "text/html").
             withBodyFile(cid)
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(200)
             .body(
             	"metadata.title[0]", is("How Filecoin Supports Video Storage"),
             	"language.language", is("en"),
             	"urls", hasItem("https://filecoin.io/uploads/video-storage-social.png")
             );
    }

    @Test
    public void testReferencedHTML() {
    	final String cid = "QmehHHRh1a7u66r7fugebp6f6wGNMGCa7eho9cgjwhAcm2";
        final String path = "/ipfs/QmUGzCZBPKL59263wbjwQ4j1RaWBV9NSNfgbDhQmaXRaaE/index.html";

        // Referenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withHeader("Content-Type", "text/html").
             withBodyFile(cid)
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(200)
             .body(
             	"metadata.title[0]", is("How Filecoin Supports Video Storage"),
             	"language.language", is("en"),
             	"urls", hasItem("https://filecoin.io/uploads/video-storage-social.png")
             );
    }

    @Test
    public void testValidJpeg() {
        final String cid = "QmezSHaJZU5ma541Ezkoa6BsUaBgVnCpVPSJvh6bE9MhqQ";
        final String path = "/ipfs/" + cid;

        // Unreferenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withHeader("Content-Type", "application/octet-stream").
             withBodyFile(cid)
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(200)
             .body(
                "metadata.Content-Type[0]", is("image/jpeg")
             );
    }

    // @Test
    // public void testNotFound() {
    //     final String path = "/notfound";

    //     // Referenced HTML file
    //     mock.stubFor(
    //      get(urlEqualTo(path)).
    //      willReturn(
    //          aResponse().
    //          withStatus(404).
    //          withHeader("Content-Type", "text/plain; charset=utf-8").
    //          withBody("404 page not found")
    //      )
    //     );

    //     given()
    //       .when().get(makeUrl(path))
    //       .then()
    //          .statusCode(404);
    // }

    @Test
    public void testInvalidCID() {
        final String path = "/ipfs/invalid";

        // Referenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withStatus(400).
             withHeader("Content-Type", "text/plain; charset=utf-8").
             withBody("invalid ipfs path: invalid path \"/ipfs/invalid\": invalid CID: selected encoding not supported")
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(400);
    }

    // Disabled: Fails unpredictably!!!
    // @Test
    // public void testReadTimeout() {
    //     // This tests unavailable resources
    //     final String cid = "QmehHHRh1a7u66r7fugebp6f6wGNMGCa7eho9cgjwhAcm2";
    //     final String path = "/ipfs/" + cid;

    //     // Configured ReadTimeout to 600ms

    //     // Unreferenced HTML file
    //     mock.stubFor(
    //      get(urlEqualTo(path)).
    //      willReturn(
    //          aResponse().
    //          withFixedDelay(700).
    //          withHeader("Content-Type", "text/html").
    //          withBodyFile(cid)
    //      )
    //     );

    //     given()
    //       .when().get(makeUrl(path))
    //       .then()
    //          .statusCode(504);
    // }

    @Test
    public void testChunked() {
        final String cid = "QmehHHRh1a7u66r7fugebp6f6wGNMGCa7eho9cgjwhAcm2";
        final String path = "/ipfs/" + cid;

        // Configured ReadTimeout to 600ms

        // Unreferenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withChunkedDribbleDelay(5, 1000). // 5 chunks with 200ms in between
             withHeader("Content-Type", "text/html").
             withBodyFile(cid)
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(200)
             .body(
                "metadata.title[0]", is("How Filecoin Supports Video Storage"),
                "language.language", is("en"),
                "urls", hasItem("https://filecoin.io/uploads/video-storage-social.png")
             );
    }

    @Test
    public void testConnectionRefused() {
        // When upstream/backend/IPFS us unavailable
        final String path = "/ipfs/QmYWWxZfDcuRxqhQwGcNvg9PMDuYnQo6S85z7dAutAjKjK";
        final String url = makeUrl(path);

        // Stop server entirely
        mock.stop();

        given()
          .when().get(url)
          .then()
             .statusCode(502);

        // Start again (teardown)
        mock.start();
    }

    @Test
    public void testServerError() {
        // When upstream/backend/IPFS us unavailable
        final String path = "/ipfs/QmYWWxZfDcuRxqhQwGcNvg9PMDuYnQo6S85z7dAutAjKjK";

        // Unreferenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
            aResponse().
             withStatus(500).
             withHeader("Content-Type", "text/plain; charset=utf-8").
             withBody("internal server error")
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(503);
    }

    @Test
    public void testLargeBodyContent() {
        // 1984 ePub
        final String cid = "QmQrtrfQ9yKRf4EVwjamSGMZv1LUAVNhYpK9gZx2J1rQUq";
        final String path = "/ipfs/" + cid;

        // Assumes BodyContentWriteLimit = 256 KB

        // Unreferenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withHeader("Content-Type", "application/octet-stream").
             withBodyFile(cid)
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(500);
    }

    @Test
    public void testTikaError() {
        // Te
        // 1984 ePub
        final String cid = "QmQrtrfQ9yKRf4EVwjamSGMZv1LUAVNhYpK9gZx2J1rQUq";
        final String path = "/ipfs/QmSGv3uotRDjrh2y9i8ZFyKpDk4rKRX9t41ZRYo8uxKMw1/corrupt.pdf";

        // Unreferenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withHeader("Content-Type", "application/octet-stream").
             withBodyFile(cid)
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(500);
    }

    @Test
    public void testVersion() {
        final String cid = "QmehHHRh1a7u66r7fugebp6f6wGNMGCa7eho9cgjwhAcm2";
        final String path = "/ipfs/" + cid;

        // Unreferenced HTML file
        mock.stubFor(
         get(urlEqualTo(path)).
         willReturn(
             aResponse().
             withHeader("Content-Type", "text/html").
             withBodyFile(cid)
         )
        );

        given()
          .when().get(makeUrl(path))
          .then()
             .statusCode(200)
             .body(
                "ipfs_tika_version", is("0.5.0"),
                "tika_version", is("1.25"),
                "tika_extractor_version", is("0.9")
             );
    }
}
