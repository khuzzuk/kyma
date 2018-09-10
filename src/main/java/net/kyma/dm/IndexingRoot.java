package net.kyma.dm;

import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexingRoot implements Comparable<IndexingRoot> {
    private static final IndexingRoot webDownloadsPath = new IndexingRoot("Imported from youtube");
    private String name;

    public static IndexingRoot forPathOnDisk(String path) {
        IndexingRoot indexingRoot = new IndexingRoot();
        indexingRoot.setName(path.substring(0, path.length() - 1));
        return indexingRoot;
    }

    public static IndexingRoot forWebDownloads() {
        return webDownloadsPath;
    }

    public String toPathRepresentation() {
        return name.replace("\\", "\\\\");
    }

    public String representation() {
        return name;
    }

    @Override
    public int compareTo(IndexingRoot o) {
        return name.compareTo(o.name);
    }
}
