package dev.learning.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
internal fun LessonVisual(lesson: Int) {
    val guide = visualGuides[lesson]
    var expanded by remember(lesson) { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth().padding(top = 12.dp).clickable { expanded = true }) {
        Column {
            Image(
                painter = painterResource(lessonDiagrams[lesson]),
                contentDescription = "第 ${lesson + 1} 课概念图，点击查看完整图解",
                modifier = Modifier.fillMaxWidth().height(128.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
            Text("点图放大 · 先预测，再写代码", Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge)
        }
    }
    Text(guide.question, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium)

    if (expanded) {
        Dialog(onDismissRequest = { expanded = false }) {
            Surface(shape = MaterialTheme.shapes.large) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                    Image(
                        painter = painterResource(lessonDiagrams[lesson]),
                        contentDescription = "第 ${lesson + 1} 课完整概念图",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                    Text("先猜：${guide.question}", Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
                    guide.steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = { expanded = false }) { Text("开始动手") }
                }
            }
        }
    }
}
