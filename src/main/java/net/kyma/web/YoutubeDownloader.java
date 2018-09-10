package net.kyma.web;

import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import pl.khuzzuk.messaging.Bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static net.kyma.log.Logger.reportToUser;

@RequiredArgsConstructor
public class YoutubeDownloader extends Thread implements Loadable {
    private static final String EXEC_PREFIX = "youtube-dl -x --audio-format mp3 --audio-quality 0 %s";
    static final Path BASE_PATH = Paths.get("downloaded");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd-kk-mm-ss-SSS");

    private final Bus<EventType> bus;
    private BlockingQueue<URL> tasks;

    @Override
    public void load() {
        setDaemon(true);
        try {
            if (Files.notExists(BASE_PATH)) Files.createDirectory(BASE_PATH);
            tasks = new LinkedBlockingQueue<>();
            bus.subscribingFor(EventType.PLAYLIST_ADD_YOUTUBE).<URL>accept(tasks::offer).subscribe();
            bus.message(EventType.DATA_GET_PATHS).send();
            start();
        } catch (IOException e) {
            bus.message(EventType.SHOW_ALERT).withContent(ExceptionUtils.getStackTrace(e)).send();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                URL url = tasks.take();
                downloadFromUrl(url);
            } catch (InterruptedException e) {
                reportToUser(bus, e);
                interrupt();
            } catch (Exception e) {
                reportToUser(bus, e);
            }
        }
    }

    private void downloadFromUrl(URL url) {
        try {
            Path newDir = Files.createDirectory(BASE_PATH.resolve(LocalDateTime.now().format(FORMATTER))).toAbsolutePath();
            String command = String.format(EXEC_PREFIX, url.toString());
            Process process = Runtime.getRuntime().exec(command, null, newDir.toFile());
            reportProgress(process);
        } catch (IOException e) {
            reportToUser(bus, e);
        }
    }

    private void reportProgress(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            bus.message(EventType.DATA_INDEXING_AMOUNT).withContent(100).send();
            while ((line = reader.readLine()) !=null) {
                if (line.contains("%")) {
                    Integer progress = Double.valueOf(line.split("\\s+")[1].replace("%", "")).intValue();
                    bus.message(EventType.DATA_INDEXING_PROGRESS).withContent(progress).send();
                }
            }
            bus.message(EventType.DATA_INDEXING_FINISH).send();
        }
    }
}
