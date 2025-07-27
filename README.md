# UltimateAdvancementAPI
[![Build Status main Branch](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/main/badge/icon?subject=main&style=flat)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/main/)
[![Build Status dev Branch](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/dev/badge/icon?subject=dev&style=flat)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/job/dev/)
[![License](https://img.shields.io/badge/license-LGPL--3.0-orange?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/blob/main/LGPL)
[![Version](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.github.com%2Frepos%2Ffrengor%2FUltimateAdvancementAPI%2Ftags&query=%24%5B%3A1%5D.name&style=flat&label=version&color=blue)](https://jenkins.frengor.com/job/UltimateAdvancementAPI/)
[![Issues](https://img.shields.io/github/issues/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/issues)
[![Stars](https://img.shields.io/github/stars/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/stargazers)
[![Forks](https://img.shields.io/github/forks/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/network)
[![Contributors](https://img.shields.io/github/contributors/frengor/UltimateAdvancementAPI?style=flat)](https://github.com/frengor/UltimateAdvancementAPI/graphs/contributors)

A powerful API to create custom advancements for your minecraft server.

![Advancement Tab Image](https://github.com/frengor/UltimateAdvancementAPI/wiki/images/spigot-photo.png)

**Modrinth Page:** <https://modrinth.com/plugin/ultimateadvancementapi>  
**Spigot Page:** <https://www.spigotmc.org/resources/95585/>  
**Hangar Page:** <https://hangar.papermc.io/DevHeim/UltimateAdvancementAPI>  
**UltimateAdvancementGenerator:** <https://escanortargaryen.dev/UltimateAdvancementGenerator/>  
**Discord:** <https://discord.gg/BMg6VJk5n3>  
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
    <version>2.6.0</version>
    <scope>provided</scope>
</dependency>
```

#### Example Plugin:

An example of plugin using UltimateAdvancementAPI can be found [here](https://github.com/DevHeim-space/UltimateAdvancementAPI-Showcase).

More examples by the community can be found in the `showcase` forum on [Discord](https://discord.gg/BMg6VJk5n3).

#### Test Plugin:

The plugin used for tests can be found [here](https://github.com/frengor/UltimateAdvancementAPI-Tests).

## Contributing

Feel free to open issues or pull requests. Feature requests can be done opening an issue, the `enhancement` tag will be applied by maintainers.

For pull requests, open them towards the `dev` branch, as the `main` branch is only for releases. Make sure to allow edits by maintainers.
Also, please use the formatting style settings present under `.idea/codeStyles` folder.

## Required Java version

Currently, the project is compiled for Java 16, although the minimum required Java version might change in future releases.

> We consider changing the minimum required Java version a breaking change, so DO NOT expect it to be frequently modified.

In order to compile the code you must be using (at least) the Java version required by the last Minecraft version, since the project uses NMS.

## License

This project is licensed under the [GNU Lesser General Public License v3.0 or later](https://www.gnu.org/licenses/lgpl-3.0.txt).

## Credits

UltimateAdvancementAPI has been made by [fren_gor](https://github.com/frengor) and [EscanorTargaryen](https://github.com/EscanorTargaryen).  
The API uses the following libraries:
  * [EventManagerAPI](https://github.com/frengor/EventManagerAPI) (released under Apache-2.0 license) to handle events
  * [Libby](https://github.com/AlessioDP/libby) (released under MIT license) to handle dependencies at runtime
  * [CommandAPI](https://github.com/JorelAli/CommandAPI) (released under MIT license) to add commands to the plugin version of the API
  * [HikariCP](https://github.com/brettwooldridge/HikariCP) (released under Apache-2.0 license) to connect to MySQL databases
  * [bStats](https://bstats.org/) (the Java library is released under MIT license) to collect usage data (which can be found [here](https://bstats.org/plugin/bukkit/UltimateAdvancementAPI/12593)) about the plugin version of the API
