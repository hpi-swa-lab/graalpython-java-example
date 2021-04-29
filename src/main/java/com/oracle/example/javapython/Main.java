/*
 * Copyright (c) 2021 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package com.oracle.example.javapython;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class Main {
    public static void main(String[] args) throws Exception {
        GraphRenderer renderer = createPygalRenderer();
        Frame window = createGraphWindow(renderer);
        window.setVisible(true);
    }

    private static String PYTHON = "python";
    private static String VENV_EXECUTABLE = Main.class.getClassLoader().getResource(Paths.get("venv", "bin", "graalpython").toString()).getPath();
    private static String SOURCE_FILE_NAME = "pygalrenderer.py";

    /**
     * This creates a Python instance of the PygalRenderer type and returns it mapped to the
     * {@link GraphRenderer} interface.
     */
    static GraphRenderer createPygalRenderer() {
        Context context = Context.newBuilder(PYTHON).
            // It is a good idea to start with allowAllAccess(true) and only when everything is
            // working to start trying to reduce it. See the GraalVM docs for fine-grained
            // permissions.
            allowAllAccess(true).
            // Python virtualenvs work by setting up their initial package paths based on the
            // runtime path of the python executable. Since we are not executing from the python
            // executable, we need to set this option to what it would be
            option("python.Executable", VENV_EXECUTABLE).
            // The actual package setup only happens inside Python's "site" module. This module is
            // automatically imported when starting the Python executable, but there is an option
            // to turn this off even for the executable. To avoid accidental file system access, we
            // do not import this module by default. Setting this option to true after setting the
            // python.Executable option ensures we import the site module at startup, but only
            // within the virtualenv.
            option("python.ForceImportSite", "true").
            build();
        InputStreamReader code = new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(SOURCE_FILE_NAME));
        Source source;
        try {
            source = Source.newBuilder(PYTHON, code, SOURCE_FILE_NAME).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        context.eval(source);
        // Getting the Python PygalRenderer class is an example of how data can be shared
        // explicitly between Python and Java. It is a good idea to limit the amount of data that
        // is explicitly shared and instead use methods and their return values, similar to how one
        // would limit the visibility of classes within a Java project.
        Value pygalRendererClass = context.getPolyglotBindings().getMember("PygalRenderer");
        // Next we instantiate the Python type and cast it to a GraphRenderer. This cast will
        // always succeed, and the relevant methods will only be forwarded when invoked, so there
        // is typechecking at this point, even at runtime. The reason is that Python objects can
        // dynamically gain or loose methods during their lifetime, so a check here would still not
        // guarantee anything.
        Value pygalRenderer = pygalRendererClass.newInstance();
        return pygalRenderer.as(GraphRenderer.class);
    }

    /**
     * Create a window to draw in.
     */
    static Frame createGraphWindow(GraphRenderer renderer) {
        Frame window = new Frame();
        window.setTitle("Java<->Python Graphing Example");
        window.setLayout(new GridBagLayout());

        SVGCanvas graph = new SVGCanvas();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        window.add(new Label("Function: f(x) ="), c);

        TextField tf = new TextField();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        window.add(tf, c);
        tf.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    // Get an InputStream with SVG data from the Python renderer
                    InputStream svg;
                    try {
                        svg = renderer.render(tf.getText(), window.getWidth());
                    } catch (PolyglotException e) {
                        e.printStackTrace();
                        tf.setText(e.getMessage());
                        return;
                    }
                    // Render the SVG into the graph canvas
                    graph.rasterizeSVG(svg);
                }
            });

        c.fill = GridBagConstraints.BOTH;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        window.add(graph, c);
        return window;
    }
}
