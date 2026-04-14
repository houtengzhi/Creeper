# Creeper项目 - Android开发知识点总结

## 一、编程语言与基础

### 1. Kotlin语言
**面试要点：**
- **扩展函数/属性**：项目中使用了如 `getClientIconPath()` 扩展函数
- **数据类**：使用 `data class` 定义实体如 `Converter`, `SubscriptionSource`
- **密封类**：`Screen` 使用 `sealed class` 实现类型安全的路由
- **协程**：大量使用 `suspend` 函数、`flow`、`viewModelScope.launch()`
- **委托属性**：使用 `by mutableStateOf()` 委托管理状态

**面试题示例：**
- Kotlin协程与Java线程的区别？
- `suspend` 函数的工作原理？
- sealed class在Android开发中的应用场景？

---

## 二、Jetpack Compose UI框架

### 1. 声明式UI
**核心概念：**
- **状态管理**：使用 `mutableStateOf`, `mutableStateListOf` 管理可观测状态
- **重组机制**：状态变化自动触发UI更新
- **Compose主题**：`AppTheme` + Material Design 3

**代码示例：**
```kotlin
var converterName by mutableStateOf("")
// UI会自动响应状态变化
```

**面试要点：**
- Compose与传统View系统的区别
- 如何避免不必要的重组
- `remember`, `rememberSaveable` 的区别

### 2. Navigation Compose
**实现方式：**
- 使用 `NavHost` + `composable` 定义路由
- 支持参数传递：`navArgument("requestCode")`
- 自定义导航结果返回：`navigateForResult`, `setResult`

**面试要点：**
- Navigation组件的类型安全路由实现
- 如何在Compose中处理页面跳转和结果返回

---

## 三、架构设计模式

### 1. MVVM架构
**项目实现：**
```
UI (Compose Page)
  ↓
ViewModel (ConvertViewModel等)
  ↓
Repository (DataRepos)
  ↓
  ├── Database (Room)
  ├── Network (Retrofit/OkHttp)
  └── File (FileRepos)
```

**面试要点：**
- MVVM的优势与职责划分
- ViewModel如何避免内存泄漏
- Repository模式的作用

### 2. Hilt依赖注入
**核心注解：**
- `@HiltAndroidApp` - Application入口
- `@AndroidEntryPoint` - 自动注入到Activity/Service
- `@HiltViewModel` - ViewModel注入
- `@Module` + `@Provides` - 依赖提供
- `@AssistedInject` - 带参数的ViewModel工厂

**代码示例：**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @ProvidesProvides
    @Singleton
    fun provideDataRepos(...): DataRepos
}
```

**面试要点：**
- Hilt与Dagger的关系
- `@Singleton` 的作用域
- 如何在非Hilt组件中获取依赖（使用EntryPoint）

---

## 四、数据持久化

### 1. Room数据库
**核心组件：**
- `@Database` - 数据库定义
- `@Dao` - 数据访问对象
- `@Entity` - 表映射
- `@Transaction` - 事务操作
- 关系映射：`ConverterWithSources` 使用 `@Relation`

**代码示例：**
```kotlin
@Database(entities = [...], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun appDao(): AppDao
}
```

**Flow集成：**
```kotlin
@Query("SELECT * FROM converter")
fun subscribeConverterList(): Flow<List<ConverterWithSources>>
```

**面试要点：**
- Room与SQLite的优势对比
- 如何处理数据库版本迁移
- Flow在Room中的应用及优势

---

## 五、、网络编程

### 1. Retrofit + OkHttp
**配置要点：**
- 动态BaseURL：使用 `RetrofitUrlManager`
- Kotlinx序列化：`converter-kotlinx-serialization`
- 日志拦截器：`HttpLoggingInterceptor`
- 超时配置：`connectTimeout`, `readTimeout`

**接口定义：**
```kotlin
interface GithubService {
    @GET("gists")
    suspend fun getGistList(@Header("Authorization") token: String): List<Gist>
}
```

### 2. Kotlinx Serialization
**JSON序列化配置：**
```kotlin
val json = Json { ignoreUnknownKeys = true }
```

**面试要点：**
- Retrofit的工作原理
- 如何处理动态BaseUrl
- 协程挂起函数在Retrofit中的使用

---

## 六、异步编程

### 1. Kotlin协程
**与其他库的集成：**
- **ViewModel**：使用 `viewModelScope.launch()`
- **Flow**：状态流 `stateIn`, 共享流 `shareIn`
- **Room**：返回Flow对象实现数据观察
- **异常处理**：`CoroutineExceptionHandler`

**代码示例：**
```kotlin
val subscribeConverterListState = dbRepos.subscribeConverterList()
    .flowOn(Dispatchers.IO)
    .map { DataState(it) }
    .onStart { emit(DataState(true, null, null)) }
    .catch { emit(DataState(it)) }
    .stateIn(viewModelScope, started = SharingStarted.Lazily, ...)
```

**面试要点：**
- 协程作用域的生命周期
- Flow的背压处理策略
- `flowOn` 与 `withContext` 的区别

---

## 七、Android服务

### 1. 前台服务
**实现要点：**
- `startForeground()` 必须调用
- 通知渠道：`NotificationChannel` (Android O+)
- 服务类型声明：`ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE`
- 防止系统杀进程

**代码示例：**
```kotlin
class CreeperService: Service() {
    private lateinit var mServer: Server
    override fun onCreate() {
        mServer = AndServer.webServer(this)
            .port(ServerManage.getPort())
            .timeout(15, TimeUnit.SECONDS)
            .build()
    }
}
```

### 2. AndServer嵌入式服务器
**功能：**
- Android设备作为HTTP服务器
- RESTful API：使用 `@Controller`, `@GetMapping`
- 注入依赖：通过 `EntryPoint` 获取Hilt管理的依赖

**面试要点：**
- 前台服务的通知要求
- 服务的生命周期管理
- AndServer与传统后端的区别

---

## 八、Firebase集成

### 1. Firebase Authentication
**集成方式：**
- Google Services插件：`id("com.google.gms.google-services")`
- BOM版本管理：`Firebase BOM`
- 依赖注入：`provideFirebaseAuth()`

**面试要点：**
- Firebase的初始化流程
- 如何处理Firebase认证状态

---

## 九、系统权限

### 1. Accompanist Permissions
**用途：**
- 运行时权限请求
- 网络权限、前台服务权限等

---

## 十、构建与打包

### 1. Gradle Kotlin DSL
**高级配置：**
- **多渠道打包**：`productFlavors`
- **动态APK命名**：自定义输出文件名
- **代码混淆**：`isMinifyEnabled`, ProGuard规则
- **签名配置**：自定义签名文件

**代码示例：**
```kotlin
flavorDimensions += "channel"
productFlavors {
    create("googlePlay") { dimension = "channel" }
}

// 动态APK命名
applicationVariants.all {
    val newApkName = "${appName}_v${versionName}_${buildType}_${time}.apk"
}
```

**面试要点：**
- Gradle构建变体的配置
- ProGuard混淆规则的作用
- 如何保护敏感数据（如签名密码）

---

## 十一、其他关键技术点

### 1. Edge-to-Edge显示
```kotlin
enableEdgeToEdge()  // Android 14+ 全面屏适配
```

### 2. Parcelize
```kotlin
id("kotlin-parcelize")
// 自动生成Parcelable实现
```

### 3. 日志与调试
- Android Log系统
- OkHttp日志拦截器

---

## 面子试高频问题总结

| 技术点 | 核心问题 |
|--------|----------|
| Compose | 声明式UI原理、状态管理、重组优化 |
| Hilt | 依赖注入原理、作用域、模块设计 |
| Room | 数据库设计、关系映射、Flow集成 |
| 协程 | 作用域、异常处理、Flow背压 |
| Retrofit | 动态BaseUrl、拦截器、协程集成 |
| 架构 | MVVM职责划分、Repository模式 |

---

## 项目技术栈概览

- **UI框架**：Jetpack Compose + Material Design 3
- **架构模式**：MVVM + Repository Pattern
- **依赖注入**：Hilt (Dagger)
- **数据库**：Room + Flow
- **网络**：Retrofit + OkHttp + Kotlinx Serialization
- **异步**：Kotlin Coroutines + Flow
- **服务器**：AndServer (嵌入式HTTP服务器)
- **认证**：Firebase Authentication
- **构建**：Gradle Kotlin DSL
- **语言**：Kotlin

---

这个项目涵盖了现代Android开发的**全套技术栈**，熟练掌握这些知识点可以应对**中高级Android开发面试**。建议重点深入理解：
1. Compose状态管理与重组机制
2. Hilt依赖注入的高级用法
3. 协程+Flow的异步编程模式
4. MVVM架构的完整实现
5. Room数据库的关系映射与迁移
