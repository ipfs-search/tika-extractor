package org.ipfs_search.tika_extractor;

import java.net.URL;
import java.net.MalformedURLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.BadRequestException;

import javax.inject.Inject;

@Path("/extract")
public class ExtractorResource {

    @Inject
    ExtractorService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String extract(@QueryParam("url") String urlStr) throws Exception {
		URL url;
    	try {
	    	url = new URL(urlStr);
    	} catch (MalformedURLException e) {
    		throw new BadRequestException(e);
    	}

        return service.extract(url);
    }
}
