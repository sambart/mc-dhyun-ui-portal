package com.dhyun.portal.plugins;

import com.dhyun.portal.plugins.impl.VolumeCategoryImpl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryManager {

    protected final Map<String, VolumeCategoryImpl> categories;

    public CategoryManager() {
        categories = new ConcurrentHashMap<>();
    }

    public void addCategory(VolumeCategoryImpl category) {
        categories.put(category.getId(), category);
    }

    public void removeCategory(String categoryId) {
        categories.remove(categoryId);
    }

    public Collection<VolumeCategoryImpl> getCategories() {
        return categories.values();
    }
}
