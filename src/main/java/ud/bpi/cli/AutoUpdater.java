package ud.bpi.cli;

import javax.json.Json;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static ud.bpi.cli.Constants.TWO_FOLDER_HOPS;
import static ud.bpi.cli.Constants.VERSION;

public class AutoUpdater {

    private static final String VERSION_ZIP_FILE = TWO_FOLDER_HOPS + "%d.zip";
    private static final String VERSION_TXT = "version.txt";

    private enum Status {
        NOT_READY,
        SHOULD_UPDATE,
        UP_TO_DATE
    }

    private static class State {
        private int currentVersion;
        private int latestVersion;
        private Status status;

        State(int currentVersion) {
            this.currentVersion = currentVersion;
            status = Status.NOT_READY;
        }

        void setLatestVersion(int latestVersion) {
            this.latestVersion = latestVersion;

            status = currentVersion < latestVersion ? Status.SHOULD_UPDATE : Status.UP_TO_DATE;
        }
    }

    private final String updateCheckURL;
    private final String updateZipURL;

    private final HttpClient updateClient;

    private final State state;

    public AutoUpdater(Properties config) {
        updateCheckURL = config.getProperty("updateCheckURL");
        updateZipURL = config.getProperty("updateZipURL");

        updateClient = HttpClient.newHttpClient();

        state = new State(Integer.valueOf(config.getProperty(VERSION)));
    }

    public CompletableFuture<Boolean> check() {
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(updateCheckURL))
                    .GET()
                    .build();

            return updateClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> Json.createReader(new StringReader(response.body())).readObject())
                    .thenApply(jsonObject -> {

                        state.setLatestVersion(Integer.valueOf(jsonObject.getString(VERSION)));

                        return state.status == Status.SHOULD_UPDATE;
                    });

        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    public CompletableFuture<Boolean> update() {

        if (state.status != Status.SHOULD_UPDATE) {
            return CompletableFuture.completedFuture(false);
        }

        var downloadToFile = String.format(VERSION_ZIP_FILE, state.latestVersion);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(String.format(updateZipURL, state.latestVersion)))
                    .GET()
                    .build();

            System.out.println("Downloading a newer version of BPI CLI...");

            return updateClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofFile(Paths.get(downloadToFile)))
                    .thenApply(response -> {

                        unzip(response.body());
                        saveVersion(state.latestVersion);

                        return true;
                    });

        } catch (URISyntaxException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    private static void unzip(Path path) {
        System.out.println("Download complete. Unzipping...");

        File zipFile = path.toFile();

        File unzipFolder = new File(zipFile.getParent());

        try {
            Utils.unzip(zipFile, unzipFolder);
            zipFile.delete();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void saveVersion(int version) {
        try {
            Files.writeString(Paths.get(TWO_FOLDER_HOPS, VERSION_TXT), String.valueOf(version));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

