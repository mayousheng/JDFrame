package io.github.burukeyou.dataframe.iframe.window;

import io.github.burukeyou.dataframe.iframe.item.FI2;

import java.util.List;

public interface SupplierFunction<T,V> {

    List<FI2<T, V>> get(List<T> windowList);
}
