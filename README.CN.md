
这是一个在 java 模块化编程环境下 (java9+)，将 `SpringBoot` 和 `OpenJFX` 集成的工具。  
测试使用的 springboot 版本是 2.7.9  
如果你在找 `java8` 的支持，请移步到这里： [springboot-javafx-support](https://github.com/roskenet/springboot-javafx-support.git)  
注：并不是一定要使用模块编程，如果你不熟悉，最好使用传统开发方式  
  
[English](README.md) | [中文](README.CN.md)  
  
[demo](https://github.com/WanneSimon/ofxDemo-spring-boot-support)  

**note: 0.0.2 还没有上传，部分功能重构**
### 使用
#### 依赖
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
声明 fxml 文件
``` java
@FXMLView("fxml/ImageSelector.fxml")
public class TestView extends BaseView{

	@Override
	public void loaded() {
		// do something after fxml loaded
	}
}
```
声明 fxml 中的 controller
``` java
@FXMLController
public class ImageSearchController {
    //@FXML
    //private FlowPane imgShowContainer;
    //@FXML
    //private TextField imgSearcherInput;

    //@Autowired
    //private ImgSelectorHandler handler;
}
```
主程序
``` java
/**
 * 注意: FX 启动的主类不能是 Spring 启动的主类
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

#### 单个 fxml 绑定多个 BaseView 类
FXML 的 Controller 需要定义为多态。  
`Scene` `FXMLLoader` `FXMLController` 是一对一的关系，复用 fxml 时请小心。虽然界面一样，但内部对象不一样。
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

### 打包
如果使用 `mvn clean package` 打包失败，尝试用 IDE 打包。
#### 使用 `spring-boot`传统方式打包
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
运行脚本 bat
``` bat
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.4.1
"%JAVA_HOME%\bin\java.exe" -jar target/ofxDemo-0.0.1.jar 
PAUSE
```
运行时你可能会遇到如下警告，原因可以参考 [https://www.nuomiphp.com/a/stackoverflow/en/61e171b79ab59a78c439553d.html](https://www.nuomiphp.com/a/stackoverflow/en/61e171b79ab59a78c439553d.html)  
使用另一种打包方式可以避免此问题。
```
Unsupported JavaFX configuration: classes were loaded from 'unnamed module @4635bc26'
```

#### 分离 openjfx 打包
因为是 springboot 项目，所以还是要使用 springboot 的maven打包工具。
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
打包结束后， target 目录下会多出一个 libs 目录，里面包含 openjfx 依赖。  
运行脚本 bat
``` bat
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.4.1
cd target
"%JAVA_HOME%\bin\java.exe"  -p libs --add-modules ALL-MODULE-PATH  -jar ofxDemo-0.0.1.jar 
PAUSE

```


