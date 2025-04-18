# Zippy

[![Maven Central](https://img.shields.io/maven-central/v/eu.maveniverse.maven.zippy/extension.svg?label=Maven%20Central)](https://search.maven.org/artifact/eu.maveniverse.maven.zippy/extension)

Requirements:
* Java 8+
* Maven 3.9+

## To use it

With Maven 3 create project-wide, or with Maven 4-rc-3+ create user-wide `~/.m2/extensions.xml` like this:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
    <extension>
        <groupId>eu.maveniverse.maven.zippy</groupId>
        <artifactId>extension</artifactId>
        <version>${currentVersion}</version>
    </extension>
</extensions>
```

Build requirements:
* Java 21
* Maven 3.9.9+
