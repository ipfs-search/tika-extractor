package org.ipfs_search.tika_extractor;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Optional;

@ConfigProperties
public class ExtractorConfiguration {
	// Should connect within 1s - this is real bad if it fails!
	public int ConnectTimeout = 1*1000;

 	// No data for 30s - die!
	public int ReadTimeout = 30*1000;

	// 10 MB default max.
	public int BodyContentWriteLimit = 10*1024*1024;

	public int ParserWorkerThreads = 8;

    // public int ClientWorkerThreads = 8;

    public int ConnectionPoolSize = 256;
    public int MaxPooledPerRoute = 256;

    // 256 KB is IPFS' standard chunk size
    public int ClientResponseBufferSize = 256*1024;
}
