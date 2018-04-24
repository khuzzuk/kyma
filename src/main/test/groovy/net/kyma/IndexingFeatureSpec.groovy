package net.kyma

import javafx.application.Platform
import pl.khuzzuk.messaging.Bus
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import static net.kyma.EventType.CLOSE
import static net.kyma.EventType.DATA_INDEX_DIRECTORY
import static net.kyma.EventType.DATA_REFRESH_PATHS
import static org.awaitility.Awaitility.await

class IndexingFeatureSpec extends Specification {
    private indexDir = new File("test_index/")

    def "index example file"() {
        given:
        Platform.startup({})
        def bus = Manager.createBus()
        Manager.prepareApp("test_index/", bus)

        def soundFilesDir = new File("test_files")

        def indexed = new AtomicBoolean(false)
        PropertyContainer<TreeSet<String>> property = new PropertyContainer<>()

        bus.subscribingFor(DATA_REFRESH_PATHS).accept({property.setValue(it as TreeSet<String>)}).subscribe()

        when:
        Thread.sleep(1000)
        println 'START TESTS'

        bus.message(DATA_INDEX_DIRECTORY).withContent(soundFilesDir).onResponse({indexed.set(true)}).send()

        then:
        await().atMost(2000, TimeUnit.MILLISECONDS).until({indexed.get()})
        Thread.sleep(1000)
        property.getValue().size() == 1

        cleanup:
        println "CLEANING"
        clean(bus)
    }

    private void clean(Bus<EventType> bus) {
        bus.message(CLOSE).send()
        Thread.sleep(2000)
        Arrays.stream(indexDir.listFiles()).forEach({it.delete()})
        indexDir.delete()
    }
}
