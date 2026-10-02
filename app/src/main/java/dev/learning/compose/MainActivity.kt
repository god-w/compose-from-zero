package dev.learning.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val lessons = listOf(
    "01 第一个 Composable" to "从 XML/View 过渡到声明式 UI",
    "02 状态与重组" to "点击计数器，理解 remember",
    "03 输入与单向数据流" to "文本输入、状态提升",
    "04 布局与 Modifier" to "Column、Row、间距与顺序",
    "05 列表与 key" to "LazyColumn 中增删项目",
    "06 副作用" to "LaunchedEffect 与协程生命周期",
    "07 阶段项目" to "把所学串成一个任务清单",
    "08 状态持有者与架构" to "ViewModel、Flow 与界面状态",
    "09 导航与返回栈" to "列表、详情与任务 ID",
    "10 UI 测试" to "用行为测试防止回归",
    "11 质量与无障碍" to "语义、预览与大字号",
    "12 View 互操作" to "渐进迁移旧项目",
    "13 动画" to "状态驱动的过渡与动效",
    "14 手势" to "点击、拖动与交互反馈",
    "15 绘制与自定义布局" to "Canvas、约束与测量",
    "16 状态恢复与持久化" to "旋转、进程死亡与离线数据",
    "17 异步数据" to "加载、错误、重试与取消",
    "18 自适应布局" to "手机、平板与横屏",
    "19 性能实战" to "测量后优化重组与滚动",
    "20 综合毕业项目" to "独立交付并解释设计取舍"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Workshop() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Workshop() {
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    var showingShowcase by rememberSaveable { mutableStateOf(false) }
    BackHandler(selected >= 0) {
        if (showingShowcase) showingShowcase = false else selected = -1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showingShowcase) "${"%02d".format(selected + 1)} · 可操作示范" else if (selected < 0) "Compose 从零开始" else lessons[selected].first) },
                navigationIcon = {
                    if (selected >= 0) TextButton(onClick = {
                        if (showingShowcase) showingShowcase = false else selected = -1
                    }) { Text("返回") }
                }
            )
        }
    ) { padding ->
        if (selected < 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("第 1 课先读讲义理解调用链，再修改 Exercises.kt。第 2–7 课可先体验示范；进阶课围绕阶段项目扩展。", style = MaterialTheme.typography.bodyLarge)
                }
                itemsIndexed(lessons) { index, lesson ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        selected = index
                        showingShowcase = false
                    }) {
                        Column(Modifier.padding(20.dp)) {
                            Text(lesson.first, style = MaterialTheme.typography.titleMedium)
                            Text(lesson.second, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else if (showingShowcase) {
            if (selected == 6) TaskShowcase(Modifier.fillMaxSize().padding(padding))
            else if (selected in 0..2) FoundationShowcase(selected, Modifier.fillMaxSize().padding(padding))
            else CoreShowcase(selected, Modifier.fillMaxSize().padding(padding))
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                Text("阅读 lessons/${"%02d".format(selected + 1)}-*.md，按任务与验收步骤动手", style = MaterialTheme.typography.titleMedium)
                if (selected in 0..6) {
                    TextButton(onClick = { showingShowcase = true }) {
                        Text(if (selected == 0) "完成静态练习后，体验名字切换示范 →" else "先体验可操作示范，再回来自己写 →")
                    }
                }
                LessonVisual(selected)
                Box(Modifier.weight(1f)) { PracticeScreen(selected) }
            }
        }
    }
}
