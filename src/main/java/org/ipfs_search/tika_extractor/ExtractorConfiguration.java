package org.ipfs_search.tika_extractor;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Optional;

@ConfigProperties
public class ExtractorConfiguration {
	// Should connect within 1s - this is real bad if it fails!
	@ConfigProperty(defaultValue = "1000")
	public int ConnectTimeout;

 	// No data for 30s - die!
	@ConfigProperty(defaultValue="30000")
	public int ReadTimeout;

	// 10 MB default max.
	@ConfigProperty(defaultValue="10485760")
	public int BodyContentWriteLimit;
}
