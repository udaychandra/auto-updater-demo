package ud.bpi.cli;

import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher {

    private static final Logger logger = Logger.getLogger(Launcher.class.getName());

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

                // TODO: Handle errors--if download and unzip fails.
                if (autoUpdater.update().get()) {
                    System.out.println("Restarting the CLI...");
                    System.exit(100);
                }
            }

        } catch (InterruptedException | ExecutionException ex) {
            logger.log(Level.INFO, "Unable to check for version updates.", ex);
        }

        welcome(config);
        getBPI(config);
    }

    private void welcome(Properties config) {
        System.out.println("\n=======================================");
        System.out.println(String.format("  BPI CLI Version %s", config.getProperty("version")));
        System.out.println("=======================================\n");
    }

    private void getBPI(Properties config) {
        try {
            var bpiClient = new BPIClient(config);

            System.out.println(bpiClient.getCurrentBPI().get());
            System.out.println();

        } catch (URISyntaxException ex) {
            logger.info("Unable to retrieve the configuration");

        } catch (InterruptedException | ExecutionException ex) {
            logger.log(Level.INFO, "Unable to retrieve Bitcoin Price Index at this moment. Please try later.", ex);
        }
    }
}

