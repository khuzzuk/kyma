package net.kyma

import javafx.application.Platform
import javafx.scene.control.ListView
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableView
import javafx.scene.control.TreeView
import javafx.stage.Stage
import net.kyma.dm.RateTagUpdateRequest
import net.kyma.dm.Rating
import net.kyma.dm.SoundFile
import net.kyma.dm.SupportedField
import net.kyma.gui.SoundFileEditor
import net.kyma.gui.controllers.ContentView
import net.kyma.gui.controllers.ControllerDistributor
import net.kyma.gui.controllers.ManagerPaneController
import net.kyma.gui.tree.RootElement
import net.kyma.player.Format
import pl.khuzzuk.messaging.Bus
import spock.lang.Shared

import java.lang.reflect.Field
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

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
    @Shared
    private ContentView contentViewController
    @Shared
    private SoundFileEditor soundFileEditor

    //test properties
    @Shared
    private PropertyContainer<Map<String, Collection<String>>> refreshedPaths = new PropertyContainer<>()
    @Shared
    private PropertyContainer<Set<String>> distinctMood = new PropertyContainer<>()
    @Shared
    private PropertyContainer<SoundFile> indexedSoundFile = new PropertyContainer<>();

    private static indexDir = new File("test_index/")
    private static soundFilesDir = new File("copied_files").getAbsoluteFile()
    private static sourceDir = new File("test_files").getAbsoluteFile()
    private static Field fileListField = ManagerPaneController.class.getDeclaredField('filesList')
    private static Field contentViewField = ManagerPaneController.class.getDeclaredField('contentView')
    private static Field moodFilterField = ManagerPaneController.class.getDeclaredField('moodFilter')
    private static Field contentViewSoundFileEditor = ContentView.class.getDeclaredField('editor')

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
        contentViewSoundFileEditor.setAccessible(true)

        contentViewController = controllerDistributor.call(ContentView.class) as ContentView
        if (contentViewController == null) false

        soundFileEditor = contentViewSoundFileEditor.get(contentViewController) as SoundFileEditor
        if (this.soundFileEditor == null) false

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
        IndexingFinishReporter.setPropertyContainer(indexedSoundFile)
        bus.subscribingFor(DATA_REFRESH_PATHS)
                .accept({ refreshedPaths.value = it as Map<String, Collection<String>> })
                .subscribe()
        bus.subscribingFor(DATA_SET_DISTINCT_MOOD).accept({ distinctMood.value = it as Set<String> }).subscribe()
    }

    void cleanupSpec() {
        cleanIndex()
        bus.message(CLOSE).send()
        Thread.sleep(2000)
        Arrays.stream(indexDir.listFiles()).forEach({ it.delete() })
        indexDir.delete()
    }

    void setup() {
        cleanIndex()
        refreshedPaths.value = null
        distinctMood.value = null
        contentView.items.clear()
    }

    void indexTestFiles() {
        Files.copy(sourceDir.toPath(), soundFilesDir.toPath())
        Arrays.stream(sourceDir.listFiles()).forEach({ Files.copy(it.toPath(), Paths.get(soundFilesDir.name, it.name)) })
        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).send()
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ refreshedPaths.value?.size() == 1 })
    }

    void cleanIndex() {
        bus.message(DATA_INDEX_CLEAN).send()
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ refreshedPaths.value?.size() == 0 })
        if (soundFilesDir.exists())
            Arrays.stream(soundFilesDir.listFiles()).forEach({ it.delete() })
        soundFilesDir.delete()
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

        await().atMost(1000, TimeUnit.MILLISECONDS).until({ moodSuggestions.size() == 3 })
        moodSuggestions.contains('mp3 mood')
        moodSuggestions.contains('flac mood')
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
    }

    def 'click on file list element should result in showing files in content view'() {
        given:
        indexTestFiles()
        Platform.runLater({ contentView.getItems().clear() })
        contentView != null

        when:
        selectFirst(filesList)
        clickMouseOn(filesList, 0, 0)
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.getItems().size() == 2
        def soundFiles = contentView.getItems().sorted()
        def flacFile = soundFiles.get(0)
        def mp3File = soundFiles.get(1)

        flacFile.format == Format.FLAC
        def expectedIndexingPath = ++refreshedPaths.value.keySet().iterator()
        flacFile.path == expectedIndexingPath + soundFilesDir.name + '/test.flac'
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
        flacFile.discNo == '1'
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
        flacFile.track == '1'
        flacFile.work == 'flac work'
        flacFile.workType == 'flac wokr type'
        flacFile.counter == 0

        mp3File.format == Format.MP3
        mp3File.path == expectedIndexingPath + soundFilesDir.name + '/test.mp3'
        mp3File.fileName == 'test.mp3'
        mp3File.indexedPath == expectedIndexingPath
        mp3File.title == 'mp3Title'
        mp3File.rate == Rating.ONE
        mp3File.date == '2018'
        mp3File.album == 'mp3 album'
        mp3File.albumArtist == 'mp3 album artist'
        mp3File.artist == 'mp3 artist'
        mp3File.composer == 'mp3 composer'
        mp3File.conductor == 'mp3 conductor'
        mp3File.country == 'mp3 country'
        mp3File.custom1 == 'mp3 custom1'
        mp3File.custom2 == 'mp3 custom2'
        mp3File.custom3 == 'mp3 custom3'
        mp3File.custom4 == 'mp3 custom4'
        mp3File.custom5 == 'mp3 custom5'
        mp3File.discNo == ''
        mp3File.genre == 'mp3 genre'
        mp3File.group == 'mp3 group'
        mp3File.instrument == 'mp3 instrument'
        mp3File.mood == 'mp3 mood'
        mp3File.movement == 'mp3 movement'
        mp3File.occasion == 'mp3 occasion'
        mp3File.opus == 'mp3 opus'
        mp3File.orchestra == 'mp3 orchestra'
        mp3File.quality == 'mp3 quality'
        mp3File.ranking == 'mp3 ranking'
        mp3File.tempo == 'mp3 tempo'
        mp3File.tonality == 'mp3 tonality'
        mp3File.track == ''
        mp3File.work == 'mp3 work'
        mp3File.workType == 'mp3 workTyp'
        mp3File.counter == 0
    }

    def 'check modification of mp3 file mood metadata from contentView'() {
        given:
        indexTestFiles()
        def editedValue = "mp3 mood edited"

        when:
        selectFirst(filesList)
        clickMouseOn(filesList, 0, 0)
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.items.size() == 2

        and:
        selectFirst(contentView)

        TableColumn<SoundFile, String> column = contentView.getColumns().stream()
                .filter({ it.text.equals(SupportedField.MOOD.getName()) })
                .findAny().get() as TableColumn<SoundFile, String>
        def tableUpdateEvent = new TableColumn.CellEditEvent<SoundFile, String>(
                contentView, new TablePosition<SoundFile, String>(contentView, 1, column), null, editedValue)

        IndexingFinishReporter.reset()
        column.onEditCommit.handle(tableUpdateEvent)
        await().atMost(2000, TimeUnit.MILLISECONDS).until({IndexingFinishReporter.isIndexingFinished()})

        then:
        indexedSoundFile.value.mood == editedValue

        and:
        contentView.items.clear()
        selectFirst(filesList)
        clickMouseOn(filesList, 0, 0)
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.items.size() == 2
        def modifiedFile = contentView.getItems().sorted().get(1)
        modifiedFile.mood == editedValue
    }

    def 'check modification of mp3 file rating metadata by sending event'() {
        given:
        indexTestFiles()
        def editedValue = Rating.FIVE

        when:
        selectFirst(filesList)
        clickMouseOn(filesList, 0, 0)
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.items.size() == 2

        and:
        selectFirst(contentView)
        def selectedSoundFile = contentView.selectionModel.getSelectedItem()
        selectedSoundFile.rate != editedValue

        IndexingFinishReporter.reset()
        bus.message(DATA_UPDATE_REQUEST).withContent(new RateTagUpdateRequest(editedValue)).send()
        await().atMost(2000, TimeUnit.MILLISECONDS).until({IndexingFinishReporter.isIndexingFinished()})

        then:
        indexedSoundFile.value.rate == editedValue

        and:
        contentView.items.clear()
        selectFirst(filesList)
        clickMouseOn(filesList, 0, 0)
        await().atMost(2000, TimeUnit.MILLISECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.items.size() == 2

        and:
        selectBySoundFilePath(contentView, selectedSoundFile)
        def modifiedFile = contentView.selectionModel.getSelectedItem()
        modifiedFile.rate == editedValue
    }
}
