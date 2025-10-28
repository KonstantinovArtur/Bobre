// ru.forge.blacksmith_shop.fav.Favorites
package ru.forge.blacksmith_shop.fav;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Favorites {
    private final Set<Integer> productIds = new LinkedHashSet<>();

    public Set<Integer> getItems() { return productIds; }
    public void add(Integer productId) { productIds.add(productId); }
    public void remove(Integer productId) { productIds.remove(productId); }
    public boolean contains(Integer productId) { return productIds.contains(productId); }
    public void clear() { productIds.clear(); }
    public int size() { return productIds.size(); }
}
