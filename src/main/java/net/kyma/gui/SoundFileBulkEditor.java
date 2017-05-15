package net.kyma.gui;

import javafx.scene.control.TextField;
import net.kyma.dm.MetadataField;
import net.kyma.dm.SoundFile;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class SoundFileBulkEditor extends SoundFileEditor {
    private Collection<SoundFile> soundFiles;
    private Set<String> values;

    @Override
    public void init() {
        super.init();
        values = new HashSet<>();
    }

    public void showEditor(Collection<SoundFile> soundFiles) {
        this.soundFiles = soundFiles;
        fields.forEach(this::fillField);
        window.show();
    }

    private void fillField(MetadataField field, TextField textField) {
        values.clear();
        soundFiles.forEach(s -> values.add(field.getGetter().apply(s)));
        if (values.size() == 1) {
            textField.setText(values.iterator().next());
            textField.getStyleClass().remove("text-field-with-different-values");
        } else {
            textField.getStyleClass().add("text-field-with-different-values");
        }
    }
}
