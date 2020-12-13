package org.ipfs_search.tika_extractor;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ServiceUnavailableException;

import javax.inject.Inject;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import org.jboss.logging.Logger;

@Path("/extract")
public class ExtractorResource {
    private static final Logger LOG = Logger.getLogger(ExtractorResource.class);

    @Inject
    ExtractorService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<String> extract(@QueryParam("url") String urlStr) throws WebApplicationException {
		URL url;
    	try {
	    	url = new URL(urlStr);
    	} catch (MalformedURLException e) {
    		LOG.error("Invalid URL", e);
    		throw new BadRequestException(e);
    	}

        return service.extract(url);

        // TODO: Re-introduce nuanced error handling, separating:
        // 1. Upstream timeouts
        // 2. Parser errors
        // 3. Upstream unavailable
        // 4. Body content read limit overschrijding
   //      try {
			// return service.extract(url);
   //      } catch (IOException e) {
   //      	// Upstream unavailable. Tell clients to try again in 2s.
   //      	throw new ServiceUnavailableException(2L, e);
   //      } catch (TikaException | SAXException e) {
   //  		LOG.error(e.toString(), e);
   //      	throw new InternalServerErrorException(e);
   //      }
    }
}
