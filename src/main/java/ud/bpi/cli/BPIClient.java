package ud.bpi.cli;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static ud.bpi.cli.Constants.BPI;
import static ud.bpi.cli.Constants.RATE;
import static ud.bpi.cli.Constants.USD;

/**
 * Talks to the coin desk REST API to retrieve the current bitcoin price index.
 */
class BPIClient {

    private final HttpRequest bpiRequest;

    BPIClient(Properties config) throws URISyntaxException {

        // The GET request to the coin desk API is always the same. So we construct the request upfront.
        bpiRequest = HttpRequest.newBuilder()
                .uri(new URI(config.getProperty("bpiURL")))
                .GET()
                .build();
    }

    /**
     * Return a string describing the current bitcoin price index in USD.
     *
     * @return a string describing the current bitcoin price index in USD.
     */
    CompletableFuture<String> getCurrentBPI() {


        return HttpClient.newHttpClient()
                .sendAsync(bpiRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::toJson)
                .thenApply(bpiJson -> {
                    var rate = bpiJson.getJsonObject(USD).getString(RATE);

                    return "Current Price: $" + rate;
                });
    }

    private JsonObject toJson(HttpResponse<String> response) {
        return Json.createReader(new StringReader(response.body())).readObject().getJsonObject(BPI);
    }
}
