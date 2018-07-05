package net.kyma.data;

import static net.kyma.EventType.DATA_INDEXING_AMOUNT;
import static net.kyma.EventType.DATA_INDEXING_PROGRESS;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_INDEX_GET_DIRECTORIES;
import static net.kyma.EventType.DATA_INDEX_LIST;
import static net.kyma.EventType.DATA_QUERY;
import static net.kyma.EventType.DATA_QUERY_RESULT_FOR_CONTENT_VIEW;
import static net.kyma.EventType.DATA_REMOVE_PATH;
import static net.kyma.EventType.RET_SOUND_FILE_CONVERTER;
import static net.kyma.data.PathUtils.normalizePath;

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
import net.kyma.dm.DataQuery;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.initialization.Dependable;
import net.kyma.initialization.Dependency;
import net.kyma.player.Format;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
public class DirectoryIndexer extends Dependable implements Loadable {
    private final Bus<EventType> bus;
    @Setter
    @Dependency
    private SoundFileConverter converter;
    @Setter(AccessLevel.PRIVATE)
    @Dependency
    private Set<String> indexedPaths;

    @Override
    public void load() {
        bus.subscribingFor(RET_SOUND_FILE_CONVERTER).accept(this::setConverter).subscribe();
        bus.subscribingFor(DATA_INDEX_GET_DIRECTORIES).accept(this::setIndexedPaths).subscribe();
    }

    @Override
    public void afterDependenciesSet() {
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
        File[] content = Optional.ofNullable(file.listFiles()).orElse(new File[]{});
        return Arrays.stream(content)
              .flatMap(f -> getFilesFromDirectory(f).stream())
              .collect(Collectors.toList());
    }

    private void indexCatalogue(File file) {
        List<File> files = getFilesFromDirectory(file)
                .stream().filter(f -> !f.isHidden()).collect(Collectors.toList());
        List<SoundFile> soundFiles = new ArrayList<>();
        String convertPath = file.getParent() + "/";

        bus.message(DATA_INDEXING_AMOUNT).withContent(files.size()).send();
        for (int x = 0; x < files.size(); x++) {
            if (x % 10 == 0) {
                bus.message(DATA_INDEXING_PROGRESS).withContent(x).send();
            }
            soundFiles.add(converter.from(files.get(x), convertPath));
        }

        String directoryPath = normalizePath(file.getPath());
        String indexingPath = indexedPaths.stream().filter(directoryPath::contains).findAny().orElse("");
        DataQuery query = DataQuery
              .queryFor(SupportedField.PATH, "*" + directoryPath.replaceFirst(indexingPath, "") + "/*", true)
              .and(SupportedField.INDEXED_PATH, indexingPath, false);

        bus.message(DATA_REMOVE_PATH)
              .withContent(query)
              .onResponse(() -> {
                  bus.message(DATA_INDEX_LIST).withContent(soundFiles).send();
                  bus.message(DATA_QUERY).withContent(query).withResponse(DATA_QUERY_RESULT_FOR_CONTENT_VIEW).send();
              })
              .send();
    }
}
