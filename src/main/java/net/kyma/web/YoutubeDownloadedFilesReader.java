package net.kyma.web;

import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.data.SoundFileConverter;
import net.kyma.dm.SoundFile;
import org.apache.commons.lang3.exception.ExceptionUtils;
import pl.khuzzuk.messaging.Bus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class YoutubeDownloadedFilesReader implements Loadable {
    public static final String LABEL_NAME = "Imported from youtube";
    private final Bus<EventType> bus;
    private SoundFileConverter soundFileConverter;

    @Override
    public void load() {
        bus.subscribingFor(EventType.RET_SOUND_FILE_CONVERTER).accept(this::setSoundFileConverter).subscribe();
        bus.subscribingFor(EventType.DATA_GET_PATHS).then(this::readDownloadedPaths).subscribe();
    }

    private void setSoundFileConverter(SoundFileConverter soundFileConverter) {
        this.soundFileConverter = soundFileConverter;
        bus.subscribingFor(EventType.DATA_WEB_DOWNLOADS_QUERY).mapResponse(this::findDownloadedFiles).subscribe();
    }

    private void readDownloadedPaths() {
        try {
            Set<String> downloadedDirectories = Files.walk(YoutubeDownloader.BASE_PATH, 1)
                    .filter(Files::isDirectory)
                    .map(Path::toString)
                    .collect(Collectors.toCollection(TreeSet::new));

            Map<String, Set<String>> paths = Map.of(LABEL_NAME + "/", downloadedDirectories);
            bus.message(EventType.DATA_REFRESH_PATHS).withContent(paths).send();
        } catch (IOException e) {
            bus.message(EventType.SHOW_ALERT).withContent(ExceptionUtils.getStackTrace(e)).send();
        }
    }

    private Collection<SoundFile> findDownloadedFiles(String path) {
        Collection<SoundFile> soundFiles = new ArrayList<>();
        Path lookup = YoutubeDownloader.BASE_PATH.resolve(path);
        if (Files.exists(lookup)) {
            addSoundFilesToCollection(lookup, YoutubeDownloader.BASE_PATH.toString(), soundFiles);
        }
        return soundFiles;
    }

    private void addSoundFilesToCollection(Path path, String basePath, Collection<SoundFile> soundFiles) {
        try (Stream<Path> files = Files.walk(path)){
            files.forEach(System.out::print);
        } catch (IOException e) {
            bus.message(EventType.SHOW_ALERT).withContent(ExceptionUtils.getStackTrace(e)).send();
        }
        try (Stream<Path> files = Files.walk(path)){
            files.map(p -> soundFileConverter.from(p.toFile(), basePath))
                    .forEach(soundFiles::add);
        } catch (IOException e) {
            bus.message(EventType.SHOW_ALERT).withContent(ExceptionUtils.getStackTrace(e)).send();
        }
    }
}
