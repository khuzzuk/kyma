package net.kyma.data;

import static net.kyma.EventType.DATA_INDEXING_AMOUNT;
import static net.kyma.EventType.DATA_INDEXING_FINISH;
import static net.kyma.EventType.DATA_INDEXING_PROGRESS;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_INDEX_LIST;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
import pl.khuzzuk.messaging.Bus;

@AllArgsConstructor
public class DirectoryIndexer implements Loadable {
    private Bus<EventType> bus;
    private SoundFileConverter converter;

    @Override
    public void load() {
        bus.<File>setReaction(DATA_INDEX_DIRECTORY, d -> bus.send(DATA_INDEX_LIST, indexCatalogue(d)));
    }

    private List<File> getFilesFromDirectory(File file) {
        if (file.isFile()) {
            String name = file.getName();
            if (name.contains(".") &&
                    Format.supportedFormats.contains(name.substring(name.lastIndexOf(".")).toLowerCase())) {
                return Collections.singletonList(file);
            } else {
                return Collections.emptyList();
            }
        }
        Optional<File[]> content = Optional.ofNullable(file.listFiles());
        return Arrays.stream(content.orElse(new File[]{})).flatMap(f -> getFilesFromDirectory(f).stream())
                .collect(Collectors.toList());
    }

    private Collection<SoundFile> indexCatalogue(File file) {
        List<File> files = getFilesFromDirectory(file)
                .stream().filter(f -> !f.isHidden()).collect(Collectors.toList());
        Collection<SoundFile> soundFiles = new ArrayList<>();
        bus.send(DATA_INDEXING_AMOUNT, files.size());
        for (int x = 0; x < files.size(); x++) {
            if (x % 10 == 0) {
                bus.send(DATA_INDEXING_PROGRESS, x);
            }
            soundFiles.add(converter.from(files.get(x),
                    file.getPath().substring(0, file.getPath().length() - file.getName().length())));
        }
        bus.send(DATA_INDEXING_FINISH);
        return soundFiles;
    }
}
