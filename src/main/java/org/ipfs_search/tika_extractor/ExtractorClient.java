package org.ipfs_search.tika_extractor;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
// import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.InputStream;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ExtractorClient {
    private static final Logger LOG = Logger.getLogger(ExtractorResource.class);
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Client client;

    public ExtractorClient() {
        client = ClientBuilder.newBuilder()
                .executorService(executorService)
                .build();

    	LOG.info("Configured extractor client");
    }

    InputStream get(String url) {
    	LOG.infov("ExtractorClient GET: %s", url);

        return client.target(url)
                .request()
                .get(InputStream.class);

    }
}
