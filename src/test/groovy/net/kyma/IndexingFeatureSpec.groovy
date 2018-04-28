package net.kyma

import javafx.application.Platform
import pl.khuzzuk.messaging.Bus
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import static net.kyma.EventType.*
import static org.awaitility.Awaitility.await

class IndexingFeatureSpec extends Specification {
    @Shared
    private Bus<EventType> bus

    private static indexDir = new File("test_index/")
    private static soundFilesDir = new File("test_files").getAbsoluteFile()

    void setupSpec() {
        Platform.startup({})
        bus = Manager.createBus()
        Manager.prepareApp("test_index/", bus)
        Thread.sleep(1000)
    }

    void cleanupSpec() {
        println "\n\n\n\nCLEANING"
        bus.message(CLOSE).send()
        Thread.sleep(2000)
        Arrays.stream(indexDir.listFiles()).forEach({it.delete()})
        indexDir.delete()
    }

    def 'index example file'() {
        given:
        def indexed = new AtomicBoolean(false)
        PropertyContainer<Map<String, Collection<String>>> refreshedPaths = new PropertyContainer<>()
        PropertyContainer<Set<String>> distinctMood = new PropertyContainer<>()

        bus.subscribingFor(DATA_REFRESH_PATHS)
                .accept({refreshedPaths.value = it as Map<String, Collection<String>>})
                .subscribe()
        bus.subscribingFor(DATA_SET_DISTINCT_MOOD).accept({distinctMood.value = it as Set<String>})

        when:
        println '\n\n\n\nSTART TESTS\t\tindex file'

        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).onResponse({indexed.set(true)}).send()

        then:
        await().atMost(2000, TimeUnit.MILLISECONDS).until({indexed.get()})
        await().atMost(1000, TimeUnit.MILLISECONDS).until(
                {refreshedPaths.hasValue() && refreshedPaths.value.size() == 1})
        def indexingPath = ++refreshedPaths.value.keySet().iterator()
        def expectedIndexingPath = soundFilesDir.getAbsolutePath().substring(0, soundFilesDir.getAbsolutePath().lastIndexOf(File.separator) + 1).replace(File.separator, '/')
        indexingPath == expectedIndexingPath

        and:
        File renamed = new File('hide_files')
        soundFilesDir.renameTo(renamed.getAbsolutePath())
        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).onResponse({indexed.set(false)}).send()

        then:
        await().atMost(2000, TimeUnit.MILLISECONDS).until({!indexed.get()})
        await().atMost(2000, TimeUnit.MILLISECONDS).until(
                {refreshedPaths.value.size() == 0})

        cleanup:
        renamed?.renameTo soundFilesDir.getAbsolutePath()
    }
}
