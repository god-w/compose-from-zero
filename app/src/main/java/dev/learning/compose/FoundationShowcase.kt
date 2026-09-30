package dev.learning.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Small interactive results for lessons 01–03; the student's exercise code stays untouched. */
@Composable
internal fun FoundationShowcase(lesson: Int, modifier: Modifier = Modifier) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (lesson) {
            0 -> GreetingShowcase()
            1 -> CounterShowcase()
            2 -> InputShowcase()
        }
    }
}

@Composable
private fun GreetingShowcase() {
    var name by rememberSaveable { mutableStateOf("小明") }
    Text("先预测：点按钮后，哪一行会改变？", style = MaterialTheme.typography.titleMedium)
    Text("你好，Compose！", style = MaterialTheme.typography.headlineMedium)
    Text("我是 $name")
    Button(onClick = { name = if (name == "小明") "小红" else "小明" }) { Text("切换传入的名字") }
    Text("观察：标题保持不变；显示 name 的那行随输入更新。回到练习，把 name 改为函数参数。")
}

@Composable
private fun CounterShowcase() {
    var count by rememberSaveable { mutableIntStateOf(0) }
    Text("先预测：连续点三次，再旋转屏幕，数字会是多少？", style = MaterialTheme.typography.titleMedium)
    Text("已点击 $count 次", style = MaterialTheme.typography.headlineMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { count++ }) { Text("+1") }
        TextButton(onClick = { count = 0 }) { Text("清零") }
    }
    Text("观察：点击回调改变可观察状态；rememberSaveable 让这个整数在旋转重建后恢复。")
}

@Composable
private fun InputShowcase() {
    var name by rememberSaveable { mutableStateOf("") }
    Text("先预测：删掉输入内容后，下方文字会怎样？", style = MaterialTheme.typography.titleMedium)
    ShowcaseNameField(value = name, onValueChange = { name = it })
    Text(if (name.isBlank()) "请输入名字" else "你好，$name", style = MaterialTheme.typography.headlineSmall)
    Text("观察：父组件保存 name；输入框只接收 value 并报告 onValueChange。回到练习，试着自己拆出无状态 NameField。")
}

@Composable
private fun ShowcaseNameField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("名字") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
