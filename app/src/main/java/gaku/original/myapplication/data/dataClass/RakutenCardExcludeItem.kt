package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.HasId

/**
 * A charge whose store name matches this isn't saved as an expense by the Rakuten Card (all charges) extraction.
 */
data class RakutenCardExcludeItem(
    override var id: String? = null,
    val name: String = "",
    val condition: MatchCondition = MatchCondition.EXACT
) : HasId
