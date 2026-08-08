package com.dd3boh.outertune.aicontentfilter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveAiContentFilterUseCase @Inject constructor(
    private val repository: AiContentFilterRepository,
) {
    operator fun invoke(): Flow<Pair<AiContentFilterSettings, AiContentFilterStatus>> =
        combine(repository.observeSettings(), repository.observeStatus(), ::Pair)
}

class UpdateAiContentFilterSettingsUseCase @Inject constructor(
    private val repository: AiContentFilterRepository,
) {
    suspend fun setEnabled(enabled: Boolean) { repository.setEnabled(enabled) }
    suspend fun setIncludeModerateConfidence(enabled: Boolean) { repository.setIncludeModerateConfidence(enabled) }
}

class RefreshAiContentFilterUseCase @Inject constructor(
    private val repository: AiContentFilterRepository,
) {
    suspend operator fun invoke(force: Boolean): AiContentFilterRefreshResult =
        repository.refreshIfStale(force)
}

class LoadAiContentFilterPolicyUseCase @Inject constructor(
    private val repository: AiContentFilterRepository,
) {
    suspend operator fun invoke(): AiContentFilterPolicy {
        val settings = repository.getSettings()
        if (!settings.enabled) return AiContentFilterPolicy.Disabled
        repository.refreshIfStale()
        val lists = repository.loadLists()
        val blockedKeys = if (settings.includeModerateConfidence) {
            lists.blocklist + lists.warnlist
        } else {
            lists.blocklist
        }
        return AiContentFilterPolicy(enabled = true, blockedChannelKeys = blockedKeys)
    }
}
