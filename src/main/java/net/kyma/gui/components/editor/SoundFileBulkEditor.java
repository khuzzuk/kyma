package net.kyma.gui.components.editor;

import javafx.scene.control.TextField;
import net.kyma.EventType;
import net.kyma.dm.SoundFile;
import net.kyma.dm.SupportedField;
import org.apache.commons.lang3.StringUtils;
import pl.khuzzuk.messaging.Bus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoundFileBulkEditor extends SoundFileEditor {
    private Collection<SoundFile> soundFiles;
    private Set<String> values;

    public SoundFileBulkEditor(Bus<EventType> bus)
    {
        super(bus);
    }

    @Override
    public void init(Map<SupportedField, Collection<String>> suggestions) {
        super.init(suggestions);
        values = new HashSet<>();
    }

    public void showEditor(Collection<SoundFile> soundFiles) {
        this.soundFiles = soundFiles;
        fields.forEach(this::fillField);
        window.show();
    }

    private void fillField(SupportedField field, TextField textField) {
        values.clear();
        soundFiles.forEach(s -> values.add(field.getGetter().apply(s)));
        if (values.size() == 1) {
            textField.setText(values.iterator().next());
            textField.getStyleClass().remove("text-field-with-different-values");
        } else {
            textField.getStyleClass().add("text-field-with-different-values");
        }
    }

    @Override
    void saveSoundFile() {
        fields.forEach((key, value) -> {
            if (!StringUtils.isBlank(value.getText())) {
                soundFiles.forEach(s -> key.getSetter().accept(s, value.getText()));
            }
        });
        bus.message(EventType.DATA_STORE_LIST).withContent(soundFiles).send();
        window.hide();
    }
}
