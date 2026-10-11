package io.github.mangi.eta.fluidcloud

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.oplus.pantanal.seedling.bean.SeedlingCard
import com.oplus.pantanal.seedling.update.SeedlingCardOptions
import com.oplus.pantanal.seedling.util.Logger
import com.oplus.pantanal.seedling.SeedlingCardWidgetProvider

/**
 * 流体云卡片 WidgetProvider。
 *
 * 继承 SDK 的 [SeedlingCardWidgetProvider]，作为 Eta 与 ColorOS 泛在服务框架之间的桥梁。
 * 该类是抽象基类在 AAR 里的实现壳，本文件提供具名子类以满足 AndroidManifest 注册需求。
 *
 * 实际的数据推送由 [FluidCloudNotifier] 通过 `SeedlingTool.INSTANCE.updateData(...)` 完成，
 * 本类主要承接生命周期回调，用于调试或未来扩展（例如：卡片订阅/取消订阅持久化）。
 */
class EtaFluidCloudProvider : SeedlingCardWidgetProvider() {

    private val tag = "EtaFluidCloudProvider"

    override fun onCardCreate(context: android.content.Context, card: SeedlingCard) {
        Logger.i(tag, "onCardCreate card=${card.serviceId}")
    }

    override fun onShow(context: android.content.Context, card: SeedlingCard) {
        Logger.i(tag, "onShow card=${card.serviceId}")
    }

    override fun onHide(context: android.content.Context, card: SeedlingCard) {
        Logger.i(tag, "onHide card=${card.serviceId}")
    }

    override fun onDestroy(context: android.content.Context, card: SeedlingCard) {
        Logger.i(tag, "onDestroy card=${card.serviceId}")
    }

    override fun onUpdateData(context: android.content.Context, card: SeedlingCard, data: Bundle) {
        Logger.i(tag, "onUpdateData card=${card.serviceId}")
    }

    override fun onSubscribed(context: android.content.Context, card: SeedlingCard) {
        Logger.i(tag, "onSubscribed card=${card.serviceId}")
    }

    override fun onUnSubscribed(context: android.content.Context, card: SeedlingCard) {
        Logger.i(tag, "onUnSubscribed card=${card.serviceId}")
    }

    override fun onCardObserve(context: android.content.Context, clientName: String, cards: List<SeedlingCard>) {
        Logger.i(tag, "onCardObserve client=$clientName count=${cards.size}")
    }
}
