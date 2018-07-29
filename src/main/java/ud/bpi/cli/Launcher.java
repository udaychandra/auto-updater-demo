package ud.bpi.cli;

import javax.json.JsonObject;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static ud.bpi.cli.Constants.BPI;
import static ud.bpi.cli.Constants.RATE;
import static ud.bpi.cli.Constants.USD;

public class Launcher {

    public static void main(String... args) {

        new Launcher().launch();
    }

    private final Properties config;

    private Launcher() {
        config = Utils.loadConfig();
    }

    private void launch() {

        AutoUpdater autoUpdater = new AutoUpdater(config);

        try {

            if (autoUpdater.check().get()) {

                System.out.println("\nA new version of BPI CLI is available. Please hold...");

                // TODO: What if download and unzip fails?
                if (autoUpdater.update().get()) {
                    System.out.println("Restarting the CLI...");
                    System.exit(100);
                }
            }

        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            System.out.println("BPI CLI: unable to check for version updates.");
        }

        welcome(config);
        getBPI(config);
    }

    private void welcome(Properties config) {
        System.out.println("\n===================================");
        System.out.println(String.format("  BPI CLI %s", config.getProperty("version")));
        System.out.println("===================================\n");
    }

    private void getBPI(Properties config) {
        try {
            BPIClient bpiClient = new BPIClient(config);

            JsonObject usd = bpiClient.getCurrentBPI().get()
                    .getJsonObject(BPI)
                    .getJsonObject(USD);

            System.out.println("Current Price: $" + usd.getString(RATE));


        } catch (URISyntaxException ex) {
            System.out.println("BPI CLI: unable to retrieve the configuration");

        } catch (InterruptedException | ExecutionException ex) {
            System.out.println("BPI CLI: unable to retrieve Bitcoin Price Index at this moment. Please try later.");
        }
    }
}

