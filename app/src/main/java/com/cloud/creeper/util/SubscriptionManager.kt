package com.cloud.creeper.util

/**
 * 订阅管理器
 *
 * 使用 by lazy 实现线程安全的懒加载单例模式
 * 用于管理代理订阅相关操作
 *
 * Created by cloud on 2026/4/14.
 */
class SubscriptionManager private constructor() {

    companion object {
        /**
         * 单例实例
         * by lazy 默认使用同步机制保证线程安全
         */
        val instance: SubscriptionManager by lazy {
            SubscriptionManager()
        }
    }

    private const val TAG = "SubscriptionManager"

    /**
     * TODO: 添加订阅管理相关方法
     */
}
