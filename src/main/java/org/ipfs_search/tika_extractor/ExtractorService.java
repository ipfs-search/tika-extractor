package org.ipfs_search.tika_extractor;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExtractorService {

    public String extract(URL url) {
        return "hello " + url.toString();
    }

}
