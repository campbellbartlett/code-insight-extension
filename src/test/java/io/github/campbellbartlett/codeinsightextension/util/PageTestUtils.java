package io.github.campbellbartlett.codeinsightextension.util;

import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageImpl;
import com.atlassian.bitbucket.util.PageRequestImpl;

import java.util.ArrayList;
import java.util.List;

public class PageTestUtils {

    private PageTestUtils() {
        // Private constructor for static utility class
    }

    public static <T> Page<T> getEmptyPage() {
        return new PageImpl<>(new PageRequestImpl(0, 100), 0, new ArrayList<>(), true);
    }

    public static <T> Page<T> getPageWithItems(List<T> items) {
        return new PageImpl<>(new PageRequestImpl(0, 100), items.size(), items, true);
    }
}
