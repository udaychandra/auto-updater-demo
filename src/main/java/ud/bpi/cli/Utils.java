package ud.bpi.cli;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Utils {

    /**
     * Load the properties file "cli.properties" from the resources folder.
     *
     * @return a {@link Properties} object holding the CLI configuration.
     */
    static Properties loadConfig() {
        Properties properties = new Properties();

        try {
            properties.load(Utils.class.getResourceAsStream("cli.properties"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return properties;
    }

    static void unzip(File zipFile, File unzipFolder) throws IOException {

        if (!unzipFolder.exists()) {
            unzipFolder.mkdir();
        }

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zipInputStream.getNextEntry();

        while (entry != null) {
            File file = new File(unzipFolder, entry.getName());

            if (!entry.isDirectory()) {
                unzipFile(zipInputStream, file);

            } else {
                file.mkdir();
            }

            zipInputStream.closeEntry();
            entry = zipInputStream.getNextEntry();
        }

        zipInputStream.close();
    }

    private static void unzipFile(ZipInputStream zipInputStream, File file) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

        byte[] bytesIn = new byte[1024];
        int read;

        while ((read = zipInputStream.read(bytesIn)) != -1) {
            outputStream.write(bytesIn, 0, read);
        }

        outputStream.close();
    }
}
