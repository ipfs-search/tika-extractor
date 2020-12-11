package org.ipfs_search.tika_extractor;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLConnection;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.language.detect.LanguageHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.xml.sax.SAXException;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ExtractorService {
    private static final Logger LOG = Logger.getLogger(ExtractorService.class);

    private AutoDetectParser parser;

    public ExtractorService() {
    	parser = new AutoDetectParser();
    }

    public String extract(URL url) throws IOException, TikaException, SAXException {
    	TikaInputStream inputStream = getInputStream(url);

        Metadata metadata = new Metadata();

        // Setup handler
        LinkContentHandler link_handler = new LinkContentHandler();
        // TODO: Make max size for body handler configurable.
        BodyContentHandler body_handler = new BodyContentHandler(10*1024*1024);
        LanguageHandler language_handler = new LanguageHandler();
        TeeContentHandler handler = new TeeContentHandler(
            link_handler, body_handler, language_handler
        );

        // Set filename from path string
        // TODO: Only do this when a sensible filename is found and/or use optional filename parameter.
        // String filename = path.substring(path.lastIndexOf("/")+1, path.length());
        // metadata.set(Metadata.RESOURCE_NAME_KEY, filename);

        // System.out.println("Parsing: " + uri.toString() + " ("+filename+")");

        // Parse
        try {
            parser.parse(inputStream, handler, metadata);
        } finally {
            inputStream.close();
        }

        List<String> links = getAbsoluteLinks(url, link_handler.getLinks());

        // Serialize to JSON
        Gson gson = new Gson();
        JsonObject output_json = gson.toJsonTree(metadata).getAsJsonObject();
        output_json.add("content", gson.toJsonTree(body_handler.toString().trim()));
        output_json.add("language", gson.toJsonTree(language_handler.getLanguage()).getAsJsonObject());
        output_json.add("urls", gson.toJsonTree(links));
        // TODO: Implement version getter, somehow.
        // output_json.add("ipfs_tika_version", gson.toJsonTree(_version));

        return output_json.toString();
    }

    private TikaInputStream getInputStream(URL url) throws IOException {
        URLConnection connection = url.openConnection();

        // TODO: Move this to configurable settings
        connection.setConnectTimeout(1000); // Should connect within 1s - this is real bad if it fails!
        connection.setReadTimeout(30*1000); // No data for 30s - die!

        return TikaInputStream.get(connection.getInputStream());
    }

    private List<String> getAbsoluteLinks(URL parent_url, List<Link> links) {
        List<String> links_out = new ArrayList<String>();
        String uri;

        for (Link link : links) {
            uri = link.getUri();

            if (StringUtils.isBlank(uri)) {
                continue;
            }
            String abs_uri;

            // build an absolute URL
            try {
                URL tmpURL = new URL(parent_url, uri);
                abs_uri = tmpURL.toExternalForm();
            } catch (MalformedURLException e) {
            	// Skip MalformedURL's.

            	// TODO: Replace entry below by log statement.
                // System.err.println("Skipping in links MalformedURLException:\n" + e.getMessage());
                continue;
            }

            links_out.add(abs_uri.toString());
        }

        return links_out;
    }
}
