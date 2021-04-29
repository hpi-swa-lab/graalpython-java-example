## Java and Python Integration Example for GraalVM

This example demonstrates how to integrate Python on GraalVM with a Java application.

### Getting Started

1. Download [GraalVM CE or EE](https://www.graalvm.org/downloads/) and set your `JAVA_HOME` to point to it. Make sure you have installed Python support:
```
"${JAVA_HOME}"/gu install python
```

2. Compile the example:
```
mvn compile
```

3. Run the example:
```
mvn exec:exec
```

### Extending and Experimenting

This repository is meant as an example and a jumping off point. There are
comments in the Java and Python files and the `pom.xml` for those aspects that
are of particular interest for Java and Python integration on GraalVM. The
example is kept small on purpose to allow reading through all of those files and
experiment.
