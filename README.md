# Compose 从零到进阶实战

面向**已经会 Android/Kotlin，但没写过 Jetpack Compose** 的开发者。这里不是一组只供阅读的示例：`app` 是可运行的练习场，`Exercises.kt` 是你亲手修改的文件，`lessons/` 是按顺序完成的讲义。

## 先看看会做出什么

![第 07 课任务清单的目标界面示意](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/effects/07.png)

这张是**完成第 07 课后的目标效果示意**，不是当前 `TODO` 练习的运行截图。每节讲义也有自己的目标画面；先看成果，再按问题一步步写出来。

![第 07 课可操作示范在 Android 模拟器中的实际画面](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/screenshots/07-running.png)

这张是**真实模拟器运行截图**。第 1–6 课讲义中也有真实截图，可与目标示意图对照。

## 再看原理图

![第一课从 Activity 到练习区的调用链图解](https://raw.githubusercontent.com/god-w/compose-from-zero/main/assets/diagrams/01.png)

第一章从你熟悉的 XML、Activity 和 TextView 出发，解释 `setContent` 如何调用到练习函数、`@Composable` 的作用，以及组合、布局、绘制怎样产生界面。先完成理解检查，再按“改文字 → 改排列 → 传名字”动手；每步都有预测、完整代码、验证与解析。

## 5 分钟开始

1. 用 Android Studio 打开本目录，等待 Gradle Sync。需要 JDK 17、Android SDK 37 和联网下载依赖。
2. 运行 `app` 到模拟器或真机。主页有 20 个课程入口。
3. 在 App 首页打开第 1 课，在练习区找到占位文字。阅读第一章 1–6 节，用 XML/View 对照理解原理；再按第 7 节动手实验。完成静态练习后再体验示范中的按钮。第 1–7 课均有可操作示范。
4. 打开 [第 1 课讲义](lessons/01-first-composable.md)，修改 [`Exercises.kt`](app/src/main/java/dev/learning/compose/Exercises.kt) 中对应函数；每做完一小步就运行一次。
5. 完成讲义中的验收；第 1 课还要解释调用链并做迁移题。卡住时按现象回读对应小节，先自己作答，再展开解析。

命令行构建：`./gradlew :app:assembleDebug`。首次构建需要下载 Gradle 和 Maven 依赖。

每节讲义都配有**目标界面示意图 + 原理流程图**：先看完成后会是什么样，再按问题写代码。目标图在 `assets/effects/`，原理图在 `assets/diagrams/`；App 内也能点击放大原理图。当前 Android Studio 的实验性 Compose Markdown 预览无法解析相对图片路径，因此讲义使用 GitHub 图片地址，在线阅读和 IDE 预览都能显示。若需离线预览，运行 `python3 tools/refresh_markdown_image_paths.py` 改为本机图片路径；上传改动前请运行 `python3 tools/refresh_markdown_image_paths.py --github` 恢复 GitHub 图片地址。

第 1–7 课还有**真正运行的示范页**，可以输入、点击、旋转并验证预测。示范代码与 `Exercises.kt` 分开，你的练习不会被答案覆盖。第 1 课先建立静态界面的理解；第 2–7 课建议先体验行为，再回讲义逐步写，完成验收后才读示范源码。

## 学习路线

第 2–20 章已按“原理 → 预测 → 分步代码 → 运行验证 → 解释与迁移”补齐。每章有目标效果、核心路径、独立界面对照图、答案解析与排错表；第 2–7 章保留真实模拟器截图，第 13–15、17–18 章新增讲义样本的真实运行截图。第 7 章建立统一的 `Task` 与 `TaskContent`，后续章节明确哪些继续扩展它、哪些建立独立实验。

使用[学习检查表](lessons/LEARNING-CHECKLIST.md)记录自己能解释、能独立完成、能排错的内容。导航、Room 和测试等新增依赖在对应章节给出文件位置与配置，不需要入门时一次全部添加。

样本的编译、自动检查与实际交互范围见[课程验证记录](lessons/VERIFICATION.md)。验证记录区分已检查的行为和需要你独立完成的挑战。

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

01–06 每节约 45–90 分钟；07 约 3–5 小时；08–19 每节约 1–3 小时；20 建议用 2–4 周独立完成。01–07 修改 `Exercises.kt`；08–11、16 继续扩展或验证 07；12、17–19 给出独立实验的接入位置；13–15 在 `AdvancedExercises.kt` 小练后再迁移到项目。第 20 章独立实现习惯打卡。时间为参考，能解释和验证后再继续。

维护课程时，可运行 `python3 tools/check_lessons.py` 检查图片、本地链接、代码块和答案区域。新增界面对照图由 `tools/build_experiment_visuals.py` 生成，需要 Pillow；这些 Python 工具只维护教材图片和文档，Android app 使用 Kotlin / Gradle 构建。

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
