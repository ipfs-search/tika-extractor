package org.ipfs_search.tika_extractor;

// import org.apache.http.client.utils.URIBuilder;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
public class ExtractorResourceTest extends MockServer {
    @Test
    public void testUnreferencedHTML() throws Exception {
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

        // This is real f*cked up, aparently in tests, URL query params are not decoded!?
        final String url = "/extract?url=" + mock.baseUrl() + path;
        // https://stackoverflow.com/questions/13626990/jax-rs-automatic-decode-pathparam#comment18716141_13632413

		// URIBuilder url = new URIBuilder("/extract");
		// url.addParameter("url", mock.baseUrl() + "/ipfs/QmehHHRh1a7u66r7fugebp6f6wGNMGCa7eho9cgjwhAcm2");

        given()
          .when().get(url)
          .then()
             .statusCode(200)
             .body(
             	"metadata.title[0]", is("How Filecoin Supports Video Storage"),
             	"language.language", is("en"),
             	"urls", hasItem("https://filecoin.io/uploads/video-storage-social.png")
             );
    }

    @Test
    public void testReferencedHTML() throws Exception {
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

        // This is real f*cked up, aparently in tests, URL query params are not decoded!?
        final String url = "/extract?url=" + mock.baseUrl() + path;
        // https://stackoverflow.com/questions/13626990/jax-rs-automatic-decode-pathparam#comment18716141_13632413

		// URIBuilder url = new URIBuilder("/extract");
		// url.addParameter("url", mock.baseUrl() + "/ipfs/QmehHHRh1a7u66r7fugebp6f6wGNMGCa7eho9cgjwhAcm2");

        given()
          .when().get(url)
          .then()
             .statusCode(200)
             .body(
             	"metadata.title[0]", is("How Filecoin Supports Video Storage"),
             	"language.language", is("en"),
             	"urls", hasItem("https://filecoin.io/uploads/video-storage-social.png")
             );
    }

}
