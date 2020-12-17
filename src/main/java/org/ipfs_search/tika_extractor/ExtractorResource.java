package org.ipfs_search.tika_extractor;

import java.net.URL;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import java.util.concurrent.CompletionStage;
import java.lang.RuntimeException;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Param;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;

import io.smallrye.mutiny.Uni;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.inject.Inject;

import org.jboss.logging.Logger;

public class ExtractorResource {
    private static final Logger LOG = Logger.getLogger(ExtractorResource.class);
    private static final Gson gson = new Gson();

    @Inject
    ExtractorService service;

    @Route(
      path = "/extract",
      produces = "application/json",
      methods = HttpMethod.GET,
      order = 0
    )
    public Uni<String> extract(@Param("url") String urlStr) throws WebApplicationException {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            LOG.error("Invalid URL", e);
            throw new BadRequestException(e);
        }

        Uni<String> result = Uni.createFrom().completionStage(service.extract(url));

        LOG.debug("Returning async response.");

        return result;
    }

    private String throwableToJson(Throwable e) {
        JsonObject output_json = new JsonObject();
        output_json.add("error", gson.toJsonTree(e.toString()));

        return output_json.toString();
    }

    // 400 in upstream yields 400 (BAD REQUEST)
    @Route(
        type = Route.HandlerType.FAILURE,
        produces = "application/json",
        order = 1
    )
    void upstreambadrequest(javax.ws.rs.BadRequestException e, HttpServerResponse response) {
        response.setStatusCode(400).end(throwableToJson(e));
    }

    // 404 in upstream yields 404 (NOT FOUND)
    @Route(
        type = Route.HandlerType.FAILURE,
        produces = "application/json",
        order = 2
    )
    void upstreamnotfound(javax.ws.rs.NotFoundException e, HttpServerResponse response) {
        response.setStatusCode(404).end(throwableToJson(e));
    }

    // 500 in upstream yields 503 (BAD GATEWAY)
    @Route(
        type = Route.HandlerType.FAILURE,
        produces = "application/json",
        order = 3
    )
    void upstreamserverexception(javax.ws.rs.InternalServerErrorException e, HttpServerResponse response) {
        response.setStatusCode(503).end(throwableToJson(e));
    }

    @Route(
        type = Route.HandlerType.FAILURE,
        produces = "application/json",
        order = 4
    )
    void processingexception(javax.ws.rs.ProcessingException e, HttpServerResponse response) {
        // It seems that this is where Client exceptions end up.

        if (e.getCause() instanceof java.net.ConnectException) {
            // Unable to connect:  502 (SERVICE UNAVAILABLE)
            response.setStatusCode(502).end(throwableToJson(e));
            return;
        }

        if (e.getCause() instanceof java.net.SocketTimeoutException) {
            // Gateway timeout
            response.setStatusCode(504).end(throwableToJson(e));
            return;
        }

        LOG.errorf(e, "Unexpected exceptiqon in Client: %s", e.toString());
        response.setStatusCode(500).end(throwableToJson(e));
    }

    @Route(
        type = Route.HandlerType.FAILURE,
        produces = "application/json",
        order = 5
    )
    void extractorexception(ExtractorException e, HttpServerResponse response) {
        // This is where exceptions from the Tika end up.

        // if (e.getCause() instanceof org.xml.sax.SAXException) {
        //     // Body too large, amongst possible others.
        //     // Note that content would be available in such case, hence we *might* choose to index it (in the future).
        //     // Ref: "org.apache.tika.sax.WriteOutContentHandler$WriteLimitReachedException: Your document contained more than 262143 characters, and so your requested limit has been reached. To receive the full text of the document, increase your limit. (Text up to the limit is however available)."
        //     response.setStatusCode(500).end(throwableToJson(e));
        //     return;
        // }

        // Any other errors here are propqer internal server errors.
        response.setStatusCode(500).end(throwableToJson(e));
    }
}
