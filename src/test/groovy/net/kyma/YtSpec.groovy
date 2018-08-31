package net.kyma

import net.kyma.player.Mp3InputPlayerJLayer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification

class YtSpec extends Specification {
    def "yt test"() {
        given:
        String url = "https://www.youtube.com/watch?v=uOiohuMkKD8"

        when:
        Document doc = Jsoup.connect(url).get()

        def adapt = doc.getElementById("player").getElementsByTag("script").get(1).toString()
        def mp4a = adapt.indexOf('mp4a', adapt.indexOf('"adaptive_fmts"'))
        def equals = adapt.indexOf('url=', mp4a)
        def linkEnd = adapt.indexOf('\\u0026', equals)
        def substring = adapt.substring(equals + 4, linkEnd)
        def url1 = new URL(substring.replace("%3A", ":").replace("%2F", "/"))

        HttpURLConnection connection = (HttpURLConnection) url1.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.110 Safari/537.3");
        connection.connect()
        Mp3InputPlayerJLayer playerJLayer = new Mp3InputPlayerJLayer(null, null, connection.inputStream)
        println 'start'
        playerJLayer.start()

        then:
        playerJLayer != null
        println 'finish'
    }
}
