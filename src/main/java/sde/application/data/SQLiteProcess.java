package sde.application.data;

import sde.application.error.Error;
import sde.application.utils.AppParams;
import sde.application.utils.SDEUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class SQLiteProcess {
    private Process sqLite;
    private String sqLiteFileName;
    private BufferedReader input;

    public SQLiteProcess() {
        sqLiteFileName = "data/" + AppParams.getSqlLiteFileName();
    }

    public void start() {
        try {
            if (SDEUtils.isJar()) {
                URI uri = SDEUtils.getFile(SDEUtils.getJarURI(), sqLiteFileName);
                sqLite = new ProcessBuilder(uri.getPath(), "sde.db").start();
            } else {
                sqLite = new ProcessBuilder(SDEUtils.getResourcePath() + sqLiteFileName, "sde.db").start();
            }

            // Here we don't actually do anything with the input from SQLite
            input = new BufferedReader(new InputStreamReader(sqLite.getInputStream()));
        } catch (IOException | URISyntaxException ex) {
            Error.SQLITE_START_EXE.record().create(ex);
        }
    }
}
