# UltimateAdvancementAPI
[![Build Status main Branch](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/main/badge/icon?subject=main)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/main/)
[![Build Status dev Branch](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/dev/badge/icon?subject=dev)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/dev/)
[![License](https://img.shields.io/badge/license-LGPL--3.0-orange)](https://github.com/frengor/UltimateAdvancementAPI/blob/main/LGPL)
[![Version](https://frengor.com/UltimateAdvancementAPI/last-release.php)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/)
[![Issues](https://img.shields.io/github/issues/frengor/UltimateAdvancementAPI)](https://github.com/frengor/UltimateAdvancementAPI/issues)
[![Stars](https://img.shields.io/github/stars/frengor/UltimateAdvancementAPI)](https://github.com/frengor/UltimateAdvancementAPI/stargazers)
[![Forks](https://img.shields.io/github/forks/frengor/UltimateAdvancementAPI)](https://github.com/frengor/UltimateAdvancementAPI/network)
[![Contributors](https://img.shields.io/github/contributors/frengor/UltimateAdvancementAPI)](https://github.com/frengor/UltimateAdvancementAPI/graphs/contributors)

A powerful API to create custom advancements for your minecraft server.

![Advancement Tab Image](https://github.com/frengor/UltimateAdvancementAPI/wiki/images/spigot-photo.png)

**Spigot Page:** <https://www.spigotmc.org/resources/95585/>  
**Download Page:** <https://frengor.com/UltimateAdvancementAPI/>  
**Official Wiki:** <https://github.com/frengor/UltimateAdvancementAPI/wiki/>  
**Javadoc:** <https://frengor.com/javadocs/UltimateAdvancementAPI/latest/>  
**Jenkins:** <https://jenkins.frengor.com/job/UltimateAdvancementAPI/>

**Get it with maven:**
```xml
<repositories>
    <repository>
        <id>fren_gor</id>
        <url>https://nexus.frengor.com/repository/public/</url>
    </repository>
</repositories>
```   
```xml
<dependency>
    <groupId>com.frengor</groupId>
    <artifactId>ultimateadvancementapi</artifactId>
    <version>2.0.5</version>
    <scope>provided</scope>
</dependency>
```

#### Test Plugin:

The plugin used for tests can be found [here](https://github.com/frengor/UltimateAdvancementAPI-Tests).

## Contributing

Feel free to open issues or pull requests. Feature requests can be done opening an issue, the `enhancement` tag will be applied by maintainers.

For pull requests, use the formatting style settings present under `.idea/codeStyles` folder.

## Credits

UltimateAdvancementAPI has been made by [fren_gor](https://github.com/frengor) and [EscanorTargaryen](https://github.com/EscanorTargaryen).  
The API uses the following libraries:
  * [EventManagerAPI](https://github.com/frengor/EventManagerAPI) (released under Apache-2.0 license) to handle events
  * [Libby](https://github.com/AlessioDP/libby) (released under MIT license) to handle dependencies at runtime
  * [CommandAPI](https://github.com/JorelAli/CommandAPI) (released under MIT license) to add commands to the plugin version of the API
  * [HikariCP](https://github.com/brettwooldridge/HikariCP) (released under Apache-2.0 license) to connect to MySQL databases
  * [bStats](https://bstats.org/) (the Java library is released under MIT license) to collect usage data (which can be found [here](https://bstats.org/plugin/bukkit/UltimateAdvancementAPI/12593)) about the plugin version of the API
