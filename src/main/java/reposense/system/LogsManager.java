package reposense.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configures and manages the loggers and handlers, including their levels.
 */
public class LogsManager {

    // Whenever the log file size exceeds {@code MAX_FILE_SIZE_IN_BYTES} it rolls over to another file
    // The maximum number of files to store the logs is {@code FILE_COUNT}
    private static final int FILE_COUNT = 2;
    private static final int MEGABYTE = (1 << 20);
    private static final int MAX_FILE_SIZE_IN_BYTES = 5 * MEGABYTE; // 5MB
    private static final ArrayList<Logger> LOGGER_LIST = new ArrayList<>();

    // All the log files will be store with a .log extension
    // eg. reposense.log.0, in the logs/ folder of the working directory
    private static final String LOG_FOLDER_NAME = "logs";
    private static final String LOG_FILE_NAME = "reposense.log";

    private static Path logFolderLocation;
    private static Level currentConsoleLogLevel = Level.INFO;
    private static Level currentFileLogLevel = Level.INFO;
    private static FileHandler fileHandler;
    private static ConsoleHandler consoleHandler;

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        LOGGER_LIST.add(logger);
        logger.setUseParentHandlers(false);

        removeHandlers(logger);
        addConsoleHandler(logger);

        if (logFolderLocation != null) {
            addFileHandler(logger);
        }

        return logger;
    }

    /**
     * Creates a {@link Logger} for the given {@code clazz} name.
     */
    public static <T> Logger getLogger(Class<T> clazz) {
        if (clazz == null) {
            return Logger.getLogger("");
        }
        return getLogger(clazz.getSimpleName());
    }

    /**
     * Adds the {@link ConsoleHandler} to the {@code logger}.
     * Creates the {@link ConsoleHandler} if it is null.
     */
    private static void addConsoleHandler(Logger logger) {
        if (consoleHandler == null) {
            consoleHandler = createConsoleHandler();
        }
        logger.addHandler(consoleHandler);
    }

    /**
     * Removes all the handlers from {@code logger}.
     */
    private static void removeHandlers(Logger logger) {
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            logger.removeHandler(handler);
        }
    }

    /**
     * Adds the {@link FileHandler} to the {@code logger}.
     * Creates {@link FileHandler} if it is null.
     */
    private static void addFileHandler(Logger logger) {
        Path path = logFolderLocation.resolve(LOG_FOLDER_NAME);

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Log folder has been successfully created");
            }

            if (fileHandler == null) {
                fileHandler = createFileHandler();
            }
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Creates a {@link FileHandler} for the log file.
     *
     * @throws IOException if there are problems opening the file.
     */
    private static FileHandler createFileHandler() throws IOException {
        Path path = logFolderLocation.resolve(LOG_FOLDER_NAME).resolve(LOG_FILE_NAME);
        FileHandler fileHandler = new FileHandler(path.toString(), MAX_FILE_SIZE_IN_BYTES, FILE_COUNT, true);
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(currentFileLogLevel);
        return fileHandler;
    }

    /**
     * Creates a {@link ConsoleHandler} to output the log to console.
     */
    private static ConsoleHandler createConsoleHandler() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(currentConsoleLogLevel);
        consoleHandler.setFormatter(new CustomLogFormatter());
        return consoleHandler;
    }

    /**
     * Sets the log folder location using {@code location} and adds file handler with this location to all the loggers
     * created.
     */
    public static void setLogFolderLocation(Path location) {
        logFolderLocation = location;
        LOGGER_LIST.stream().forEach(logger -> addFileHandler(logger));
    }
}

