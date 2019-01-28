package net.kyma.data;

import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.DataQuery;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import net.kyma.player.Format;
import pl.khuzzuk.messaging.Bus;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static net.kyma.EventType.*;
import static net.kyma.data.PathUtils.normalizePath;

@RequiredArgsConstructor
public class DirectoryIndexer implements Loadable {
    private final Bus<EventType> bus;
    private SoundFileConverter converter;
    private Set<String> indexedPaths;

    @Override
    public void load() {
        bus.subscribingFor(RET_SOUND_FILE_CONVERTER).accept(this::setConverter).subscribe();
        bus.subscribingFor(DATA_INDEX_GET_DIRECTORIES).accept(this::setIndexedPaths).subscribe();
    }

    private synchronized void setConverter(SoundFileConverter converter) {
        this.converter = converter;
        if (indexedPaths != null) {
            afterDependenciesSet();
        }
    }

    private synchronized void setIndexedPaths(Set<String> indexedPaths) {
        this.indexedPaths = indexedPaths;
        if (converter != null) {
            afterDependenciesSet();
        } //TODO resolve this dependency cycle
    }

    private void afterDependenciesSet() {
        bus.subscribingFor(DATA_INDEX_DIRECTORY).accept(this::indexCatalogue).subscribe();
    }

    private List<File> getFilesFromDirectory(File file) {
        if (file.isFile()) {
            return Format.isSupportingFormat(file.toPath()) ? Collections.singletonList(file) : Collections.emptyList();
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
