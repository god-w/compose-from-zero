package dev.learning.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * 08、10–11、16 课扩展或验证 07 的任务清单。
 * 09、12、17–19 各有独立入口；按对应讲义只替换入口函数的内容。
 * 20 课独立实现习惯打卡应用。
 * 13–15 可先在下面的练习函数做小实验，然后迁移到任务清单。
 */
@Composable
fun AdvancedPracticeScreen(lesson: Int) {
    when (lesson) {
        8 -> NavigationExerciseEntry()
        11 -> InteropExerciseEntry()
        12 -> AnimationExercise()
        13 -> GestureExercise()
        14 -> DrawingExercise()
        16 -> AsyncExerciseEntry()
        17 -> AdaptiveExerciseEntry()
        18 -> PerformanceExerciseEntry()
        else -> Text("第 ${lesson + 1} 课：先读对应讲义的文件位置与接入步骤，再完成实验和验收。")
    }
}

@Composable
private fun NavigationExerciseEntry() {
    // TODO 09: 完成 TaskNavigation.kt 后，把下一行换成 TaskNavigation()。
    Text("09 · 在这里练习列表、详情与返回栈")
}

@Composable
private fun InteropExerciseEntry() {
    // TODO 12: 在这里接入讲义中的 AndroidView 实验。
    Text("12 · 在这里练习 View 互操作")
}

@Composable
private fun AsyncExerciseEntry() {
    // TODO 17: 完成 AsyncTaskScreen 后，把下一行换成 AsyncTaskScreen()。
    Text("17 · 在这里练习异步、错误与取消")
}

@Composable
private fun AdaptiveExerciseEntry() {
    // TODO 18: 完成 AdaptiveTasks 后，把下一行换成 AdaptiveTasks()。
    Text("18 · 在这里练习窄屏与宽屏")
}

@Composable
private fun PerformanceExerciseEntry() {
    // TODO 19: 完成 PerformanceExercise 后，把下一行换成 PerformanceExercise()。
    Text("19 · 在这里练习测量与优化")
}

@Composable
private fun AnimationExercise() {
    // TODO 13: 用 animateColorAsState 和 AnimatedVisibility 制作可展开任务卡。
    Text("13 · 在这里练习状态驱动动画")
}

@Composable
private fun GestureExercise() {
    // TODO 14: 用 clickable 和拖动手势做任务卡，保留可访问的按钮操作。
    Text("14 · 在这里练习手势")
}

@Composable
private fun DrawingExercise() {
    // TODO 15: 用 Canvas 画完成进度环，再尝试用 Layout 写一个标签流。
    Text("15 · 在这里练习绘制与测量")
}
