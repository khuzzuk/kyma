package net.kyma

import javafx.application.Platform
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.control.TreeView
import javafx.stage.Stage
import net.kyma.dm.Rating
import net.kyma.dm.SoundFile
import net.kyma.dm.SupportedField
import net.kyma.gui.controllers.ControllerDistributor
import net.kyma.gui.controllers.ManagerPaneController
import net.kyma.gui.tree.RootElement
import net.kyma.player.Format
import pl.khuzzuk.messaging.Bus
import spock.lang.Shared

import java.lang.reflect.Field
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import static net.kyma.EventType.*
import static org.awaitility.Awaitility.await

class IndexingFeatureSpec extends FxmlTestHelper {
    @Shared
    private Bus<EventType> bus
    @Shared
    private ControllerDistributor controllerDistributor

    //ManagerPaneController
    @Shared
    private RootElement fileListRootElement
    @Shared
    private Collection<String> moodSuggestions
    @Shared
    private TreeView<String> filesList
    @Shared
    private TableView<SoundFile> contentView

    //test properties
    @Shared
    private AtomicBoolean indexed = new AtomicBoolean(false)
    @Shared
    private PropertyContainer<Map<String, Collection<String>>> refreshedPaths = new PropertyContainer<>()
    @Shared
    private PropertyContainer<Set<String>> distinctMood = new PropertyContainer<>()

    private static indexDir = new File("test_index/")
    private static soundFilesDir = new File("test_files").getAbsoluteFile()
    private static Field fileListField = ManagerPaneController.class.getDeclaredField('filesList')
    private static Field contentViewField = ManagerPaneController.class.getDeclaredField('contentView')
    private static Field moodFilterField = ManagerPaneController.class.getDeclaredField('moodFilter')

    void setupSpec() {
        Platform.startup({})
        bus = Manager.createBus()

        bus.subscribingFor(RET_CONTROLLER_DISTRIBUTOR)
                .accept({ controllerDistributor = it as ControllerDistributor })
                .subscribe()

        Manager.bus = bus
        Manager.prepareApp("test_index/", bus)
        Thread.sleep(1000)
        await().atMost(2000, TimeUnit.MILLISECONDS)
                .until({ Manager.mainWindow != null && controllerDistributor != null })

        Platform.runLater({ Manager.mainWindow.initMainWindow(new Stage()) })
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ checkControllers() })

        prepareProperties()
    }

    boolean checkControllers() {
        fileListField.setAccessible(true)
        moodFilterField.setAccessible(true)
        contentViewField.setAccessible(true)

        ManagerPaneController managerPaneController = controllerDistributor.call(ManagerPaneController.class) as ManagerPaneController
        if (managerPaneController == null) false

        filesList = fileListField.get(managerPaneController) as TreeView<String>
        if (this.filesList == null) false

        contentView = contentViewField.get(managerPaneController) as TableView<SoundFile>
        if (this.contentView == null) false

        fileListRootElement = this.filesList.getRoot() as RootElement
        if (fileListRootElement == null) false

        moodSuggestions = (moodFilterField.get(managerPaneController) as ListView<String>)?.items
        if (moodSuggestions == null) false

        true
    }

    void prepareProperties() {
        bus.subscribingFor(DATA_REFRESH_PATHS)
                .accept({ refreshedPaths.value = it as Map<String, Collection<String>> })
                .subscribe()
        bus.subscribingFor(DATA_SET_DISTINCT_MOOD).accept({ distinctMood.value = it as Set<String> }).subscribe()
    }

    void cleanupSpec() {
        bus.message(CLOSE).send()
        Thread.sleep(2000)
        Arrays.stream(indexDir.listFiles()).forEach({ it.delete() })
        indexDir.delete()
    }

    void indexTestFiles() {
        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).onResponse({ indexed.set(true) }).send()
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ refreshedPaths.value?.size() == 1 })
    }

    void cleanIndex() {
        bus.message(DATA_INDEX_CLEAN).send()
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ refreshedPaths.value?.size() == 0 })
    }

    def 'index example files'() {
        when:
        indexTestFiles()

        then:
        await().atMost(1000, TimeUnit.MILLISECONDS).until(
                { refreshedPaths.hasValue() && refreshedPaths.value.size() == 1 })
        def indexingPath = ++refreshedPaths.value.keySet().iterator()
        def expectedIndexingPath = soundFilesDir.getAbsolutePath().substring(0, soundFilesDir.getAbsolutePath().lastIndexOf(File.separator) + 1).replace(File.separator, '/')
        indexingPath == expectedIndexingPath

        cleanup:
        cleanIndex()
    }

    def 'check reindexing'() {
        when:
        indexTestFiles()
        bus.message(DATA_INDEX_GET_DISTINCT).withResponse(DATA_SET_DISTINCT_MOOD).withContent(SupportedField.MOOD).send()

        then:
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ distinctMood.hasValue() })
        distinctMood.value.size() == 2
        distinctMood.value.contains('mp3 mood')
        distinctMood.value.contains('flac mood')

        and: then:
        await().atMost(1000, TimeUnit.MILLISECONDS).until({ moodSuggestions.size() == 3 })
        moodSuggestions.contains('mp3 mood')
        moodSuggestions.contains('flac mood')

        cleanup:
        cleanIndex()
    }

    def 'check if reindex can cleanup paths'() {
        given:
        indexTestFiles()

        when:
        File renamed = new File('hide_files')
        soundFilesDir.renameTo(renamed.getAbsolutePath())
        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).send()

        then:
        await().atMost(2000, TimeUnit.MILLISECONDS).until(
                { refreshedPaths.value.size() == 0 })

        cleanup:
        renamed?.renameTo soundFilesDir.getAbsolutePath()
        cleanIndex()
    }

    def 'click on file list element should result in showing files in content view'() {
        given:
        indexTestFiles()
        ManagerPaneController controller = controllerDistributor.call(ManagerPaneController.class) as ManagerPaneController
        Platform.runLater({ contentView.getItems().clear() })
        filesList.getRoot().expanded = true
        filesList.getRoot().getChildren().forEach({ it.expanded = true })
        filesList.selectionModel.select(2)

        when:
        contentView != null
        fireEventOn(filesList, 0, 0)
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.getItems().size() == 2
        def soundFiles = contentView.getItems().sorted()
        def flacFile = soundFiles.get(0)

        flacFile.format == Format.FLAC
        def expectedIndexingPath = ++refreshedPaths.value.keySet().iterator()

        flacFile.path == expectedIndexingPath + 'test_files/test.flac'
        flacFile.fileName == 'test.flac'
        flacFile.indexedPath == expectedIndexingPath
        flacFile.title == 'flac title'
        flacFile.rate == Rating.TWO_HALF
        flacFile.date == '2018'
        flacFile.album == 'flac album'
        flacFile.albumArtist == 'flac album artist'
        flacFile.artist == 'flac artist'
        flacFile.composer == 'flac composer'
        flacFile.conductor == 'flac conductor'
        flacFile.country == 'flac country'
        flacFile.custom1 == 'flac custom1'
        flacFile.custom2 == 'flac custom2'
        flacFile.custom3 == 'flac custom3'
        flacFile.custom4 == 'flac custom4'
        flacFile.custom5 == 'flac custom5'
        flacFile.discNo == 'flac discNo'
        flacFile.genre == 'flac genre'
        flacFile.group == 'flac group'
        flacFile.instrument == 'flac instrument'
        flacFile.mood == 'flac mood'
        flacFile.movement == 'flac movement'
        flacFile.occasion == 'flac occasion'
        flacFile.opus == 'flac opus'
        flacFile.orchestra == 'flac orchestra'
        flacFile.quality == 'flac quality'
        flacFile.ranking == 'flac ranking'
        flacFile.tempo == 'flac tempo'
        flacFile.tonality == 'flac tonality'
        flacFile.track == 'flac track'
        flacFile.work == 'flac work'
        flacFile.workType == 'flac workType'
        flacFile.counter == 0

        cleanup:
        cleanIndex()
    }
}
