package net.kyma.dm;

import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexingRoot implements Comparable<IndexingRoot> {
    private String name;

    public String getPath(String fullPath) {
        return fullPath.substring(name.length() + 1);
    }

    @Override
    public int compareTo(IndexingRoot o) {
        return name.compareTo(o.name);
    }
}
