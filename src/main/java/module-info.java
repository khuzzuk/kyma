module kyma {
    requires static lombok;
    requires MessageBus;
    requires lucene.core;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.apache.commons.lang3;
    requires java.desktop;
    requires tritonus.share;
    requires jaudiotagger;
    requires functional.logic;
    requires com.fasterxml.jackson.databind;
    requires jflac;
    requires org.apache.logging.log4j;
    requires jaad;
    requires mp3spi;

    exports net.kyma;
    exports net.kyma.properties;
    exports net.kyma.dm;
}