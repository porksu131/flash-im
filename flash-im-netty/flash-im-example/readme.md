# 通过maven插件打成jar包

```xml

<build>
    <finalName>${project.artifactId}</finalName>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.6.0</version>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>com.szr.flashim.example.ChatClient</mainClass> <!-- 替换为你的主类 -->
                    </manifest>
                </archive>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

# 安装wix 3.XX

下载地址：（https://github.com/wixtoolset/wix3/releases）  
下载二进制包（wix311-binaries.zip）解压并添加根目录路径到环境变量path中

# 打包window可执行文件FlashIm.exe

```shell
jpackage --name FlashIm --input target/ --main-jar flash-im-example-jar-with-dependencies.jar --type exe --icon chat-logo.ico --win-shortcut –-win-dir-chooser
```

```text
# -i 表示输入文件夹
# -n 表示应用名称
# --main-jar 指定jar包，需要jar已经指定main类，否则需要添加--main-class手动指定，如：--main-class com.demo.DemoApplication
# --vendor 发行商信息
# --verbose 显示打包执行过程
# --win-console 使用控制台输出
# --win-dir-chooser 选择安装位置
# --win-shortcut 创建快捷方式
# --icon 指定应用程序图标
#其他的选项如下：
# --win-dir-chooser, 安装时添加 “选择安装路路径”
# --win-shortcut, 安装后自动在桌面添加快捷键
# --win-menu-group, 启动该应用程序所在的菜单组 (实测无效，但是必须有这条命令，没有–win-menu 会报 311 错误)
# --update 2025-3-18： --win-menu-group 应该放在--win-menu 之后，否则无效。
# -- win-menu，添加到系统菜单中
# 示例：jpackage --name Non-modular-installer --input lib --main-class com.raven.App --main-jar Non-modular-packaging-demo.jar --vendor raven --win-dir-chooser --win-shortcut --win-menu --win-menu-group "Non-modular-packaging"
```

# 执行FlashIm.exe安装程序
