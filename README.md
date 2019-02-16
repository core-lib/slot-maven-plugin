# Slot [![](https://jitpack.io/v/core-lib/slot.svg)](https://jitpack.io/#core-lib/slot)

Spring Boot 可插件化拓展改造器，让 Spring-Boot 应用支持加载外部 jar 包，实现插件化拓展。

GitHub: https://github.com/core-lib/slot

#### Slot: 在计算机行业指的就是周边元件扩展插槽。

## 问题描述
Spring-Boot 项目打包后是一个FatJar 即把所有依赖的第三方jar也打包进自身的jar中，运行时 classpath 包括 FatJar 中的 BOOT-INF/classes 目录和 BOOT-INF/lib 目录下的所有jar。

那么问题是要想加载外部化 jar 就只能打包期间把 jar 依赖进去，无法实现可插拔式插件化拓展。

[Slot](https://github.com/core-lib/slot) 就是一个可以将 Spring-Boot 项目升级为可支持加载外部 jar 的 Maven 插件。

## 原理说明

一个 Spring-Boot JAR 启动的流程可以分为以下几步：
1. 通过 java -jar spring-boot-app.jar args... 命令启动
2. JVM 读取该 jar 的 META-INF/MANIFEST.MF 文件中的 Main-Class，在 Spring-Boot JAR 中这个值通常为 org.springframework.boot.loader.JarLauncher 
3. JVM 调用该类的 main 方法，传入参数即上述命令中参数
4. JarLauncher 构建 ClassLoader 并反射调用 META-INF/MANIFEST.MF 中的 Start-Class 类的 main 方法，通常为项目中的 Application 类 
5. Application 类的 main 方法调用 SpringApplication.run(Application.class, args); 以最终启动应用

[Slot](https://github.com/core-lib/slot) 的核心原理是：
1. 拓展 org.springframework.boot.loader.JarLauncher 实现根据启动命令参数读取外部 jar 包并且加入至 classpath 中
2. 修改 META-INF/MANIFEST.MF 中的 Main-Class 为拓展的 JarLauncher

## 环境依赖
1. JDK 1.7 +
2. Spring-Boot

##  使用说明
```xml
<project>
    <!-- 设置 jitpack.io 插件仓库 -->
    <pluginRepositories>
        <pluginRepository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </pluginRepository>
    </pluginRepositories>
    <!-- 添加 Slot Maven 插件 -->
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.core-lib</groupId>
                <artifactId>slot-maven-plugin</artifactId>
                <version>LATEST_VERSION</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>transform</goal>
                        </goals>
                        <phase>package</phase>
                        <configuration>
                            <!-- optional
                            <sourceDir/>
                            <sourceJar/>
                            <targetDir/>
                            <targetJar/>
                            -->
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## 参数说明
| 参数名称 | 命令参数名称 | 参数说明 | 参数类型 | 缺省值 | 示例值 |
| :------ | :----------- | :------ | :------ | :----- | :----- |
| sourceDir | -Dslot.sourceDir | 源jar所在目录 | File | ${project.build.directory} | 文件目录 |
| sourceJar | -Dslot.sourceJar | 源jar名称 | String | ${project.build.finalName}.jar | 文件名称 |
| targetDir | -Dslot.targetDir | 目标jar存放目录 | File | ${project.build.directory} | 文件目录 |
| targetJar | -Dslot.targetJar | 目标jar名称 | String | ${project.build.finalName}.slot | 文件名称 |

插件的默认执行阶段是 package ， 当然也可以通过使用以下命令来单独执行。
```text
mvn slot:transform

mvn slot:transform -Dslot.targetJar=your-spring-boot-app-slot.jar
```

默认情况下，通过 slot 升级后的 jar 名称为 ${project.build.finalName}-slot.jar ，可以通过插件配置或命令参数修改。

## 注意事项
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <!-- 需要将executable和embeddedLaunchScript参数删除，目前还不能支持对该模式Jar的升级！
    <configuration>
        <executable>true</executable>
        <embeddedLaunchScript>...</embeddedLaunchScript>
    </configuration>
    -->
</plugin>
```

## 启动应用

Slot 支持使用两个参数来指定要加载的外部 jar 包：
1. --slot.root 即外部 jar 的根路径，缺省情况下为 Spring-Boot JAR 包的目录。
2. --slot.path 即外部 jar 的路径，支持设置多个，支持 ANT 表达式风格。

```text
java -jar spring-boot-app-slot.jar --slot.root=/absolute/root/ --slot.path=foo.jar  --slot.path=bar.jar

java -jar spring-boot-app-slot.jar --slot.path=/relative/path/to/plugin.jar

java -jar spring-boot-app-slot.jar --slot.path=/relative/path/to/**.jar
``` 

ANT 表达式通配符说明

| 通配符 | 含义 | 示例 |
| :----- | :--- | :--- |
| ** | 任意个字符及目录 | /plugins/**.jar 即 /plugins 目录及子目录的所有 .jar 后缀的文件 |
| * | 任意个字符 | /plugins/*.jar 即 /plugins 目录的所有 .jar 后缀的文件 |
| ? | 单个字符 | ???.jar 即当前目录所有名称为三个任意字符及以 .jar 为后缀的文件 |

通配符可以随意组合使用！ 例如 /plugins/\*\*/plugin-\*-v???.jar

## 使用技巧
由于通过 Slot 加载后的外部 jar 实际上和 Spring-Boot JAR 中的 jar 处于同一个 ClassLoader 所以外部插件和母体应用之间是一个平级的关系，
外部插件可以引用母体应用中的 class 同样母体应用也可以引用外部插件的 class。

由于外部插件项目或模块通常也会依赖另外的第三方jar，所以外部插件与母体应用集成运行时也需要把另外的第三方jar通过--slot.path参数加载进来。
推荐使用 maven-dependency-plugin 在打包时将需要用到的第三方jar拷贝到指定目录，最后通过ANT表达式方式一起加载运行。
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <includeScope>runtime</includeScope>
                <outputDirectory>${project.build.directory}/dependencies</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

或者使用 maven-shade-plugin 插件把相关的第三方jar资源通通打包进一个。
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.1.1</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                ...
            </configuration>
        </execution>
    </executions>
</plugin>
```
另外需要注意的是，当母体应用和外部插件有相同的第三方依赖时，推荐让外部插件模块以 &lt;scope&gt;provided&lt;/scope&gt; 的方式依赖之。


下面是作者想到的一些插件化拓展的方案：

1. IoC方式：母体应用声明接口，外部插件实现接口并且通过 @Component @Service 或其他注解让Spring 容器管理， 母体应用通过 @Resource @Autowired 来注入。

2. SPI方式：母体应用声明接口，外部插件实现接口并且配置于 META-INF/services/ 下，母体应用通过 ServiceLoader 加载接口的实现类。

3. AOP方式：外部插件通过 Spring Aspect 技术实现对母体应用的切面拦截。


## 版本记录
* 1.0.0 第一个正式版发布

## 协议声明
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 联系作者
QQ 646742615 不会钓鱼的兔子