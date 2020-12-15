package org.ipfs_search.tika_extractor;

import java.lang.RuntimeException;
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

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executorService;

    private Parser parser;

    @Inject
    ExtractorConfiguration configuration;

    @Inject
    ExtractorClient client;

    @Inject
    public ExtractorService(ExtractorConfiguration configuration) {
    	parser = new AutoDetectParser();
        executorService = Executors.newFixedThreadPool(configuration.WorkerThreads);
    }

    private String extract(URL url, TikaInputStream inputStream) throws IOException, TikaException, SAXException {
        // Synchronous implementation of extraction

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

    public CompletionStage<String> extract(URL url) {
        return client.get(url.toString()).thenApplyAsync(
            inputStream -> {
                // Aparently, in Java, lambda's cannot by marked as throwing exceptions.
                // Hence, we need to work around this by wrapping them in ExtractorException (which subclasses RuntimeException and hence isn't checked).
                try {
                    return extract(url, TikaInputStream.get(inputStream));
                } catch (Exception e) {
                    throw new ExtractorException(e);
                }
            }, executorService
        );
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
	    		LOG.errorf(e, "Malformed URL in links: '%s'", uri);
                continue;
            }

            links_out.add(abs_uri.toString());
        }

        return links_out;
    }
}
