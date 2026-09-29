# 11 · 质量：无障碍、预览与本地化

**目标**：让任务清单能用 TalkBack 操作，用预览覆盖边界状态，并使用户可见文本可本地化。预计 90–120 分钟。性能测量集中在第 19 课。

<!-- visual-start -->
## 看图动手：先预测，再验证

### 先看目标效果

![第 11 课完成后的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/11.png)

上图是**完成练习后的目标效果示意**，当前练习代码仍是留给你动手的占位内容。观察画面后，先回答下面的问题，再运行 App 比对。

![第 11 课概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/11.png)

**看图先猜**：如果屏幕只画了一个勾选图标，TalkBack 能知道它代表“已完成”吗？先写下你的答案，运行后再对照。

**三小步跟做**：

1. 开启 TalkBack，先听当前任务行被读成什么。
2. 给状态与删除动作补语义描述，重新听读。
3. 把系统字体放大，检查标题、按钮和列表是否还能操作。

**停下来验收**：读屏能说出任务名、状态和动作；大字号下主操作可达。

**若结果不同**：如果读屏重复朗读，检查父子节点语义是否需要合并。
<!-- visual-end -->

## 无障碍

打开 TalkBack 走一遍毕业项目。每个图标按钮要有明确 `contentDescription`（如“删除任务 A”）；纯装饰图形用 `null`。勾选状态要能读出，触控目标要足够大。不要只靠颜色表达完成状态。文本缩放到 200% 时检查截断和按钮可达性。

## 预览与本地化

给无状态的 `TaskList` 或资料卡添加 `@Preview`，传入示例数据，至少预览空列表、长标题、已完成三种状态。预览不需要启动模拟器，但仍需在真机或模拟器验证交互。把用户可见文案移到 `strings.xml`，使用 `stringResource`；“已完成 X / Y”也应由资源格式化，避免翻译时词序无法调整。

## 跟做

1. 给任务行的复选框、删除按钮写清楚名称与状态；纯装饰图标不读出。开启 TalkBack 逐项滑动检查。
2. 将字体放大到 200%，横竖屏各检查一次；修复被截断的主操作。
3. 写 3 个 `@Preview`：空、长标题、完成/未完成混合。预览函数只调用无状态 UI，不应依赖真实数据库。
4. 把硬编码的用户可见字符串移入资源；添加一套英文资源作验证，切换系统语言检查界面。

## 验收

- TalkBack 能读出任务名、完成状态及删除按钮目的。
- 大字号下没有被遮挡的主操作。
- 切换语言后页面仍有正确文案；长文本与大字号下功能可达。

官方资料：[无障碍](https://developer.android.com/develop/ui/compose/accessibility)、[预览](https://developer.android.com/develop/ui/compose/tooling/previews)、[字符串资源](https://developer.android.com/develop/ui/compose/resources)。
