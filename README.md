# tika-extractor project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `tika-extractor-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/tika-extractor-1.0.0-SNAPSHOT-runner.jar`.

## API

The API has only a single endpoint, and a single parameter: `/extract?url={url}`

Example:
```
```

## Configuration

* Connect timeout: HTTP/TCP request timeout (max. seconds until connection established).
* Read timeout: HTTP/TCP socket read timeout (max. seconds allowed time with no content coming in).
* Body content write limit: maximum amount of textual content extracted from files, in bytes.
* Worker threads: number of Tika parser threads to use.

### Environment
* EXTRACTOR_CONNECT_TIMEOUT
* EXTRACTOR_READ_TIMEOUT
* EXTRACTOR_BODY_CONTENT_WRITE_LIMIT
* EXTRACTOR_WORKER_THREADS

### application.properties
* "extractor.connect-timeout"
* "extractor.read-timeout"
* "extractor.body-content-write-limit"
* "extractor.worker-threads"
