package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int LINES = 3;
    private static final String HOME = System.getProperty("user.home");
    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String FILE_OUTPUT = HOME + SEPARATOR + "Output.txt";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        Configuration.Builder cb = new Configuration.Builder();
         new Configuration.Builder().build();
        try {
            cb = initializeConstants();
        } catch (IOException e) {
            for (var v : this.views) {
                v.displayError(e.getMessage());
            }
        }
        final Configuration fileConf = cb.build();
        if (fileConf.isConsistent()) {
            this.model = new DrawNumberImpl(fileConf);
        } else {
            this.model = new DrawNumberImpl(new Configuration.Builder().build());
        }
    }

    private static Configuration.Builder initializeConstants() throws IOException {
        final Configuration.Builder cf = new Configuration.Builder();
        try (final BufferedReader sr = new BufferedReader(
            new InputStreamReader(
                ClassLoader.getSystemResourceAsStream("config.yml")
            )
        )) {
            for (int i = 0; i < LINES; i++) {
                String line = sr.readLine();
                if (line.contains("minimum")) {
                    cf.setMin(Integer.parseInt(getLastWord(line)));
                } else if (line.contains("maximum")) {
                    cf.setMax(Integer.parseInt(getLastWord(line)));
                } else if (line.contains("attempts")) {
                    cf.setAttempts(Integer.parseInt(getLastWord(line)));
                }
            }
        }
        return cf;
    }

    private static String getLastWord(final String line) {
        final var tknzr = new StringTokenizer(line);
        String lastWord = new String();
        while (tknzr.hasMoreTokens()) {
            lastWord = tknzr.nextToken();
        }
        return lastWord;
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(),
                          new DrawNumberViewImpl(),
                          new PrintStreamView(System.out),
                          new PrintStreamView(FILE_OUTPUT));
    }

}
