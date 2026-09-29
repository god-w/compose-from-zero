# 09 · 导航与返回栈

**目标**：把任务列表和详情拆成两个目的地，能正确传递 ID 与处理系统返回。预计 90–120 分钟。直接扩展第 07/08 课任务清单。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 09 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/09.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 09 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/09.png)

**看图先猜**：在详情页打开期间任务被删除，详情页还应显示旧对象吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 先让列表点击只传任务 `id`，日志或页面标题显示它。
2. 创建详情目的地，按 `id` 从状态源取得当前任务。
3. 加入任务不存在的界面，再验证系统返回与旋转。

**停下来验收**：详情不会持有过时的整份任务对象，删除后能显示明确的空状态。

**若结果不同**：如果返回时叠出多个列表，检查是否重复导航到列表而没有使用返回栈。
<!-- visual-end -->

## 讲解

单屏时，项目首页的 `selected` 足够。真正多屏时用 Navigation Compose，它管理返回栈和目的地生命周期。跨页面只传稳定 ID，详情页按 ID 从状态来源读取数据；不要把整个可变对象塞入路由。详情页必须处理任务被删除或 ID 不存在的情况。

## 动手步骤

1. 在 `app/build.gradle.kts` 加入 `implementation("androidx.navigation:navigation-compose:2.10.2")`，Sync。
2. 为毕业项目建立列表、详情两个目的地。先用最简单的字符串路由，或按官方最新文档使用类型安全路由。
3. 点击列表项只传 `task.id`；详情页显示标题、完成状态和“返回”。
4. 删除当前任务后导航回列表；测试系统返回键。
5. 从详情进入一个“编辑标题”页面；保存后返回详情，再返回列表。画出每一步的返回栈。

## 验收

- 连续打开不同任务，详情显示正确内容。
- 任务不存在时显示“任务已不存在”，不会崩溃。
- 旋转设备后仍在当前详情页；返回键回到列表。
- 在详情页连续点击“返回”不会产生重复列表页；恢复当前页面后仍能读取正确任务。

## 提示

先让 `NavHost` 和两个 `composable` 路由跑通，再迁移真实数据。若仍在学习状态提升，可把任务状态留在 `NavHost` 上层，两个目的地共享同一状态来源。字符串路由最小骨架：

```kotlin
NavHost(navController, startDestination = "tasks") {
    composable("tasks") { TaskListScreen(onOpen = { id -> navController.navigate("task/$id") }) }
    composable("task/{id}") { entry ->
        val id = entry.arguments?.getString("id")?.toIntOrNull()
        TaskDetailScreen(id = id, onBack = { navController.popBackStack() })
    }
}
```

这里省略了数据来源和函数签名，需按你的第 07 课实现补齐。对非法 `id` 与不存在的任务显示明确空状态。

## 自检

观察列表 → 详情 → 编辑 → 保存 → 返回的返回栈。若保存后使用 `navigate("tasks")` 产生两个列表页，改用合适的 `popBackStack()` 或返回策略。

官方资料：[Navigation with Compose](https://developer.android.com/develop/ui/compose/navigation)。
