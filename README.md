# YEss
基础功能插件

## CommandHelp

===== YEss v1.0.0 帮助 =====

- /yess help <player>
  - yess.command.help
- /yess ec <player>
  - yess.command.ec
- /yess glow <player>
  - yess.command.glow

- /yess gift <create|edit|give|help>
  - yess.command.gift

- /yess team <create|invite|remove|promote|disband|show|leave|accept|deny>
  - yess.command.team

- /yess help
  - yess.command.help
- /yess reload
  - yess.command.reload

## Config.yml

```yaml
version: ${version}

# 该项的更改需要重启服务器才能生效
registerCommand:
  reload:
    enable: true
    alias: "none"
    permission: "yess.command.reload"
  help:
    enable: true
    alias: "none"
    permission: "yess.command.help"
  craft:
    enable: true
    alias: "craft, c"
    permission: "yess.command.craft"
  ec:
    enable: true
    alias: "enderchest, ec"
    permission: "yess.command.ec"
  glow:
    enable: true
    alias: "glow"
    permission: "yess.command.glow"
  gift:
    enable: true
    alias: "gift"
    permission: "yess.command.gift"
  team:
    enable: true
    alias: "team"
    permission: "yess.command.team"

# 存储类型: yaml, mysql 或 sqlite
storage:
  type: "yaml"  # 目前仅支持yaml，填写其他类型无效

  # MySQL配置 (当storage.type=mysql时使用)
  mysql:
    host: "localhost"
    port: 3306
    database: "minecraft"
    username: "root"
    password: "password"

# 门
portal:
  world: "spawn"
  command:
    - "multiverse-core:mv tp %player% afk"
  regionMin:
    x: 43
    y: 72
    z: -2
  regionMax:
    x: 44
    y: 78
    z: 2

disableNetherPortal:
  - spawn
  - afk
  - arena
```

## PAPI

- %yess_glow_status% 玩家是否处于发光状态
