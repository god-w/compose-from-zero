# 16 · 恢复与 Room：旋转保留不等于长期保存

> **Kotlin 前置回查：** [K06 类、属性与数据类](kotlin/K06-classes.md) · [K07 集合与不可变更新](kotlin/K07-collections.md) · [K12 Flow 与 StateFlow](kotlin/K12-flow.md)。语法卡住时先做对应小步练习，再继续本章。

**本章目标：**按数据寿命选择存放位置，把任务保存到数据库，并明确进程恢复实验。

**学习顺序：**先理解原理和数据来源，再预测实验结果；每完成一个小步就运行一次，最后遮住解析完成验收。进阶章节列出的依赖只在学到该章时添加，现有课程 app 可以先正常运行。

## 本章要做出的效果与核心路径

![第 16 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/16.png)

这是完成练习后的**目标效果示意**，不是当前占位练习的运行截图。先观察内容与操作，不要求像素级复制设计。

![第 16 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/16.png)

先用下面的解释看懂这条路径，再做分步实验。新增的界面对照图也会明确标为教学示意；真实截图另行标注。
## 1. 先把三种实验分开

| 操作 | 发生什么 | 主要验证 |
| --- | --- | --- |
| 旋转导致配置重建 | Activity 重建，进程通常还在 | ViewModel / 小状态恢复 |
| 后台进程被系统回收后恢复任务 | 内存丢失，可能有系统保存信息 | SavedStateHandle / saved state |
| 强制停止，再从图标启动 | 新的启动过程 | 磁盘数据，不能承诺原屏幕小状态恢复 |

第 7 章 Saver 是小列表教学。真实任务数量增长后，应放数据库；Bundle 只保存筛选、选中 id 等恢复所需的小值。

## 2. 准备依赖：文件位置必须分清

本章沿用第 7–8 章模型和 UI。根目录 build.gradle.kts 的 plugins 加入：

```kotlin
id("com.google.devtools.ksp") version "2.3.10" apply false
id("androidx.room") version "2.8.5" apply false
```

app/build.gradle.kts 的 plugins 加入相同 id、不重复版本；文件顶层添加 room 配置，dependencies 添加运行时和处理器：

```kotlin
// plugins 块内
id("com.google.devtools.ksp")
id("androidx.room")

// plugins / android / dependencies 块之外
room { schemaDirectory("$projectDir/schemas") }

// dependencies 块内
implementation("androidx.room:room-runtime:2.8.5")
implementation("androidx.room:room-ktx:2.8.5")
ksp("androidx.room:room-compiler:2.8.5")
```

以上是插入位置示意，不是把三个片段原样合成一个裸文件。Sync 后先构建一次，确认 Room 处理器能运行，再写 UI。AGP 9 使用本项目已有内置 Kotlin 配置，不额外添加 kotlin-android 插件。

## 3. 第一步：定义数据库边界

新建 `TaskStorage.kt`，完整内容：

```kotlin
package dev.learning.compose

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val done: Boolean = false
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id")
    fun observeAll(): Flow<List<TaskEntity>>
    @Insert suspend fun insert(task: TaskEntity)
    @Query("UPDATE tasks SET done = NOT done WHERE id = :id")
    suspend fun toggle(id: Int)
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Int)
}

@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

object TaskStorage {
    @Volatile private var instance: TaskDatabase? = null
    fun database(context: Context): TaskDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext,
            TaskDatabase::class.java, "learning-tasks.db").build().also { instance = it }
    }
}
```

Entity 是存储结构，Task 是 UI/业务模型。id 由数据库生成，不再自己用列表长度生成。DAO 的 Flow 报告表变化；UI 不需要手工刷新一份镜像列表。

## 4. 第二步：让 ViewModel 观察唯一持久源

新建 `StoredTaskViewModel.kt`：

```kotlin
package dev.learning.compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoredTaskViewModel(application: Application, private val saved: SavedStateHandle)
    : AndroidViewModel(application) {
    private val dao = TaskStorage.database(application).taskDao()
    val tasks = dao.observeAll().map { rows -> rows.map { Task(it.id, it.title, it.done) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val filter = saved.getStateFlow("filter", 0)
    fun setFilter(value: Int) { if (value in 0..2) saved["filter"] = value }
    fun add(raw: String) {
        val title = normalizedTitle(raw) ?: return
        viewModelScope.launch { dao.insert(TaskEntity(title = title)) }
    }
    fun toggle(id: Int) { viewModelScope.launch { dao.toggle(id) } }
    fun delete(id: Int) { viewModelScope.launch { dao.delete(id) } }
}
```

这个类由 Activity 的默认 ViewModel 创建机制提供 Application 与 SavedStateHandle，不要在 UI 中手工 new。数据库写入可能失败，下一章会练习错误状态；当前样本先验证本地读写链。

## 5. 第三步：接回原有无状态 UI

把 CapstoneExercise 内的 model 类型替换为 StoredTaskViewModel，收集 tasks 和 filter：

```kotlin
val model: StoredTaskViewModel = viewModel()
val tasks by model.tasks.collectAsStateWithLifecycle()
val filter by model.filter.collectAsStateWithLifecycle()
var input by rememberSaveable { mutableStateOf("") }
TaskContent(tasks = tasks, input = input, filter = filter,
    onInput = { input = it },
    onAdd = { if (normalizedTitle(input) != null) { model.add(input); input = "" } },
    onFilter = model::setFilter, onToggle = model::toggle, onDelete = model::delete)
```

这是函数体片段，使用前几章的导入。不要同时保留旧的 `var tasks`；数据源已经移到 DAO。

![第 16 课实验界面对照](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/16-experiment.png)

教学示意比较强制停止后内存任务消失与 Room 记录恢复。筛选恢复和任务持久化分别验收。

## 6. 按操作留证据

1. 创建 A、B，完成 A，记下状态。
2. 强制停止 app，再从图标启动，进入第 7 课；两项与完成状态应存在。
3. 选择未完成再旋转，应保留筛选；不要要求强制停止后也恢复相同筛选。
4. 后台进程恢复实验：先让 app 进后台，再使用系统/adb 的进程回收方式，返回最近任务；记录方法与结果。不要把 force-stop 的结果冒充此实验。

DAO 初始 Flow 到达前，本例 emptyList 是初始占位；需要区分加载和真正空数据时，按下一章建模，不据此显示永久“空库”。

## 7. 理解检查与 schema 迁移

1. 为什么不把 10,000 条任务放 SavedStateHandle？
2. UI 删除成功后为什么不再自己维护一份列表？
3. 给表新增列，卸载重装成功是否证明迁移正确？

<details><summary>解析</summary>

1. Saved state 适合少量恢复信息，Bundle 大小和生命周期都不适合大量业务数据。
2. DAO Flow 会发出新查询结果；维护第二份列表会出现不同步。
3. 没有。卸载删掉旧数据库，迁移要在保留旧数据的安装上验证。

</details>

独立扩展：新增 createdAt 列，版本升到 2，写 Migration(1,2) 的 ALTER TABLE，并在 builder.addMigrations(...) 注册。不要用 destructive migration 掩盖升级问题。把生成的 app/schemas 提交版本控制。

| 症状 | 检查 |
| --- | --- |
| Cannot find implementation | KSP 是否应用到 app，compiler 是否用 ksp 配置？ |
| 强制停止后任务丢失 | 是否仍接的是内存 TaskViewModel？ |
| 数据库越来越多 | 是否每次组合都创建不同文件或实例？ |
| 改表后启动失败 | 版本、schema 与迁移是否匹配？ |

验收：持久化脚本通过、筛选恢复正确、唯一任务源明确。官方核对：[Room](https://developer.android.com/training/data-storage/room)、[Room 版本](https://developer.android.com/jetpack/androidx/releases/room)、[KSP 配置](https://kotlinlang.org/docs/ksp-quickstart.html)、[保存 UI 状态](https://developer.android.com/develop/ui/compose/state-saving)。

---

上一章：[第 15 章](15-drawing-layout.md) · 下一章：[第 17 章](17-async-data.md)
