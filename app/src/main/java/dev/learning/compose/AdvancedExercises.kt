package dev.learning.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * 08–11、16 课扩展或验证 07 的任务清单；先读对应讲义，再改 07 的实现。
 * 12、17–19 课按讲义创建独立实验，并把入口加入下面的 when。
 * 20 课独立实现习惯打卡应用。
 * 13–15 可先在下面的练习函数做小实验，然后迁移到任务清单。
 */
@Composable
fun AdvancedPracticeScreen(lesson: Int) {
    when (lesson) {
        12 -> AnimationExercise()
        13 -> GestureExercise()
        14 -> DrawingExercise()
        else -> Text("第 ${lesson + 1} 课：先读对应讲义的文件位置与接入步骤，再完成实验和验收。")
    }
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
