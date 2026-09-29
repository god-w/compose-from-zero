# 17 · 异步数据、错误与取消

**目标**：把异步结果建模为加载、成功、空结果和错误；让快速切换筛选时旧请求不覆盖新结果。预计 2 小时。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 17 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/17.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 17 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/17.png)

**看图先猜**：“请求成功但列表为空”和“请求失败”，用户下一步应该看到同一句话吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先用假仓库固定返回 Loading、Empty、Success、Error 四种结果。
2. 给每种状态做最小界面；先验证文字，再接真实列表。
3. 人为延迟旧请求并快速切换筛选，观察是否被旧结果覆盖。

**停下来验收**：空结果有引导，错误有重试；快速切换后仍显示最新条件的结果。

**若结果不同**：如果旧结果覆盖新结果，检查请求是否随条件变化取消或用 `flatMapLatest`。
<!-- visual-end -->

## 概念

异步 UI 不能只有 `List<Task>`：请求未完成、失败、结果为空都是不同状态。网络、数据库或模拟仓库负责数据；ViewModel 负责发起和取消工作；Composable 负责呈现和回传“重试”等事件。副作用也有生命周期：页面离开后，已经不需要的加载应取消或不再更新该页面。

## 跟做：先用本地假仓库

1. 定义 `sealed interface LoadState { data object Loading; data class Success(val tasks: List<Task>); data class Error(val message: String) }`。按你的模型改写类型。
2. 定义 `suspend fun loadTasks(filter: Filter): List<Task>`，先 `delay(800)`；某个筛选条件故意抛异常，用来练错误分支。
3. ViewModel 用 `viewModelScope.launch` 加载，更新 `StateFlow<LoadState>`。调用新查询前取消旧 `Job`，或用 `flatMapLatest`/`collectLatest` 表达“只要最新请求”。
4. UI 分别显示进度、列表、空状态和错误页；错误页提供“重试”按钮。
5. 快速切换“全部 → 已完成 → 未完成”，检查最终一定显示“未完成”的数据。

## 验收

- 初始加载时能看到进度；成功但无数据时显示“暂无任务”，与网络失败文案不同。
- 故意失败后能重试成功；重试不会创建无限个并行请求。
- 快速切换条件不会让旧结果覆盖新结果；离开页面不崩溃。

## 提示

先让每个状态单独正确，再解决并发。对演示用的假仓库可用 `delay` 模拟网络。实际接 API 时不要在 Composable 中直接创建 HTTP 客户端；根据需要复用现有项目的网络层。

**延伸**：接入一个你可控制的测试 API，增加超时、HTTP 错误映射和离线缓存。为失败和重试写一条 UI 测试。

官方资料：[Android UI 层架构](https://developer.android.com/topic/architecture/ui-layer)、[在 Compose 中使用其他状态类型](https://developer.android.com/develop/ui/compose/state#use-other-types-of-state-in-jetpack-compose)。
