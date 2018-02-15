package net.kyma.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import net.kyma.dm.SoundFile;
import net.kyma.player.Format;
import pl.khuzzuk.messaging.Bus;

public class DirectoryIndexer {
    @Inject
    private Bus bus;
    @Inject
    @Named("messages")
    private Properties messages;
    @Inject
    private SoundFileConverter converter;

    public void init() {
        bus.<File>setReaction(messages.getProperty("data.index.directory"), d -> bus.send(messages.getProperty("data.index.list"), indexCatalogue(d)));
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
        bus.send(messages.getProperty("data.index.gui.amount"), files.size());
        for (int x = 0; x < files.size(); x++) {
            if (x % 10 == 0) {
                bus.send(messages.getProperty("data.index.gui.progress"), x);
            }
            soundFiles.add(converter.from(files.get(x),
                    file.getPath().substring(0, file.getPath().length() - file.getName().length())));
        }
        bus.send(messages.getProperty("data.index.gui.finish"));
        return soundFiles;
    }
}
