package net.kyma

import net.kyma.dm.SoundFile
import net.kyma.player.Format

class FilesDataValidator extends FxmlTestHelper {
    void validate(SoundFile file, String prefix, Format format) {
        file.fileName == 'test.' + prefix
        file.format == format
        file.album == prefix + ' album'
        file.albumArtist == prefix + ' album artist'
        file.group == prefix + ' group'
        file.artist == prefix + ' artist'
        file.composer == prefix + ' composer'
        file.conductor == prefix + ' conductor'
        file.country == prefix + ' country'
        file.custom1 == prefix + ' custom1'
        file.custom2 == prefix + ' custom2'
        file.custom3 == prefix + ' custom3'
        file.custom4 == prefix + ' custom4'
        file.custom5 == prefix + ' custom5'
        file.date == prefix + ' custom5'
        file.discNo == prefix + ' custom5'
        file.counter == 0
    }
}
