# Compose 从零到进阶实战

面向**已经会 Android/Kotlin，但没写过 Jetpack Compose** 的开发者。这里不是一组只供阅读的示例：`app` 是可运行的练习场，`Exercises.kt` 是你亲手修改的文件，`lessons/` 是按顺序完成的讲义。

## 先看看会做出什么

![第 07 课任务清单的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/07.png)

这张是**完成第 07 课后的目标效果示意**，不是当前 `TODO` 练习的运行截图。每节讲义也有自己的目标画面；先看成果，再按问题一步步写出来。

![第 07 课可操作示范在 Android 模拟器中的实际画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/07-running.png)

这张是**真实模拟器运行截图**。第 1–6 课讲义中也有真实截图，可与目标示意图对照。

## 再看原理图

![从 View 转向 Compose 的概念图](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01.png)

图中从左到右是“旧思路 → 输入 → Compose → 结果”。先试着回答：如果名字改变，哪一步负责更新屏幕？再打开第 1 课写代码验证。

## 5 分钟开始

1. 用 Android Studio 打开本目录，等待 Gradle Sync。需要 JDK 17、Android SDK 37 和联网下载依赖。
2. 运行 `app` 到模拟器或真机。主页有 20 个课程入口。
3. 在 App 首页打开第 1 课，先体验可操作示范，再回练习页点图放大：按箭头读图，先回答预测题，再看“三小步跟做”。第 1–7 课均有可操作示范。
4. 打开 [第 1 课讲义](lessons/01-first-composable.md)，修改 [`Exercises.kt`](app/src/main/java/dev/learning/compose/Exercises.kt) 中对应函数；每做完一小步就运行一次。
5. 对照讲义的“停下来验收”检查结果；卡住时看“若结果不同”与提示，最后才看参考实现。

命令行构建：`./gradlew :app:assembleDebug`。首次构建需要下载 Gradle 和 Maven 依赖。

每节讲义都配有**目标界面示意图 + 原理流程图**：先看完成后会是什么样，再按问题写代码。目标图在 `assets/effects/`，原理图在 `assets/diagrams/`；App 内也能点击放大原理图。当前 Android Studio 的实验性 Compose Markdown 预览无法解析相对图片路径，因此讲义使用 GitHub 图片地址，在线阅读和 IDE 预览都能显示。若需离线预览，运行 `python3 tools/refresh_markdown_image_paths.py` 改为本机图片路径；上传改动前请运行 `python3 tools/refresh_markdown_image_paths.py --github` 恢复 GitHub 图片地址。

第 1–7 课还有**真正运行的示范页**，可以输入、点击、旋转并验证预测。示范代码与 `Exercises.kt` 分开，你的练习不会被答案覆盖。建议先体验行为，再回讲义逐步写，完成验收后才读示范源码。

## 学习路线

| 阶段 | 课程 | 能力目标 |
| --- | --- | --- |
| 入门 | 01 Composable、02 状态 | 从 `setContent` 到可交互 UI |
| 基础 | 03 输入与状态提升、04 布局 | 单向数据流与 Modifier |
| 常用界面 | 05 列表、06 副作用 | 列表身份、协程与生命周期 |
| 阶段项目 | 07 任务清单 | 独立整合基础能力 |
| 工程化 | 08 架构、09 导航、10 测试、11 质量、12 互操作 | 将 Compose 接入真实项目 |
| 深入 UI | 13 动画、14 手势、15 绘制与布局 | 理解复杂交互与渲染 |
| 真实数据 | 16 状态恢复、17 异步数据 | 离线、错误、取消与恢复 |
| 交付能力 | 18 自适应、19 性能、20 综合毕业项目 | 多设备验证与证据驱动优化 |

01–06 每节约 45–90 分钟；07 约 3–5 小时；08–19 每节约 1–3 小时；20 建议用 2–4 周独立完成。01–07 修改 `Exercises.kt`，13–15 可先在 `AdvancedExercises.kt` 小练，再迁移到任务清单；其余进阶课直接扩展 07 项目。每节都先自己写，再看提示，最后按验收脚本验证。

“精通”不是读完目录：至少要完成 20 的交付要求，能解释状态归属、生命周期、性能测量，并能独立排查一个真实 UI 问题。

## 学习约定

- `@Composable` 函数描述当前状态应呈现什么 UI；不要把它理解为“一次性创建 View”。
- 状态归属于需要它的最低共同父组件；子组件尽量只接收值和事件。
- 先让功能正确，再考虑模块拆分、架构库或性能优化。本项目刻意只保留一个 app 模块。
- 参考实现是一种做法，不是唯一答案。每课都安排了变式练习，避免照抄后产生掌握错觉。

## 课程目录

1. [第一个 Composable](lessons/01-first-composable.md)
2. [状态与重组](lessons/02-state.md)
3. [输入与单向数据流](lessons/03-input.md)
4. [布局与 Modifier](lessons/04-layout.md)
5. [列表与 key](lessons/05-list.md)
6. [副作用](lessons/06-effects.md)
7. [阶段项目：任务清单](lessons/07-capstone.md)
8. [状态持有者与架构](lessons/08-architecture.md)
9. [导航与返回栈](lessons/09-navigation.md)
10. [UI 测试](lessons/10-testing.md)
11. [质量、无障碍与预览](lessons/11-quality.md)
12. [与 View 互操作](lessons/12-interop.md)
13. [状态驱动动画](lessons/13-animation.md)
14. [手势与交互冲突](lessons/14-gestures.md)
15. [自定义绘制与布局](lessons/15-drawing-layout.md)
16. [状态恢复与离线持久化](lessons/16-state-persistence.md)
17. [异步数据、错误与取消](lessons/17-async-data.md)
18. [自适应布局与多窗口](lessons/18-adaptive.md)
19. [性能实战](lessons/19-performance.md)
20. [综合毕业项目](lessons/20-mastery-project.md)

## 官方资料

- [Compose 入门](https://developer.android.com/develop/ui/compose/setup)
- [Compose 思维模型](https://developer.android.com/develop/ui/compose/mental-model)
- [状态管理](https://developer.android.com/develop/ui/compose/state)
- [Compose 路线图](https://developer.android.com/develop/ui/compose/documentation)

版本固定在创建项目时的稳定组合：AGP 9.4.0、Gradle 9.6.0、Compose BOM 2026.09.00。后续升级请先看官方兼容表。
