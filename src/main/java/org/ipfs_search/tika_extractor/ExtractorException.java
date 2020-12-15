package org.ipfs_search.tika_extractor;

import java.lang.RuntimeException;


public class ExtractorException extends java.lang.RuntimeException {
    public ExtractorException(Throwable e) {
        super(e);
    }
}
