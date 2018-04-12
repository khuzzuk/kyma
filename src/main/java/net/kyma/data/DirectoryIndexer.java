package net.kyma.data;

import static net.kyma.EventType.DATA_INDEXING_AMOUNT;
import static net.kyma.EventType.DATA_INDEXING_PROGRESS;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_INDEX_GET_DIRECTORIES;
import static net.kyma.EventType.DATA_INDEX_LIST;
import static net.kyma.EventType.DATA_QUERY;
import static net.kyma.EventType.DATA_REMOVE_PATH;
import static net.kyma.EventType.RET_SOUND_FILE_CONVERTER;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
public class DirectoryIndexer implements Loadable {
    private final Bus<EventType> bus;
    private SoundFileConverter converter;
    @Setter(AccessLevel.PRIVATE)
    private Set<String> indexedPaths;

    @Override
    public void load() {
        bus.subscribingFor(RET_SOUND_FILE_CONVERTER).accept(this::setConverter).subscribe();
        bus.subscribingFor(DATA_INDEX_GET_DIRECTORIES).accept(this::setIndexedPaths).subscribe();
    }

    private void setConverter(SoundFileConverter converter)
    {
        this.converter = converter;
        bus.subscribingFor(DATA_INDEX_DIRECTORY).accept(this::indexCatalogue).subscribe();
    }

    private List<File> getFilesFromDirectory(File file) {
        if (file.isFile()) {
            String name = file.getName();
            if (name.contains(".") &&
                    Format.isSupportingFormat(name.substring(name.lastIndexOf('.')).toLowerCase())) {
                return Collections.singletonList(file);
            } else {
                return Collections.emptyList();
            }
        }
        Optional<File[]> content = Optional.ofNullable(file.listFiles());
        return Arrays.stream(content.orElse(new File[]{})).flatMap(f -> getFilesFromDirectory(f).stream())
                .collect(Collectors.toList());
    }

    private void indexCatalogue(File file) {
        List<File> files = getFilesFromDirectory(file)
                .stream().filter(f -> !f.isHidden()).collect(Collectors.toList());
        List<SoundFile> soundFiles = new ArrayList<>();
        bus.message(DATA_INDEXING_AMOUNT).withContent(files.size()).send();
        for (int x = 0; x < files.size(); x++) {
            if (x % 10 == 0) {
                bus.message(DATA_INDEXING_PROGRESS).withContent(x).send();
            }
            soundFiles.add(converter.from(files.get(x),
                    file.getPath().substring(0, file.getPath().length() - file.getName().length())));
        }

        String directoryPath = file.getPath();
        String indexingPath = indexedPaths.stream().filter(directoryPath::contains).findAny().orElse("");
        PathQueryParameters queryParameters = new PathQueryParameters(
              directoryPath.replaceFirst(indexingPath, ""), indexingPath);
        bus.message(DATA_QUERY).withContent(queryParameters).send();
        bus.message(DATA_REMOVE_PATH)
              .withContent(queryParameters)
              .onResponse(() -> bus.message(DATA_INDEX_LIST).withContent(soundFiles).send()).send();
    }
}
