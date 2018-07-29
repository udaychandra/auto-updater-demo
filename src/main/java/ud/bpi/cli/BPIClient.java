package ud.bpi.cli;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class BPIClient {

    private final HttpRequest bpiRequest;
    private final HttpClient bpiApiClient;

    public BPIClient(Properties config) throws URISyntaxException {

        bpiRequest = HttpRequest.newBuilder()
                .uri(new URI(config.getProperty("bpiURL")))
                .GET()
                .build();

        bpiApiClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<JsonObject> getCurrentBPI() {

        return bpiApiClient
                .sendAsync(bpiRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> Json.createReader(new StringReader(response.body())).readObject());
    }
}
