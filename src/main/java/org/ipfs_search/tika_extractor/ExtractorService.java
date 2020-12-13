package org.ipfs_search.tika_extractor;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLConnection;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.language.detect.LanguageHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;

import org.xml.sax.ContentHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.xml.sax.SAXException;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ExtractorService {
    private static final Logger LOG = Logger.getLogger(ExtractorService.class);

    private Parser parser;

    @Inject
    ExtractorConfiguration configuration;
    @Inject
    ExtractorClient client;

    public ExtractorService() {
    	parser = new AutoDetectParser();
    }

    public String extract(URL url) throws IOException, TikaException, SAXException {
    	TikaInputStream inputStream = getInputStream(url);

        Metadata metadata = new Metadata();

        // Setup handler
        LinkContentHandler link_handler = new LinkContentHandler();

        // Creates a content handler that writes XHTML body character events to
        // an internal string buffer. The contents of the buffer can be retrieved
        // using the {@link #toString()} method.
        //
        // The internal string buffer is bounded at the given number of characters.
        // If this write limit is reached, then a {@link SAXException} is thrown.
        BodyContentHandler content_handler = new BodyContentHandler(
            configuration.BodyContentWriteLimit
        );

        LanguageHandler language_handler = new LanguageHandler();
        ContentHandler handler = new TeeContentHandler(
            link_handler, content_handler, language_handler
        );

        String filename = FilenameUtils.getName(url.getPath());

    	// Pass resource name to Tika to aid in type detection.
    	metadata.set(Metadata.RESOURCE_NAME_KEY, filename);

    	LOG.infof("Parsing: '%s' (%s)", url.toString(), filename);

        ParseContext context = new ParseContext();

        // Parse
        try {
            parser.parse(inputStream, handler, metadata, context);
        } finally {
            inputStream.close();
        }

        List<String> links = getAbsoluteLinks(url, link_handler.getLinks());

        // Serialize to JSON
        Gson gson = new Gson();
        JsonObject output_json = gson.toJsonTree(metadata).getAsJsonObject();
        output_json.add("content", gson.toJsonTree(content_handler.toString().trim()));
        output_json.add("language", gson.toJsonTree(language_handler.getLanguage()).getAsJsonObject());
        output_json.add("urls", gson.toJsonTree(links));
        // TODO: Implement version getter, somehow.
        // output_json.add("ipfs_tika_version", gson.toJsonTree(_version));

        return output_json.toString();
    }

    private TikaInputStream getInputStream(URL url) throws IOException {
        // connection.setConnectTimeout(configuration.ConnectTimeout);
        // connection.setReadTimeout(configuration.ReadTimeout);

        return TikaInputStream.get(client.get(url.toString()));
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
            	// errorv is not working for some reason.
	    		LOG.errorf(e, "Malformed URL: '%s'", uri);
                continue;
            }

            links_out.add(abs_uri.toString());
        }

        return links_out;
    }
}
