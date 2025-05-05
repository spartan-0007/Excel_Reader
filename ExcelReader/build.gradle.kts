plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")


        implementation("org.apache.poi:poi:5.2.3") // Apache POI core library
        implementation("org.apache.poi:poi-ooxml:5.2.3") // Apache POI OOXML support (for .xlsx files)
        implementation("org.apache.xmlbeans:xmlbeans:5.0.2") // Required library for Apache POI
        implementation("org.apache.commons:commons-collections4:4.4") // Required library
        implementation("org.apache.commons:commons-compress:1.21") // Use the latest version available
    implementation("org.apache.logging.log4j:log4j-api:2.20.0") // Use the latest version
    implementation("org.apache.logging.log4j:log4j-core:2.20.0") // Use the latest version
    implementation("mysql:mysql-connector-java:8.0.33")
}

tasks.test {
    useJUnitPlatform()
}