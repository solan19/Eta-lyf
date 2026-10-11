package io.github.mangi.eta.fluidcloud

import android.content.Context
import com.oplus.pantanal.seedling.bean.SeedlingCard
import com.oplus.pantanal.seedling.update.SeedlingCardOptions
import com.oplus.pantanal.seedling.util.SeedlingTool
import org.json.JSONObject

/**
 * ColorOS 流体云通知管理器。
 *
 * 在 ColorOS 14+ 设备上，将 Eta 的运行态通知以流体云胶囊/面板形式呈现；
 * 不支持的设备自动静默回退到普通通知（由调用方负责发送 fallback）。
 *
 * 基于 SeedlingSupportSDK v3.0.7 (lite) 实际 API。
 * SeedlingCardOptions 使用无参构造 + setter，避免参数名不匹配。
 */
object FluidCloudNotifier {

    /** 同步快速判断设备是否支持泛在卡片。 */
    val isSupported: Boolean by lazy {
        runCatching { SeedlingTool.isSupportSeedlingCard(appContext) }
            .getOrDefault(false)
    }

    private lateinit var appContext: Context

    /** 必须在 Application.onCreate 中调用，注入 ApplicationContext。 */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ── 智能体执行 ──

    fun showExecution(title: String, tool: String, stepDescription: String? = null, progress: Int? = null) {
        val data = JSONObject().apply {
            put("icon", "ic_notification")
            put("title", title)
            put("tool", tool)
            stepDescription?.let { put("stepDescription", it) }
            progress?.let { put("progress", it) }
            put("stopLabel", "停止")
        }
        pushCard("eta_execution", data)
    }

    // ── 虚拟屏运行 ──

    fun showVirtualScreen(title: String, tool: String) {
        val data = JSONObject().apply {
            put("icon", "ic_notification")
            put("title", title)
            put("tool", tool)
        }
        pushCard("eta_virtual_screen", data)
    }

    // ── 运行进度 ──

    fun showProgress(title: String, percent: Int, currentStep: String? = null) {
        val data = JSONObject().apply {
            put("icon", "ic_notification")
            put("title", title)
            put("progress", percent)
            put("percent", "${percent}%")
            currentStep?.let { put("currentStep", it) }
        }
        pushCard("eta_run_progress", data)
    }

    // ── 自动化任务运行中 ──

    fun showAutomationJob(taskTitle: String, triggerSource: String) {
        val data = JSONObject().apply {
            put("icon", "ic_notification")
            put("title", taskTitle)
            put("tool", triggerSource)
            put("stopLabel", "停止")
        }
        pushCard("eta_automation_jobs", data)
    }

    // ── 自动化任务失败 ──

    fun showAutomationFailure(title: String, errorMessage: String) {
        val data = JSONObject().apply {
            put("icon", "ic_notification")
            put("title", title)
            put("error", errorMessage)
            put("retryLabel", "重试")
        }
        pushCard("eta_automation_failures", data, requestShowPanel = true)
    }

    // ── 任务完成摘要 ──

    fun showCompletion(title: String, summary: String, details: String? = null) {
        val data = JSONObject().apply {
            put("icon", "ic_notification")
            put("title", title)
            put("summary", summary)
            details?.let { put("details", it) }
        }
        pushCard("eta_task_completion", data, requestShowPanel = true)
    }

    // ── 需要用户同意主屏操作 ──

    /**
     * 当虚拟屏不兼容时，请求用户授权使用主屏。
     * 流体云胶囊带"允许"/"取消"按钮，用户在状态栏直接操作。
     */
    fun showUiFallbackRequest(reason: String) {
        val data = JSONObject().apply {
            put("icon", "ic_notification")
            put("title", "Eta 请求使用主屏")
            put("reason", reason)
            put("allowLabel", "允许")
            put("cancelLabel", "取消")
        }
        pushCard("eta_ui_fallback", data, requestShowPanel = true)
    }

    // ── 内部方法 ──

    private fun pushCard(widgetCode: String, businessData: JSONObject, requestShowPanel: Boolean? = null) {
        if (!isSupported) return
        runCatching {
            val card = SeedlingCard.build(widgetCode)
            // 使用无参构造 + setter，避免 Kotlin 编译时参数名不匹配
            val options = SeedlingCardOptions().apply {
                if (requestShowPanel != null) this.requestShowPanel = requestShowPanel
            }
            SeedlingTool.INSTANCE.updateData(card, businessData, options)
        }
    }
}
