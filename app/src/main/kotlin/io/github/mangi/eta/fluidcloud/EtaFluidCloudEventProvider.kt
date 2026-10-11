package io.github.mangi.eta.fluidcloud

import com.oplus.pantanal.seedling.SeedlingCardEventProvider
import com.oplus.pantanal.seedling.bean.CardCreateErrorBean
import com.oplus.pantanal.seedling.util.Logger

/**
 * 流体云卡片事件 Provider。
 *
 * 承接来自 ColorOS 泛在服务的错误与关闭事件：
 * - [onCardCreateErrorInfo]：卡片创建失败（模板缺失、平台版本不匹配、包名校验失败等）
 * - [onServiceClose]：泛在服务主动关闭了本 App 的卡片通道
 *
 * 只做日志与告警，不影响 Eta 主流程；失败时由调用方降级到普通 Notification。
 */
class EtaFluidCloudEventProvider : SeedlingCardEventProvider() {

    private val tag = "EtaFluidCloudEvent"

    override fun onCardCreateErrorInfo(cardErrorInfo: CardCreateErrorBean) {
        Logger.e(tag, "onCardCreateErrorInfo entranceType=${cardErrorInfo.entranceType} " +
            "entranceName=${cardErrorInfo.entranceName} errorCode=${cardErrorInfo.errorCode} " +
            "errorMsg=${cardErrorInfo.errorMsg}")
    }

    override fun onServiceClose(serviceId: String, reason: String, serviceInstanceId: String, extra: String) {
        Logger.w(tag, "onServiceClose serviceId=$serviceId reason=$reason instanceId=$serviceInstanceId extra=$extra")
    }
}
