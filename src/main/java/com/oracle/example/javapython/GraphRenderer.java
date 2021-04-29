/*
 * Copyright (c) 2021 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package com.oracle.example.javapython;

import java.io.InputStream;

/**
 * This interface demonstrates that we can adapt a Python object to a Java interface and have
 * auto-completion in our Java IDE. However, there is still no static type safety, since Python
 * objects are dynamic and the interface methods are dispatched to Python at runtime.
 *
 * @see Main.createPygalRenderer
 */
interface GraphRenderer {
    /**
     * Render the {@code function} to an SVG and return an {@link InputStream} of that SVG.
     */
    InputStream render(String function, int steps);
}
