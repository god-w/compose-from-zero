package dev.learning.compose

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

// 普通 Kotlin：这些函数和模型不依赖 Compose。
data class KotlinTask(val id: Int, val title: String, val done: Boolean = false)
data class KotlinTaskState(val tasks: List<KotlinTask> = emptyList(), val nextId: Int = 1)
sealed interface KotlinTaskEvent {
    data class Add(val title: String) : KotlinTaskEvent
    data class Toggle(val id: Int) : KotlinTaskEvent
    data class Delete(val id: Int) : KotlinTaskEvent
}

fun reduceKotlinTasks(state: KotlinTaskState, event: KotlinTaskEvent): KotlinTaskState = when (event) {
    is KotlinTaskEvent.Add -> {
        val title = event.title.trim()
        if (title.isEmpty()) state else state.copy(
            tasks = state.tasks + KotlinTask(state.nextId, title), nextId = state.nextId + 1
        )
    }
    is KotlinTaskEvent.Toggle -> state.copy(tasks = state.tasks.map {
        if (it.id == event.id) it.copy(done = !it.done) else it
    })
    is KotlinTaskEvent.Delete -> state.copy(tasks = state.tasks.filterNot { it.id == event.id })
}

sealed interface KotlinLoad {
    data object Loading : KotlinLoad
    data class Success(val count: Int) : KotlinLoad
    data class Error(val message: String) : KotlinLoad
}
fun describeKotlinLoad(state: KotlinLoad): String = when (state) {
    KotlinLoad.Loading -> "加载中"
    is KotlinLoad.Success -> "共 ${state.count} 项"
    is KotlinLoad.Error -> "失败：${state.message}"
}
fun <T> kotlinFirst(values: List<T>): T? = values.firstOrNull()
inline fun <reified T> kotlinFirstOfType(values: List<Any>): T? = values.filterIsInstance<T>().firstOrNull()
class KotlinScore : ReadWriteProperty<Any?, Int> {
    private var value = 0
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        this.value = value.coerceIn(0, 10)
    }
}
object KotlinGreetings {
    @JvmStatic fun greet(name: String): String = "你好，$name"
}

val kotlinExpected = listOf(
    "小明：3；比例=0.5", "你好，小明；总和=6", "匿名；42", "结果=6；调用=1",
    "标题=A；长度=1", "原来=false；副本=true；共享标签=true", "未完成=[B]；完成=1",
    "加载中 / 共 2 项 / 失败：断网", "首项=A；首个数字=7", "分数=10；初始化=1",
    "A+B；cancelled=true", "[10, 20] / [10, 20]；启动=2；state=1", "匿名；你好，小明",
    "A：完成；总数=1；下一ID=3"
)

suspend fun runKotlinExample(index: Int): String = when (index) {
    0 -> {
        val name = "小明"
        var count = 0
        count += 3
        "$name：$count；比例=${5.toDouble() / 10}"
    }
    1 -> {
        fun greet(name: String, prefix: String = "你好") = "$prefix，$name"
        var sum = 0
        for (number in 1..3) sum += number
        "${greet(name = "小明")}；总和=$sum"
    }
    2 -> {
        val raw: String? = null
        val name = raw?.trim()?.takeIf { it.isNotEmpty() } ?: "匿名"
        val age = "42".toIntOrNull() ?: 0
        "$name；$age"
    }
    3 -> {
        fun applyOperation(value: Int, action: (Int) -> Int): Int = action(value)
        var calls = 0
        val result = applyOperation(3) { number -> calls++; number * 2 }
        "结果=$result；调用=$calls"
    }
    4 -> {
        fun String.cleanTitle() = trim().uppercase()
        val title = " a ".cleanTitle()
        val length = title.let { it.length }
        "标题=$title；长度=$length"
    }
    5 -> {
        data class Draft(val done: Boolean, val tags: MutableList<String>)
        val original = Draft(false, mutableListOf("学习"))
        val copy = original.copy(done = true)
        copy.tags.add("Kotlin")
        "原来=${original.done}；副本=${copy.done}；共享标签=${original.tags === copy.tags}"
    }
    6 -> {
        val tasks = listOf(KotlinTask(1, "A", true), KotlinTask(2, "B"))
        val titles = tasks.filterNot { it.done }.map { it.title }
        "未完成=$titles；完成=${tasks.count { it.done }}"
    }
    7 -> listOf(KotlinLoad.Loading, KotlinLoad.Success(2), KotlinLoad.Error("断网"))
        .joinToString(" / ", transform = ::describeKotlinLoad)
    8 -> "首项=${kotlinFirst(listOf("A", "B"))}；首个数字=${kotlinFirstOfType<Int>(listOf("A", 7))}"
    9 -> {
        var score by KotlinScore()
        score = 99
        var calls = 0
        val label by lazy { calls++; "已初始化" }
        check(label == label)
        "分数=$score；初始化=$calls"
    }
    10 -> coroutineScope {
        val a = async { delay(50); "A" }
        val b = async { delay(20); "B" }
        val result = "${a.await()}+${b.await()}"
        val job = launch { delay(1000) }
        job.cancelAndJoin()
        "$result；cancelled=${job.isCancelled}"
    }
    11 -> {
        var starts = 0
        val source = flow { starts++; emit(1); emit(2) }
        val first = source.map { it * 10 }.toList()
        val second = source.map { it * 10 }.toList()
        val state = MutableStateFlow(0)
        state.value = 1
        "$first / $second；启动=$starts；state=${state.value}"
    }
    12 -> {
        val name: String? = LegacyNames.optionalName(false)
        "${name ?: "匿名"}；${KotlinGreetings.greet("小明")}"
    }
    13 -> {
        var state = KotlinTaskState()
        for (event in listOf(KotlinTaskEvent.Add(" A "), KotlinTaskEvent.Add("B"),
            KotlinTaskEvent.Toggle(1), KotlinTaskEvent.Delete(2))) {
            state = reduceKotlinTasks(state, event)
        }
        "${state.tasks.single().title}：完成；总数=${state.tasks.size}；下一ID=${state.nextId}"
    }
    else -> error("Unknown Kotlin lesson: $index")
}

// 学习检查：既检查可见结果，也检查任务规则的边界。不会运行或覆盖练习答案。
suspend fun verifyKotlinExamples(): String {
    kotlinExpected.forEachIndexed { index, expected ->
        check(runKotlinExample(index) == expected) { "K${index + 1} 示范结果不符" }
    }
    val empty = KotlinTaskState()
    check(reduceKotlinTasks(empty, KotlinTaskEvent.Add("  ")) == empty)
    val added = reduceKotlinTasks(empty, KotlinTaskEvent.Add(" A "))
    check(added.tasks.single().title == "A" && added.nextId == 2)
    val toggled = reduceKotlinTasks(added, KotlinTaskEvent.Toggle(1))
    check(!added.tasks.single().done && toggled.tasks.single().done)
    check(toggled.tasks.single().id == added.tasks.single().id)
    check(reduceKotlinTasks(added, KotlinTaskEvent.Toggle(999)) == added)
    val deleted = reduceKotlinTasks(toggled, KotlinTaskEvent.Delete(1))
    check(deleted.tasks.isEmpty() && deleted.nextId == 2)
    check(kotlinFirst(emptyList<String>()) == null)
    check(kotlinFirstOfType<Int>(listOf("A")) == null)
    var score by KotlinScore()
    score = -1
    check(score == 0)
    return "通过：14 个示范 + 9 项边界检查。练习请逐章自行验收。"
}
