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
It produces the `tika-extractor-<version>-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/tika-extractor-<version>-SNAPSHOT-runner.jar`.

## API

The API has only a single endpoint, and a single parameter: `/extract?url={url}`

Example:
```bash
$ curl http://localhost:8080/extract?url=https://ipfs-search.com | jq
```
```json
{
  "metadata": {
    "X-Parsed-By": [
      "org.apache.tika.parser.DefaultParser",
      "org.apache.tika.parser.html.HtmlParser"
    ],
    "theme-color": [
      "#ffffff"
    ],
    "ipfs-search/config/environment": [
      "%7B%22modulePrefix%22%3A%22ipfs-search%22%2C%22environment%22%3A%22production%22%2C%22rootURL%22%3A%22%2F%22%2C%22locationType%22%3A%22hash%22%2C%22EmberENV%22%3A%7B%22FEATURES%22%3A%7B%7D%2C%22EXTEND_PROTOTYPES%22%3A%7B%22Date%22%3Afalse%7D%2C%22_APPLICATION_TEMPLATE_WRAPPER%22%3Afalse%2C%22_DEFAULT_ASYNC_OBSERVERS%22%3Atrue%2C%22_JQUERY_INTEGRATION%22%3Afalse%2C%22_TEMPLATE_ONLY_GLIMMER_COMPONENTS%22%3Atrue%7D%2C%22APP%22%3A%7B%22name%22%3A%22ipfs-search%22%2C%22version%22%3A%220.0.0%2B50b0309f%22%7D%2C%22exportApplicationGlobal%22%3Afalse%7D"
    ],
    "description": [
      "Searching the Universe since 2016."
    ],
    "resourceName": [
      ""
    ],
    "title": [
      "ipfs-search.com"
    ],
    "msapplication-TileColor": [
      "#ffffff"
    ],
    "X-UA-Compatible": [
      "IE=edge"
    ],
    "viewport": [
      "width=device-width, initial-scale=1"
    ],
    "dc:title": [
      "ipfs-search.com"
    ],
    "Content-Encoding": [
      "UTF-8"
    ],
    "Content-Type": [
      "text/html; charset=UTF-8"
    ],
    "msapplication-TileImage": [
      "/assets/favicon/ms-icon-144x144-97b82643fa46f40bc5d39bb9f9f4d2a4.png"
    ]
  },
  "content": "",
  "language": {
    "language": "en",
    "confidence": "HIGH",
    "rawScore": 0.9999939
  },
  "urls": [
    "https://ipfs-search.com/assets/favicon/apple-icon-57x57-f3d9762ed2c0ef67e76f4c077ec8786d.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-60x60-fb3c7378f10d458e28dffd7c53b8ad5d.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-72x72-4151f9cdb0d0529aeb17c70269cb5aab.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-76x76-7baafb7e85c244f2497853bf8f0b694a.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-114x114-c6b50a36a2adee6936e0350d0e345b56.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-120x120-a8150629e7753742e5264f1ef4279a80.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-144x144-97b82643fa46f40bc5d39bb9f9f4d2a4.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-152x152-d5e933200e0e3d6546a1a6aa102ccf93.png",
    "https://ipfs-search.com/assets/favicon/apple-icon-180x180-a2505e6d4f41e922e09e6671aa19149f.png",
    "https://ipfs-search.com/assets/favicon/android-icon-192x192-f22e8fbbf9f1671c96d02384de845653.png",
    "https://ipfs-search.com/assets/favicon/favicon-32x32-c791131f8140b7f0f6a6468ffe06d2c8.png",
    "https://ipfs-search.com/assets/favicon/favicon-96x96-bc47792df41e7408c7bd96f98940d31e.png",
    "https://ipfs-search.com/assets/favicon/favicon-16x16-b989cab1b3e10ef7cdc2e3cc8edf48a2.png",
    "https://ipfs-search.com/assets/favicon/manifest.json",
    "https://ipfs-search.com/assets/vendor-d3aa84b783735f00b7be359e81298bf2.css",
    "https://ipfs-search.com/assets/ipfs-search-bb25acca849005bf36ba75e089bc50e0.css"
  ],
  "ipfs_tika_version": "0.5.0",
  "tika_version": "1.25",
  "tika_extractor_version": "0.9"
}
```

## Configuration

* Connect timeout: HTTP/TCP request timeout (max. seconds until connection established).
* Read timeout: HTTP/TCP socket read timeout (max. seconds allowed time with no content coming in).
* Body content write limit: maximum amount of textual content extracted from files, in bytes.
* Parser worker threads: number of Tika parser threads to use.
* Connection pool size: maximum number of client connections to maintain.
* Max pooled per route: maximum number of connections to maintain per route.

### Environment
* `EXTRACTOR_CONNECT_TIMEOUT`
* `EXTRACTOR_READ_TIMEOUT`
* `EXTRACTOR_BODY_CONTENT_WRITE_LIMIT`
* `EXTRACTOR_PARSER_WORKER_THREADS`
* `EXTRACTOR_CONNECTION_POOL_SIZE`
* `EXTRACTOR_MAX_POOLED_PER_ROUTE`

### application.properties
* `extractor.connect-timeout`
* `extractor.read-timeout`
* `extractor.body-content-write-limit`
* `extractor.parser-worker-threads`
* `extractor.connection-pool-size`
* `extractor.max-pooled-per-route`
