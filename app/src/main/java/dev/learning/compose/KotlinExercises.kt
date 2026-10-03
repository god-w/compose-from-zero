package dev.learning.compose

// 每章只替换对应函数体。完整函数签名已给出，先完成讲义的小步骤，再做变式题。
// 练习与示范分开；App 不会自动把参考答案写到这里。
fun kotlinExercise01(): String {
    val name = "小明"
    var count = 0
    return "$name：$count"
}
fun kotlinExercise02(): String = "K02：在这里写问候函数与求和循环"
fun kotlinExercise03(): String = "K03：在这里处理空名字与数字输入"
fun kotlinExercise04(): String = "K04：在这里传入并执行 lambda"
fun kotlinExercise05(): String = "K05：在这里写扩展函数与 let"
fun kotlinExercise06(): String = "K06：在这里创建 data class 并 copy"
fun kotlinExercise07(): String = "K07：在这里过滤任务并计算完成数"
fun kotlinExercise08(): String = "K08：在这里处理三种 KotlinLoad 状态"
fun kotlinExercise09(): String = "K09：在这里调用泛型函数"
fun kotlinExercise10(): String = "K10：在这里体验 KotlinScore 与 lazy"
suspend fun kotlinExercise11(): String = "K11：在这里启动并取消子协程"
suspend fun kotlinExercise12(): String = "K12：在这里收集 Flow 两次"
fun kotlinExercise13(): String = "K13：在这里安全调用旧 Java API"
fun kotlinExercise14(): String = "K14：在这里用事件更新任务状态"

suspend fun runKotlinExercise(index: Int): String = when (index) {
    0 -> kotlinExercise01()
    1 -> kotlinExercise02()
    2 -> kotlinExercise03()
    3 -> kotlinExercise04()
    4 -> kotlinExercise05()
    5 -> kotlinExercise06()
    6 -> kotlinExercise07()
    7 -> kotlinExercise08()
    8 -> kotlinExercise09()
    9 -> kotlinExercise10()
    10 -> kotlinExercise11()
    11 -> kotlinExercise12()
    12 -> kotlinExercise13()
    13 -> kotlinExercise14()
    else -> error("Unknown Kotlin exercise: $index")
}
