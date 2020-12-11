package org.ipfs_search.tika_extractor;

import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.inject.Inject;

@Path("/extract")
public class ExtractorResource {

    @Inject
    ExtractorService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String extract() throws Exception {
    	URL url = new URL("http://google.com/");
        return service.extract(url);
    }
}
