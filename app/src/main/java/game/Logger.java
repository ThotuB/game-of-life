package game;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static void log(String message) {
        final String timestamp = DateTimeFormatter.ofPattern("mm:ss:SSS")
            .format(LocalDateTime.now());

        System.out.println("[" + timestamp + "] " + message);
    }
}
