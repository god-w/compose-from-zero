# 16 · 状态恢复与离线持久化

**目标**：按数据寿命选择 `remember`、`rememberSaveable`、`ViewModel`、`SavedStateHandle` 和数据库。预计 2–3 小时；修改第 07/08 课的任务清单。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 16 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/16.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 16 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/16.png)

**看图先猜**：旋转、杀进程、重新启动，哪种操作会让内存状态消失？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先对当前任务清单依次做三种操作，记录丢失了什么。
2. 只把筛选条件改为可恢复的小状态，重复实验。
3. 最后把任务放入 Room，再次强制停止并启动。

**停下来验收**：任务记录跨重启存在；筛选条件按设计恢复；能解释二者为何存放位置不同。

**若结果不同**：如果任务只在旋转后还在、重启后消失，检查是否仍只存在 ViewModel 中。
<!-- visual-end -->

## 先做决策表

| 数据 | 合适位置 | 原因 |
| --- | --- | --- |
| 卡片是否展开 | `rememberSaveable` | 小型界面状态，旋转后可恢复 |
| 当前筛选条件 | `SavedStateHandle` 或 `rememberSaveable` | 恢复当前屏幕位置所需的小值 |
| 任务记录 | Room/其他持久层 | 关机、进程结束后仍应存在 |
| 当前网络请求 Job | ViewModel 的协程作用域 | 不需要写入磁盘 |

`rememberSaveable` 和 `SavedStateHandle` 通过 Bundle 保存少量恢复信息，不适合存大量任务。ViewModel 跨配置变化存活，但进程结束后会重建。数据库负责真正的持久化。

## 跟做

1. 先记录现状：输入 3 条任务，旋转、后台杀进程、强制停止、重新启动。写下每一步哪些状态丢失。
2. 让筛选条件用 `rememberSaveable` 保存；若已用 ViewModel，把它移入 `SavedStateHandle`。再次做旋转与进程恢复实验。
3. 将任务数据放入 Room：定义 `TaskEntity(id, title, done)`、DAO 的查询/插入/更新/删除、Database；为 Room 添加官方当前稳定依赖及 KSP 配置。
4. DAO 返回 `Flow<List<TaskEntity>>`；ViewModel 把它转换为 `TaskUiState`，UI 用 `collectAsStateWithLifecycle()` 观察。
5. 不要把每次数据库写入放在 Composable 函数体；事件调用 ViewModel，由 `viewModelScope` 执行。

## 验收脚本

1. 新增 A、B，完成 A；强制停止 App 再打开，两项及完成状态仍在。
2. 筛选“未完成”，旋转屏幕后仍是“未完成”；若进程恢复行为与预期不同，能指出是保存机制还是导航作用域问题。
3. 将 Room 查询人为延迟，UI 仍能显示加载或现有数据，不会因主线程阻塞而卡住。

## 常见错误

- 把整个 `TaskUiState` 塞进 `SavedStateHandle`，造成 Bundle 过大。
- 在两个地方各维护一份可变任务列表，UI 与数据库逐渐不一致。
- 每次重组都新建数据库实例或发起一次查询。

**延伸**：做一次 schema 变更（新增 `createdAt`）并写迁移，旧安装上的任务必须保留。不要用 destructive migration 掩盖数据升级问题。

官方资料：[Compose 状态恢复](https://developer.android.com/develop/ui/compose/state-saving)、[Room](https://developer.android.com/training/data-storage/room)、[生命周期收集 Flow](https://developer.android.com/develop/ui/compose/state#use-other-types-of-state-in-jetpack-compose)。
