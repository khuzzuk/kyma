package net.kyma.data;

import static net.kyma.EventType.DATA_INDEXING_AMOUNT;
import static net.kyma.EventType.DATA_INDEXING_FINISH;
import static net.kyma.EventType.DATA_INDEXING_PROGRESS;
import static net.kyma.EventType.DATA_INDEX_DIRECTORY;
import static net.kyma.EventType.DATA_INDEX_LIST;
import static net.kyma.EventType.RET_SOUND_FILE_CONVERTER;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.kyma.EventType;
import net.kyma.Loadable;
import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
import pl.khuzzuk.messaging.Bus;

@RequiredArgsConstructor
public class DirectoryIndexer implements Loadable {
    private final Bus<EventType> bus;
    private SoundFileConverter converter;

    @Override
    public void load() {
        bus.subscribingFor(RET_SOUND_FILE_CONVERTER).accept(this::setConverter).subscribe();
    }

    private void setConverter(SoundFileConverter converter)
    {
        this.converter = converter;
        bus.subscribingFor(DATA_INDEX_DIRECTORY).<File>accept(d -> bus
              .message(DATA_INDEX_LIST)
              .withContent(indexCatalogue(d))
              .send()).subscribe();
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
        bus.message(DATA_INDEXING_AMOUNT).withContent(files.size()).send();
        for (int x = 0; x < files.size(); x++) {
            if (x % 10 == 0) {
                bus.message(DATA_INDEXING_PROGRESS).withContent(x).send();
            }
            soundFiles.add(converter.from(files.get(x),
                    file.getPath().substring(0, file.getPath().length() - file.getName().length())));
        }
        bus.message(DATA_INDEXING_FINISH).send();
        return soundFiles;
    }
}
