module kyma {
    requires static lombok;
    requires MessageBus;
    requires org.apache.lucene.core;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.apache.commons.lang3;
    requires java.desktop;
    requires tritonus.share;
    requires jaudiotagger;
    requires com.fasterxml.jackson.databind;
    requires jflac;
    requires org.apache.logging.log4j;
    requires jaad;
    requires mp3spi;
    requires org.controlsfx.controls;
    requires java.logging;
    requires org.apache.logging.log4j.core;

  exports net.kyma;
    exports net.kyma.properties;
    exports net.kyma.dm;
}