package net.kyma

import javafx.application.Platform
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.stage.Stage
import net.kyma.dm.RateTagUpdateRequest
import net.kyma.dm.Rating
import net.kyma.dm.SoundFile
import net.kyma.dm.SupportedField
import net.kyma.gui.controllers.ContentView
import net.kyma.gui.controllers.ControllerDistributor
import net.kyma.gui.controllers.ManagerPaneController
import net.kyma.gui.controllers.ManagerPaneControllerTestHelper
import net.kyma.player.Format
import pl.khuzzuk.messaging.Bus
import spock.lang.Shared
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static net.kyma.EventType.*
import static org.awaitility.Awaitility.await

@Stepwise
class IndexingFeatureSpec extends FxmlTestHelper {
    private static final int WAITING_SECONDS = 5
    @Shared
    private Bus<EventType> bus
    @Shared
    private ControllerDistributor controllerDistributor

    //ManagerPaneController
    @Shared
    private ManagerPaneControllerTestHelper managerHelper
    @Shared
    private ContentView contentViewController

    //test properties
    @Shared
    private PropertyContainer<Map<String, Collection<String>>> refreshedPaths = new PropertyContainer<>()
    @Shared
    private PropertyContainer<Collection<Integer>> refreshedPathsSizes = new PropertyContainer<>()
    @Shared
    private PropertyContainer<Set<String>> distinctMood = new PropertyContainer<>()
    @Shared
    private PropertyContainer<SoundFile> indexedSoundFile = new PropertyContainer<>()

    private static indexDir = new File("test_index/")
    private static soundFilesDir = new File("copied_files").getAbsoluteFile()
    private static sourceDir = new File("test_files").getAbsoluteFile()

    void setupSpec() {
        Platform.startup({})
        bus = Manager.createBus()

        bus.subscribingFor(RET_CONTROLLER_DISTRIBUTOR)
                .accept({ controllerDistributor = it as ControllerDistributor })
                .subscribe()

        Manager.bus = bus
        Manager.prepareApp("test_index/", bus)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS)
                .until({ Manager.mainWindow != null && controllerDistributor != null })

        Platform.runLater({ Manager.mainWindow.initMainWindow(new Stage()) })
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ checkControllers() })

        prepareProperties()
    }

    boolean checkControllers() {
        managerHelper = new ManagerPaneControllerTestHelper()
        def managerPaneController = controllerDistributor.call(ManagerPaneController.class) as ManagerPaneController
        managerHelper.retrieveFields(managerPaneController)
        if (!managerHelper.isValid()) {
            return false
        }

        contentViewController = controllerDistributor.call(ContentView.class) as ContentView
        if (contentViewController == null) return false

        if (GuiPrivateFields.contentViewSuggestions == null) return false
        if (GuiPrivateFields.contentViewSoundFileEditor == null) return false

        true
    }

    void prepareProperties() {
        IndexingFinishReporter.setPropertyContainer(indexedSoundFile)
        refreshedPaths.value = new HashMap<>()
        refreshedPathsSizes.value = new ArrayList<>()
        bus.subscribingFor(DATA_REFRESH_PATHS)
                .accept({
            refreshedPaths.value.putAll(it as Map<String, Collection<String>>)
            refreshedPathsSizes.value.add((it as Map<String, Collection<String>>).size())
        })
                .subscribe()
        bus.subscribingFor(DATA_SET_DISTINCT_MOOD).accept({ distinctMood.value = it as Set<String> }).subscribe()
    }

    void cleanupSpec() {
        cleanIndex()
        bus.message(CLOSE).send()
        Thread.sleep(2200)
        Arrays.stream(indexDir.listFiles()).forEach({ it.delete() })
        indexDir.delete()
    }

    void setup() {
        cleanIndex()
        refreshedPaths.value = new HashMap<>()
        distinctMood.value = null
        managerHelper.contentView.items.clear()
    }

    void indexTestFiles() {
        Files.copy(sourceDir.toPath(), soundFilesDir.toPath())
        Arrays.stream(sourceDir.listFiles()).forEach({ Files.copy(it.toPath(), Paths.get(soundFilesDir.name, it.name)) })
        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).send()
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ refreshedPaths.value.size() == 2 })
    }

    void cleanIndex() {
        bus.message(DATA_INDEX_CLEAN).send()
        await().atMost(WAITING_SECONDS*2, TimeUnit.SECONDS).until({ refreshedPathsSizes.value.contains(0) })
        if (soundFilesDir.exists())
            Arrays.stream(soundFilesDir.listFiles()).forEach({ it.delete() })
        soundFilesDir.delete()
    }

    def 'index example files'() {
        when:
        indexTestFiles()

        then:
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until(
                { refreshedPaths.hasValue() && refreshedPaths.value.size() == 2 })
        def expectedIndexingPath = soundFilesDir.getAbsolutePath().substring(0, soundFilesDir.getAbsolutePath().lastIndexOf(File.separator) + 1).replace(File.separator, '/')
        refreshedPaths.value.containsKey(expectedIndexingPath)
    }

    def 'check reindexing'() {
        given:
        indexTestFiles()

        when:
        bus.message(DATA_INDEX_GET_DISTINCT).withResponse(DATA_SET_DISTINCT_MOOD).withContent(SupportedField.MOOD).send()
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ distinctMood.hasValue() })

        then:
        distinctMood.value.size() == 2
        distinctMood.value.contains(MetadataValues.mp3Mood)
        distinctMood.value.contains('flac mood')

        def suggestions = GuiPrivateFields.contentViewSuggestions
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS)
                .until({ suggestions.get(SupportedField.MOOD).size() == 2 })
        suggestions.get(SupportedField.MOOD).containsAll([MetadataValues.mp3Mood, MetadataValues.flacMood])
    }

    def 'check if reindex can cleanup paths'() {
        given:
        indexTestFiles()

        when:
        File renamed = new File('hide_files')
        soundFilesDir.renameTo(renamed.getAbsolutePath())
        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).send()

        then:
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until(
                { refreshedPathsSizes.value.contains(0) })

        cleanup:
        renamed?.renameTo soundFilesDir.getAbsolutePath()
    }

    def 'click on file list element should result in showing files in content view'() {
        given:
        indexTestFiles()
        Platform.runLater({ managerHelper.contentView.getItems().clear() })

        when:
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ !managerHelper.contentView.items.isEmpty() })

        then:
        managerHelper.contentView.getItems().size() == 2
        def soundFiles = managerHelper.contentView.getItems().sorted()
        def flacFile = soundFiles.get(0)
        def mp3File = soundFiles.get(1)

        flacFile.format == Format.FLAC
        refreshedPaths.value.keySet().stream().anyMatch({ (flacFile.indexedPath == it) })
        flacFile.fileName == MetadataValues.flacFileName
        flacFile.title == MetadataValues.flacTitle
        flacFile.rate == MetadataValues.flacRate
        flacFile.date == MetadataValues.flacDate
        flacFile.album == MetadataValues.flacAlbum
        flacFile.albumArtist == MetadataValues.flacAlbumArtist
        flacFile.artist == MetadataValues.flacArtist
        flacFile.composer == MetadataValues.flacComposer
        flacFile.conductor == MetadataValues.flacConductor
        flacFile.country == MetadataValues.flacCountry
        flacFile.custom1 == MetadataValues.flacCustom1
        flacFile.custom2 == MetadataValues.flacCustom2
        flacFile.custom3 == MetadataValues.flacCustom3
        flacFile.custom4 == MetadataValues.flacCustom4
        flacFile.custom5 == MetadataValues.flacCustom5
        flacFile.discNo == MetadataValues.flacDiscNo
        flacFile.genre == MetadataValues.flacGenre
        flacFile.group == MetadataValues.flacGroup
        flacFile.instrument == MetadataValues.flacInstrument
        flacFile.mood == MetadataValues.flacMood
        flacFile.movement == MetadataValues.flacMovement
        flacFile.occasion == MetadataValues.flacOccasion
        flacFile.opus == MetadataValues.flacOpus
        flacFile.orchestra == MetadataValues.flacOrchestra
        flacFile.quality == MetadataValues.flacQuality
        flacFile.ranking == MetadataValues.flacRanking
        flacFile.tempo == MetadataValues.flacTempo
        flacFile.tonality == MetadataValues.flacTonality
        flacFile.track == MetadataValues.flacTrack
        flacFile.work == MetadataValues.flacWork
        flacFile.workType == MetadataValues.flacWorkType
        flacFile.counter == MetadataValues.flacCounter

        mp3File.format == Format.MP3
        refreshedPaths.value.keySet().stream().anyMatch({ (mp3File.indexedPath == it) })
        mp3File.fileName == MetadataValues.mp3FileName
        mp3File.title == MetadataValues.mp3Title
        mp3File.rate == MetadataValues.mp3Rate
        mp3File.date == MetadataValues.mp3Date
        mp3File.album == MetadataValues.mp3Album
        mp3File.albumArtist == MetadataValues.mp3AlbumArtist
        mp3File.artist == MetadataValues.mp3Artist
        mp3File.composer == MetadataValues.mp3Composer
        mp3File.conductor == MetadataValues.mp3Conductor
        mp3File.country == MetadataValues.mp3Country
        mp3File.custom1 == MetadataValues.mp3Custom1
        mp3File.custom2 == MetadataValues.mp3Custom2
        mp3File.custom3 == MetadataValues.mp3Custom3
        mp3File.custom4 == MetadataValues.mp3Custom4
        mp3File.custom5 == MetadataValues.mp3Custom5
        mp3File.discNo == MetadataValues.mp3DiscNo
        mp3File.genre == MetadataValues.mp3Genre
        mp3File.group == MetadataValues.mp3Group
        mp3File.instrument == MetadataValues.mp3Instrument
        mp3File.mood == MetadataValues.mp3Mood
        mp3File.movement == MetadataValues.mp3Movement
        mp3File.occasion == MetadataValues.mp3Occasion
        mp3File.opus == MetadataValues.mp3Opus
        mp3File.orchestra == MetadataValues.mp3Orchestra
        mp3File.quality == MetadataValues.mp3Quality
        mp3File.ranking == MetadataValues.mp3Ranking
        mp3File.tempo == MetadataValues.mp3Tempo
        mp3File.tonality == MetadataValues.mp3Tonality
        mp3File.track == MetadataValues.mp3Track
        mp3File.work == MetadataValues.mp3Work
        mp3File.workType == MetadataValues.mp3WorkType
        mp3File.counter == MetadataValues.mp3Counter
    }

    def 'check modification of mp3 file mood metadata from contentView'() {
        given:
        indexTestFiles()
        def editedValue = "mp3 mood edited"
        def contentView = managerHelper.contentView

        when:
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ !contentView.items.isEmpty() })

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
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({IndexingFinishReporter.isIndexingFinished()})

        then:
        indexedSoundFile.value.mood == editedValue

        and:
        contentView.items.clear()
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.items.size() == 2
        def modifiedFile = contentView.getItems().sorted().get(1)
        modifiedFile.mood == editedValue
    }

    def 'check modification of mp3 file rating metadata by sending event'() {
        given:
        indexTestFiles()
        def editedValue = Rating.FIVE
        def contentView = managerHelper.contentView

        when:
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.items.size() == 2

        and:
        selectFirst(contentView)
        def selectedSoundFile = contentView.selectionModel.getSelectedItem()
        selectedSoundFile.rate != editedValue

        IndexingFinishReporter.reset()
        bus.message(DATA_UPDATE_REQUEST).withContent(new RateTagUpdateRequest(editedValue)).send()
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({IndexingFinishReporter.isIndexingFinished()})

        then:
        indexedSoundFile.value.rate == editedValue

        and:
        contentView.items.clear()
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ !contentView.items.isEmpty() })

        then:
        contentView.items.size() == 2

        and:
        selectBySoundFilePath(contentView, selectedSoundFile)
        def modifiedFile = contentView.selectionModel.getSelectedItem()
        modifiedFile.rate == editedValue
    }

    def 'check main filters content'() {
        given:
        indexTestFiles()

        when:
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ !managerHelper.moodFilter.items.isEmpty() })

        then:
        managerHelper.moodFilter.items.size() == 3
        managerHelper.moodFilter.items.containsAll('...', MetadataValues.mp3Mood, MetadataValues.flacMood)
    }

    def 'check main filters action'() {
        given:
        indexTestFiles()
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ managerHelper.moodFilter.items.size() == 3 })
        managerHelper.contentView.items.size() == 2

        when:
        select(managerHelper.moodFilter, 1)
        clickMouseOn(managerHelper.moodFilter, 10, 10)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ managerHelper.contentView.items.size() == 1 })

        then:
        managerHelper.contentView.items.get(0).fileName == MetadataValues.mp3FileName
    }

    def "add files to Playlist"() {
        given:
        indexTestFiles()
        selectFirst(managerHelper.filesList)
        clickMouseOn(managerHelper.filesList, 0, 0)
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({ managerHelper.moodFilter.items.size() == 3 })
        managerHelper.contentView.items.size() == 2

        when:
        selectFirst(GuiPrivateFields.mainContentView)
        clickMouseOn(GuiPrivateFields.mainContentView, 10, 10, 2)
        def playlistItems = GuiPrivateFields.playlistView.getItems()
        await().atMost(WAITING_SECONDS, TimeUnit.SECONDS).until({playlistItems.size() != 0})

        then:
        def soundFile = playlistItems.get(0)
        soundFile.format == Format.MP3
    }
}
