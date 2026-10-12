# FluidCloudHook — 强制 Eta 流体云卡片渲染（LSPosed 模块）

> 本模块是一个独立的 Xposed / LSPosed 模块，**不参与 Eta 主 App 的构建**。
> 它以独立 Gradle 子工程的形式存放于本仓库，与主 App 之间没有 Gradle 依赖关系。

## 背景

Eta 主工程内已内置完整的流体云 SDK：

- `app/libs/seedling-support-lite-3.0.7.aar`
- `app/src/main/kotlin/io/github/mangi/eta/fluidcloud/FluidCloudNotifier.kt`
- `app/src/main/kotlin/io/github/mangi/eta/fluidcloud/EtaFluidCloudProvider.kt`
- `app/src/main/kotlin/io/github/mangi/eta/fluidcloud/EtaFluidCloudEventProvider.kt`

但在 OPPO / 一加等机型上，`SeedlingTool.isSupportSeedlingCard(Context)` 会因服务端凭据校验不通过而返回 `false`，导致 `FluidCloudNotifier.init()` 从未被调用，卡片也就不会渲染。

## 原理

Hook `SeedlingTool` 的三个布尔查询方法，强制返回 `true`，绕过服务端校验：

| Hook 方法 | 效果 |
|---|---|
| `SeedlingTool.isSupportSeedlingCard(Context)` | 让 SDK 认为设备支持 Seedling 卡片 |
| `SeedlingTool.isSupportFluidCloud(Context)` | 让 SDK 认为设备支持流体云 |
| `SeedlingTool.isServicesEnabled(Context)` | 让 SDK 认为服务端校验已通过 |

并在 `Application.onCreate` 之后延迟 5 秒主动调用 `FluidCloudNotifier.init(context)`，确保即使上游判定失败也强制完成初始化。

## 构建

需要 JDK 17+ 和 Android SDK（36）。可以直接复用 Eta 主工程提供的 Gradle Wrapper：

```bash
# 1. 从仓库根目录进入本模块
cd fluidcloud-hook

# 2. 用上层 gradlew 构建（本模块有独立的 settings.gradle.kts）
../gradlew assembleDebug

# 3. 安装到手机
adb install app/build/outputs/apk/debug/app-debug.apk
```

> 说明：本模块有自己的 `settings.gradle.kts` 与 `build.gradle.kts`，因此不会与主 App 相互影响。运行主工程 `./gradlew assembleDebug` 时不会构建本模块。

## 启用

1. 安装 [LSPosed](https://github.com/LSPosed/LSPosed) 管理器。
2. 在 LSPosed 中启用本模块（`io.github.mangi.hook`）。
3. 作用域选择 `io.github.mangi.eta`（`res/values/arrays.xml` 已默认配置）。
4. 重启 Eta（或重启手机）。

## 验证

```bash
adb logcat | grep FluidCloudHook
# 期望看到：
# [FluidCloudHook] Loaded for io.github.mangi.eta
# [FluidCloudHook] Hooked com.oplus.pantanal.seedling.util.SeedlingTool.isSupportSeedlingCard
# [FluidCloudHook] Hooked com.oplus.pantanal.seedling.util.SeedlingTool.isSupportFluidCloud
# [FluidCloudHook] Hooked com.oplus.pantanal.seedling.util.SeedlingTool.isServicesEnabled
# [FluidCloudHook] All hooks installed
# [FluidCloudHook] Got context: io.github.mangi.eta
# [FluidCloudHook] FluidCloudNotifier.init called
# [FluidCloudHook] isSupported = true
# [FluidCloudHook] SUCCESS: Seedling SDK is supported! Cards should display now.
```

若 `isSupported` 仍为 `false`，说明 SDK 内还有其它校验点，需要进一步 Hook `SeedlingUpdateManager` 的初始化方法。

## 文件清单

| 路径 | 说明 |
|---|---|
| `app/src/main/java/io/github/mangi/hook/FluidCloudHook.java` | Hook 主体 |
| `app/src/main/assets/xposed_init` | Xposed 入口声明 |
| `app/src/main/AndroidManifest.xml` | 模块 meta-data 与属性 |
| `app/src/main/res/values/arrays.xml` | 默认 Xposed 作用域（`io.github.mangi.eta`） |
| `app/build.gradle.kts` | 模块构建配置（`compileOnly` xposed API） |
| `settings.gradle.kts` | 独立 Gradle 项目配置（含 xposed maven） |
| `build.gradle.kts` | 顶层插件声明 |
| `gradle.properties` | JVM / AndroidX 开关 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 版本锁定（8.9） |

## 已知限制

- 仅验证过 OPPO（pantanal）机型，其它厂商的 Seedling SDK 包名不同，需修改 `SEEDLING_TOOL` 常量。
- `isServicesEnabled` 的签名在不同厂商 ROM 上可能不同（有无 `Context` 参数），遇到失败会走 `Hook failed` 分支，可观察 logcat 后调整。
- 强制 `isSupportSeedlingCard = true` 只是本地渲染，服务端数据仍需 OPPO 后台正常下发；若后台本身未投放卡片，Hook 后仍无内容。
