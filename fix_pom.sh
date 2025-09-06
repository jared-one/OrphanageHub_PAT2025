#!/bin/bash
# Add dependencyManagement section before </project>
sed -i '/<\/project>/i \
    <dependencyManagement>\
        <dependencies>\
            <dependency>\
                <groupId>com.google.guava</groupId>\
                <artifactId>guava</artifactId>\
                <version>33.3.1-jre</version>\
            </dependency>\
        </dependencies>\
    </dependencyManagement>' pom.xml
