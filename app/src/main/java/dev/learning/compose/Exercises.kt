package dev.learning.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 这是你的练习文件。按 lessons/ 的顺序，每次只改一个函数。
 * 所有函数起初都能编译运行；每完成一节，重新运行 App 看变化。
 */
@Composable
fun PracticeScreen(lesson: Int) {
    Column {
        Spacer(Modifier.height(24.dp))
        when (lesson) {
            0 -> GreetingExercise()
            1 -> CounterExercise()
            2 -> InputExercise()
            3 -> LayoutExercise()
            4 -> ListExercise()
            5 -> EffectExercise()
            6 -> CapstoneExercise()
            else -> AdvancedPracticeScreen(lesson)
        }
    }
}

@Composable
private fun GreetingExercise() {
    // 第 1 小步：只把下面引号里的内容改成“你好，Compose！”，再运行 app。
    // 第 2 小步：读讲义，用 Column 上下排列两个 Text。
    // 第 3 小步：再学习 name 参数；定义和调用要一起修改。
    Text("01 · 在这里写第一个 Composable")
}

@Composable
private fun CounterExercise() {
    // TODO 02: rememberSaveable + mutableIntStateOf；按钮点击后计数加一。
    Text("02 · 在这里写计数器")
}

@Composable
private fun InputExercise() {
    // TODO 03: 做一个可输入名字的 TextField；将输入值显示在下方。
    // 进阶：把 TextField 拆成无状态 NameField(value, onValueChange)。
    Text("03 · 在这里写输入框")
}

@Composable
private fun LayoutExercise() {
    // TODO 04: 组合 Row、Column、Modifier；制作个人资料卡。
    Text("04 · 在这里写资料卡")
}

@Composable
private fun ListExercise() {
    // TODO 05: 用 LazyColumn 显示可增删的任务；给 items 指定稳定 key。
    Text("05 · 在这里写任务列表")
}

@Composable
private fun EffectExercise() {
    // TODO 06: 用 LaunchedEffect 让秒数自动增加；离开页面时自动停止。
    Text("06 · 在这里写计时器")
}

@Composable
private fun CapstoneExercise() {
    // TODO 07: 在这节整合输入、列表、过滤、状态提升与可访问性。
    Text("07 · 在这里写阶段项目")
}
