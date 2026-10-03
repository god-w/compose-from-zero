package dev.learning.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val kotlinTitles = listOf(
    "值、类型与字符串", "函数与控制流程", "可空类型与安全输入", "函数类型与 lambda",
    "接收者、扩展与作用域函数", "类、属性与数据类", "集合与不可变更新", "密封类型与状态建模",
    "泛型与类型边界", "委托属性与 by", "协程与取消", "Flow 与 StateFlow",
    "注解与 Java 互操作", "综合练习：纯 Kotlin 任务规则"
)
private val kotlinQuestions = listOf(
    "5 / 10 为什么是 0？val 的含义是什么？", "参数在哪里声明，实参在哪里传入？",
    "null、空字符串与空白字符串是一回事吗？", "传入 { } 后，谁负责调用它？",
    "let 返回什么？apply 返回什么？this 与 it 指谁？", "copy 复制属性值，还是递归复制对象？",
    "filter 会删除原列表中的元素吗？", "三种状态能否同时存在？when 能否漏分支？",
    "同一个函数为什么能接收 List<String> 与 List<Int>？", "by 把读写交给谁？它会自动刷新 UI 吗？",
    "suspend 会自动切后台线程吗？退出页面后任务属于谁？", "同一个冷流收集两次，执行体跑几次？",
    "Java 返回值一定非空吗？注解会自己执行函数吗？", "界面之外，如何证明添加、完成与删除规则正确？"
)

@Composable
fun KotlinLab(modifier: Modifier = Modifier) {
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    BackHandler(selected >= 0) { selected = -1 }
    if (selected < 0) {
        LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("先读 lessons/kotlin/README.md。每课先预测，再运行示范；修改 KotlinExercises.kt 后重建 App，点击运行我的练习。", style = MaterialTheme.typography.bodyLarge) }
            item { KotlinChecks() }
            itemsIndexed(kotlinTitles) { index, title ->
                Card(Modifier.fillMaxWidth().clickable { selected = index }) {
                    Column(Modifier.padding(18.dp)) {
                        Text("K${"%02d".format(index + 1)} · $title", style = MaterialTheme.typography.titleMedium)
                        Text(kotlinQuestions[index])
                    }
                }
            }
        }
    } else key(selected) {
        KotlinLesson(selected, modifier) { selected = -1 }
    }
}

@Composable
private fun KotlinChecks() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("参考示范的检查不会替你完成练习。") }
    var busy by remember { mutableStateOf(false) }
    TextButton(enabled = !busy, onClick = {
        busy = true
        scope.launch {
            try { result = verifyKotlinExamples() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { result = "检查失败：${failure.message}" }
            finally { busy = false }
        }
    }) { Text(if (busy) "检查中…" else "检查全部参考示范") }
    Text(result)
}

@Composable
private fun KotlinLesson(index: Int, modifier: Modifier, back: () -> Unit) {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("先在纸上写下预测，再点击按钮。") }
    var busy by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = back) { Text("返回 Kotlin 目录") }
        Text("K${"%02d".format(index + 1)} · ${kotlinTitles[index]}", style = MaterialTheme.typography.headlineSmall)
        Text(kotlinQuestions[index], style = MaterialTheme.typography.titleMedium)
        Text("讲义：lessons/kotlin/K${"%02d".format(index + 1)}-*.md\n动手：KotlinExercises.kt → kotlinExercise${"%02d".format(index + 1)}()")
        Text("第一步：预测示范结果。第二步：运行并解释差异。第三步：修改自己的练习、重新运行 App。")
        Button(enabled = !busy, onClick = {
            busy = true
            scope.launch {
                try { result = "参考示范：\n${runKotlinExample(index)}" }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (failure: Exception) { result = "示范异常：${failure.message}" }
                finally { busy = false }
            }
        }) { Text("运行参考示范") }
        Button(enabled = !busy, onClick = {
            busy = true
            scope.launch {
                try { result = "我的练习：\n${runKotlinExercise(index)}" }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (failure: Exception) { result = "练习异常：${failure::class.simpleName}：${failure.message}" }
                finally { busy = false }
            }
        }) { Text("运行我的练习") }
        Card(Modifier.fillMaxWidth()) {
            Text(result, Modifier.padding(18.dp), style = MaterialTheme.typography.bodyLarge)
        }
        Text("小步完成的目标：${kotlinExpected[index]}\n目标不是死记这行输出：还要完成讲义中的变式与边界验收。")
        Text("K11–12 的任务属于当前练习页。返回 Kotlin 目录会取消此页的协程；suspend 本身不会把计算移到后台。")
    }
}
