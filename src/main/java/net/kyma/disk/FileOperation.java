package net.kyma.disk;

import java.nio.file.Path;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter(AccessLevel.PACKAGE)
public class FileOperation {
   private Path path;
   private Runnable operation;
}
