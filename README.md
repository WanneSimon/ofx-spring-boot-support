
`SpringBoot` and `OpenJFX` , simple integration (java9+)  
looking for `javaFX8` ?  see [springboot-javafx-support](https://github.com/roskenet/springboot-javafx-support.git)  
  
[English](README.md) | [中文](README.CN.md)  
  
[demo](https://github.com/WanneSimon/ofxDemo-spring-boot-support)  

### Usage
#### dependency
``` xml
<repositories>
    <repository>
      <id>ossrh</id>
      <name>ossrh</name>
      <url>https://s01.oss.sonatype.org/content/repositories/releases/</url>
    </repository>
</repositories>

<dependencies>
  <dependency>
      <groupId>cc.wanforme</groupId>
      <artifactId>ofx-spring-boot-support</artifactId>
      <version>0.0.2</version>
  </dependency>
  <dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>19.0.2.1</version>
  </dependency>
  <!-- necessary if springboot's configuration is *.yml or *.yaml and you have module-info.java  -->
  <dependency>
      <groupId>org.yaml</groupId>
      <artifactId>snakeyaml</artifactId>
      <version>1.30</version>
  </dependency>
</dependencies>
```
#### Coding
Declaring fxml file and view, bind a BaseView class
``` java
@FXMLView("fxml/ImageSelector.fxml")
public class TestView extends BaseView{

    @Override
    public void loaded() {
        // do something after fxml loaded
    }
}
```
Declaring controller in fxml
``` java
@FXMLController
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) 
public class ImageSearchController {
    //@FXML
    //private FlowPane imgShowContainer;
    //@FXML
    //private TextField imgSearcherInput;

    //@Autowired
    //private ImgSelectorHandler handler;
}
```
Main class
``` java
/**
 * note: BaseOFXApplication can not be the SpringBootApplication
 */
public class OFXApp extends BaseOFXApplication {

	@Override
	protected void scene(Scene scene) {
		// custom settings
	}

	@Override
	protected void stage(Stage primaryStage) {
		// custom settings
	}
}

@SpringBootApplication
public class SpringApp{

	public static void main(String[] args) {
		BaseOFXApplication.launchOFX(OFXApp.class, SpringApp.class, TestView.class, args);
	}
}
```
module-info.java
``` java
module cc.wanforme.ofxDemo {
    requires cc.wanforme.ofx;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.core;

    requires org.apache.tika.core;
    requires org.slf4j;

    requires transitive java.sql;

    exports cc.wanforme.ofxDemo;
    opens cc.wanforme.ofxDemo;
}
```

#### A fxml with multi BaseView
A *.fxml file associate with multi BaseView classes,  
Controller should be 'prototype'
``` java
@FXMLController
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) 
public class ImageSearchController {
    //@FXML
    //private FlowPane imgShowContainer;
    //@FXML
    //private TextField imgSearcherInput;

    //@Autowired
    //private ImgSelectorHandler handler;
}
```

### package
If you run `mvn clean package` failed, try with your IDE .
#### packaging with `spring-boot`
``` xml
<build>
  <!-- app file name -->
  <finalName>ofxDemo-${project.version}</finalName>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
        <mainClass>cc.wanforme.ofxDemo.OFXApp</mainClass>
      </configuration>
    </plugin>
  </plugins>
</build>
```
bat
``` bat
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.4.1
"%JAVA_HOME%\bin\java.exe" -jar target/ofxDemo-0.0.1.jar 
PAUSE
```
You may encounter a warning, it can be found on stackoverflow or [here](https://www.nuomiphp.com/a/stackoverflow/en/61e171b79ab59a78c439553d.html)  
Try another way to avoid .
```
Unsupported JavaFX configuration: classes were loaded from 'unnamed module @4635bc26'
```

#### packaging - separating openjfx jars
We still need springboot's maven plugin
``` xml
<build>
  <finalName>ofxDemo-${project.version}</finalName>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
          <mainClass>cc.wanforme.ofxDemo.OFXApp</mainClass>
          <!-- exclude openjfx jars -->
          <excludeGroupIds>org.openjfx</excludeGroupIds>
      </configuration>
    </plugin>

    <!-- openjfx jars -->
      <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-dependency-plugin</artifactId>
          <executions>
              <execution>
                <id>copy-dependencies</id>
                <phase>package</phase>
                <goals>
                    <goal>copy-dependencies</goal>
                </goals>
                <configuration>
                    <skip>false</skip>
                    <outputDirectory>${project.build.directory}/libs</outputDirectory>
                    <includeGroupIds>org.openjfx</includeGroupIds>
                </configuration>
              </execution>
          </executions>
    </plugin>
  </plugins>
</build>
```
Directory - libs will generate under target after package, all openjfx jars lie here  
bat
``` bat
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.4.1
cd target
"%JAVA_HOME%\bin\java.exe"  -p libs --add-modules ALL-MODULE-PATH  -jar ofxDemo-0.0.1.jar 
PAUSE

```


