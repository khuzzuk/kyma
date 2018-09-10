package net.kyma.web;

import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.IndexingRoot;
import org.apache.commons.lang3.exception.ExceptionUtils;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class YoutubeDownloadedFilesReader implements Loadable {
    private final Bus<EventType> bus;

    @Override
    public void load() {
        bus.subscribingFor(EventType.DATA_GET_PATHS).then(this::readDownloadedPaths).subscribe();
    }

    private void readDownloadedPaths() {
        try {
            Set<String> downloadedDirectories = Files.walk(YoutubeDownloader.BASE_PATH, 1)
                    .filter(Files::isDirectory)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.toCollection(TreeSet::new));

            Map<IndexingRoot, Set<String>> paths = Map.of(IndexingRoot.forWebDownloads(), downloadedDirectories);
            bus.message(EventType.DATA_REFRESH_PATHS).withContent(paths).send();
        } catch (IOException e) {
            bus.message(EventType.SHOW_ALERT).withContent(ExceptionUtils.getStackTrace(e)).send();
        }
    }
}
