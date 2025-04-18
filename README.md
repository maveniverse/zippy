# Zippy

[![Maven Central](https://img.shields.io/maven-central/v/eu.maveniverse.maven.zippy/extension.svg?label=Maven%20Central)](https://search.maven.org/artifact/eu.maveniverse.maven.zippy/extension)

Requirements:
* Java 8+
* Maven 3.9+

This extension and plugin suite provides following bits:
* Provides `zip-classpath` artifact handler that makes it possible to declare this as dependency type, and the ZIP file will
  be added to classpath
* Provides `zip` artifact handler that makes it behave _exactly as Maven 3 would do_ (do not add it to classpath), **but**
  you can define `maveniverse.zippy.zip.classpath=true` Java System Property and then ZIPs will be added to classpath.
  For new projects, use of explicitly type `zip-classpath` is *recommended*.
* Provides simple `zip` packaging (POM `<packaging>zip</packaging>`) that is producing a ZIP file from unfiltered and
  possibly filtered resources (is based on `maven-jar-plugin` so same applies).

## To use it

With Maven 3 create project-wide, or with Maven 4-rc-3+ create user-wide `~/.m2/extensions.xml` like this:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
    <extension>
        <groupId>eu.maveniverse.maven.zippy</groupId>
        <artifactId>extension3</artifactId>
        <version>${currentVersion}</version>
    </extension>
</extensions>
```

Build requirements:
* Java 21
* Maven 3.9.9+
