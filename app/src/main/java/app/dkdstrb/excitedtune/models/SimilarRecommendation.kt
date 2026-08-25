package app.dkdstrb.excitedtune.models

import app.dkdstrb.excitedtune.db.entities.LocalItem
import com.metrolist.innertube.models.YTItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
