# Kotlin 从基础到 Compose · 学习入口

[回到项目首页](../../README.md)

你有 Android 基础，仍可能在 Compose 代码里被 `by`、花括号、泛型和协程卡住。这条路线先把语言规则拆开，再回到界面；不要求已经熟练掌握 Kotlin。

## 第一次怎么开始

1. Android Studio 打开项目并运行 app。首页选择“Kotlin 从基础到 Compose · 14 章”。
2. 从 [K01 值与类型](K01-values.md) 读起。先看图，再手写预测。
3. 进入 App 对应章，点“运行参考示范”，观察结果。示范源码在 KotlinExamples.kt；先做预测再打开。
4. 修改 [KotlinExercises.kt](../../app/src/main/java/dev/learning/compose/KotlinExercises.kt) 中对应函数，Run app 重建后点“运行我的练习”。练习不使用 main/println：函数返回 String，由 App 显示。不要把函数复制到 MainActivity。
5. 做完小步才展开答案，最后做变式与边界检查。若输出不一致，先确认自己点击的是哪个按钮。

K01 提供可运行的初始函数，其他章是可编译占位；每章讲义告诉你怎么逐步替换。普通函数只需要 Kotlin，K11/12 额外使用项目已有协程库；UI 外壳已接好。按钮的协程属于当前练习页，返回目录会取消，不使用阻塞主线程的 runBlocking。

## 第一次修改 Kotlin 文件时

文件开头的 `package dev.learning.compose` 表示所属包，保持原样。`import` 放在 package 后、函数前；Android Studio 对未导入名称通常提示 **Alt/Option + Enter → Import**，也能手动添加 `import kotlinx.coroutines.async`。不要把 import 写进函数体。

`fun kotlinExercise01(): String { ... }` 外面的名字和 String 是 App 找到练习的约定，先保留。讲义“函数体”代码填在这对大括号里；只替换里面原有内容，不要把答案接到原来的 return 后面。若函数起初用 `= "占位"`，可改成同名的 `{ ... }` 形式，仍保留 `: String`。例如：

```kotlin
fun kotlinExercise02(): String {
    // 在这里写本章步骤；最后 return 一个 String。
    return "先确认我改对了函数"
}
```

K11、K12 的签名前还要保留 `suspend`。两种代码块是不同用途：`text` 仅展示输出，`kotlin` 才是代码。完整函数体通常包含 return；只读注解或 Compose 的示意片段会在本章明确标出。

## 选一条学习顺序

- **从零补齐语言：**按 K01→K14，每章解释、独写、变式都过关，再走 Compose 01→20。
- **边做界面边补：**先 K01–05，再读 Compose 01–04；K06–10 后读 Compose 05/07；K11–12 后读 Compose 06/08/17；K13 配合 View 互操作；K14 收拢任务规则。
- K09 的 variance/reified、K13 的桥接是进阶阅读，首次可先完成基本练习，稍后回读细节；不是第一天必须背完的知识。

## 课程目录与检查表

| 章 | 解决的问题 | Compose 对应 |
| --- | --- | --- |
| [K01 值、类型与字符串](K01-values.md) | 能逐个解释 val、var、类型、赋值和字符串模板，并独立计算一个正确的完成比例。 | 01/02/15 |
| [K02 函数与控制流程](K02-functions.md) | 能读懂函数签名、传入实参、返回结果，并用 if、when 和循环表达规则。 | 01/03 |
| [K03 可空类型与安全输入](K03-null-safety.md) | 能区分 null、空与空白，并把不可信文本转换为有明确兜底的数据。 | 03/09/18 |
| [K04 函数类型与 lambda](K04-lambdas.md) | 把回调还原成普通函数值，解释花括号、箭头、it 和尾随 lambda 的执行时机。 | 03/06 |
| [K05 接收者、扩展与作用域函数](K05-receivers.md) | 能辨认 this 与 it，按返回值选择作用域函数，并读懂 Compose 的接收者作用域。 | 04/15 |
| [K06 类、属性与数据类](K06-classes.md) | 能定义一个有身份的任务对象，理解 copy、相等与共享可变属性。 | 07/08 |
| [K07 集合与不可变更新](K07-collections.md) | 能读懂 filter/map/count，保留完整源数据，并用新列表表达修改。 | 05/07/19 |
| [K08 密封类型与状态建模](K08-sealed-state.md) | 用互斥类型表达加载、成功、错误，利用编译器检查分支是否覆盖。 | 17 |
| [K09 泛型与类型边界](K09-generics.md) | 能辨认 T 与具体类型、可空返回值、协变与逆变，并知道 reified 的使用边界。 | 08 |
| [K10 委托属性与 by](K10-delegation.md) | 能把委托语法展开成读写调用，并区分 Kotlin 语法与 Compose 状态行为。 | 02 |
| [K11 协程与取消](K11-coroutines.md) | 能解释挂起、作用域、等待、取消和调度，并写一段不会阻塞界面的并发练习。 | 06/17 |
| [K12 Flow 与 StateFlow](K12-flow.md) | 能区分流的创建、收集、冷流与当前状态，避免无限等待与重复工作。 | 08/16/17 |
| [K13 注解与 Java 互操作](K13-interop.md) | 能解释注解的作用边界，安全处理平台类型，并读懂旧 Android/Java API。 | 01/12 |
| [K14 综合练习：纯 Kotlin 任务规则](K14-integration.md) | 独立写规则、验证边界，再把结果交给 Compose，而不是让业务逻辑依赖屏幕。 | 07/08 |

## 学习检查表

下面三个层次分别记录，看到示范结果不等于能独立写出。

| 章 | 能解释类型/执行关系 | 能关闭答案独写 | 能通过变式与边界 |
| --- | --- | --- | --- |
| K01 | □ | □ | □ |
| K02 | □ | □ | □ |
| K03 | □ | □ | □ |
| K04 | □ | □ | □ |
| K05 | □ | □ | □ |
| K06 | □ | □ | □ |
| K07 | □ | □ | □ |
| K08 | □ | □ | □ |
| K09 | □ | □ | □ |
| K10 | □ | □ | □ |
| K11 | □ | □ | □ |
| K12 | □ | □ | □ |
| K13 | □ | □ | □ |
| K14 | □ | □ | □ |

## 语法快速回查

| 看到的写法 | 去哪里拆解 |
| --- | --- |
| `$name`、`${...}`、`val`/`var` | K01 |
| 默认值、具名实参、`when` | K02 |
| `?`、`?.`、`?:`、`!!` | K03 |
| `(String) -> Unit`、`{ it }`、`::` | K04 |
| `RowScope.() -> Unit`、`this`、`apply` | K05 |
| `data class`、`copy`、`==`/`===` | K06 |
| `map`、`filterNot`、`List`/`MutableList` | K07 |
| `sealed`、`data object`、`is` | K08 |
| `<T>`、`out`/`in`、`reified` | K09 |
| `by`、`lazy`、`getValue`/`setValue` | K10 |
| `suspend`、`launch`、`async`、取消 | K11 |
| `Flow`、`StateFlow`、`collect` | K12 |
| `@...`、平台类型、Java lambda | K13 |
| 事件、旧状态→新状态、断言 | K14 |

## 怎样判断真正掌握

每章至少保留一次错误预测的原因；K14 独立写 reducer，通过空白、无效 id、旧快照、删除后添加等边界检查，再在 Compose 07 上迁移。之后还能自己设计不同规则的应用、解释取舍并排错，才是在往熟练与精通推进。这 14 章覆盖本 Compose 项目的核心语言前置，不声称覆盖 Kotlin 多平台、编译器开发和所有后端场景。

App 的“检查全部参考示范”验证 14 个示范输出和 9 项规则边界，验证范围见 [课程验证记录](../VERIFICATION.md)。它不检查你的 14 个练习，也不会替你完成学习验收。
