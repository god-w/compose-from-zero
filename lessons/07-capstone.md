# 07 · 阶段项目：任务清单

**目标**：不照抄答案，独立组合前六课知识，完成能使用的小应用。预计 3–5 小时。第 08–19 课会在它之上逐步扩展，第 20 课才是综合毕业项目。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 07 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/07.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 07 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/07.png)

**看图先猜**：过滤成“已完成”时再删除一项，原始列表和可见列表各会怎样变化？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 用纸先写出 A 未完成、B 已完成时三个过滤结果。
2. 只实现添加和总数；运行后再加入勾选、删除。
3. 最后从唯一 `tasks` 源推导过滤结果，照验收脚本逐步操作。

**停下来验收**：删除 B 后，切换回全部时 B 也确实消失；统计来自原始任务源。

**若结果不同**：如果某个过滤页显示旧数据，检查是否维护了第二份可变列表。
<!-- visual-end -->

## 产品要求

在 `CapstoneExercise` 做一个单屏任务清单：

- 顶部输入框和“添加”按钮；空白输入不得添加，前后空格应去掉。
- 每项可勾选完成、删除；显示“已完成 X / 总共 Y”。
- 过滤器：全部、未完成、已完成。过滤后添加和删除仍正确。
- 列表为空与过滤结果为空分别有明确提示。
- 旋转屏幕时数据保留。至少给输入、过滤器、列表项一个有意义的无障碍描述。

## 推荐实现顺序

1. 先画静态界面（输入、按钮、列表、统计）。
2. 定义 `Task(id, title, done)`；决定状态放在哪里。
3. 接入添加、勾选、删除，使用新的列表值更新状态。
4. 添加过滤；过滤是从原列表推导出的值，不要维护第二份可变列表。
5. 处理空输入、空状态、旋转与无障碍。

## 验收脚本

1. 输入空格并点击添加，数量仍为 0。
2. 添加 A、B，勾选 A：显示 1/2；“未完成”只显示 B。
3. 在“已完成”下删除 A，再切回“全部”：只剩 B，显示 0/1。
4. 旋转设备，B 仍在；返回首页再进入时是否保留，按你选定的作用域解释结果。

## 提示阶梯

1. `val visible = tasks.filter { filter == All || (filter == Done) == it.done }`。
2. 状态提升到 `CapstoneExercise`；行组件接收 `task, onToggle, onDelete`。
3. 要保存列表：`rememberSaveable` 配合自定义 `Saver`，或用 `ViewModel` 和 `SavedStateHandle`。先确保基本功能，再选一种实现。

## 参考设计（不是可复制答案）

```text
CapstoneExercise  ← 持有 tasks、input、filter
  ├── InputRow(value, onValueChange, onAdd)
  ├── FilterBar(selected, onSelect)
  ├── ProgressText(done, total)
  └── TaskList(tasks, onToggle, onDelete)
```

检查自己是否能独立回答：谁拥有状态？谁只显示状态？为什么过滤后的列表不该单独存成可变状态？

**挑战**：加入编辑标题和撤销删除。完成后进入架构课程，把这个单屏应用改造成真实项目结构。
