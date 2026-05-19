package com.imbir.game.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imbir.game.R
import com.imbir.game.data.RoomCatalog
import com.imbir.game.data.RoomItem
import com.imbir.game.data.RoomItemType
import com.imbir.game.data.RoomState
import com.imbir.game.logic.GameManager

class RoomFragment : Fragment() {

    private lateinit var roomPreview: LinearLayout
    private lateinit var roomBonusText: TextView
    private lateinit var itemsRecycler: RecyclerView
    private lateinit var filterAll: TextView
    private lateinit var filterWall: TextView
    private lateinit var filterFurn: TextView
    private lateinit var filterToy: TextView
    private lateinit var filterDeco: TextView
    private var currentFilter: RoomItemType? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_room, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gm = (requireActivity() as MainActivity).gameManager

        roomPreview   = view.findViewById(R.id.roomPreview)
        roomBonusText = view.findViewById(R.id.roomBonusText)
        itemsRecycler = view.findViewById(R.id.roomItemsRecycler)
        filterAll     = view.findViewById(R.id.filterAll)
        filterWall    = view.findViewById(R.id.filterWallpaper)
        filterFurn    = view.findViewById(R.id.filterFurniture)
        filterToy     = view.findViewById(R.id.filterToy)
        filterDeco    = view.findViewById(R.id.filterDecoration)

        currentFilter = null

        itemsRecycler.layoutManager = LinearLayoutManager(requireContext())

        fun applyFilter(type: RoomItemType?) {
            currentFilter = type
            val catLevel = gm.state.getLevel()
            val items = RoomCatalog.items.filter { type == null || it.type == type }
            itemsRecycler.adapter = RoomItemAdapter(
                items = items,
                roomState = gm.roomState,
                catLevel = catLevel,
                onPlace = { item ->
                    gm.placeRoomItem(item.id)
                    updatePreview(gm)
                    itemsRecycler.adapter?.notifyDataSetChanged()
                },
                onRemove = { item ->
                    gm.removeRoomItem(item.id)
                    updatePreview(gm)
                    itemsRecycler.adapter?.notifyDataSetChanged()
                }
            )
        }

        filterAll.setOnClickListener  { applyFilter(null) }
        filterWall.setOnClickListener { applyFilter(RoomItemType.WALLPAPER) }
        filterFurn.setOnClickListener { applyFilter(RoomItemType.FURNITURE) }
        filterToy.setOnClickListener  { applyFilter(RoomItemType.TOY) }
        filterDeco.setOnClickListener { applyFilter(RoomItemType.DECORATION) }

        applyFilter(null)
        updatePreview(gm)
    }

    private fun updatePreview(gm: GameManager) {
        val bgColor = RoomCatalog.wallpaperColors[gm.roomState.activeWallpaper] ?: 0xFFFDF5E6.toInt()
        roomPreview.setBackgroundColor(bgColor)

        val placed = gm.roomState.placedItems
        val emojiRow = roomPreview.findViewById<TextView>(R.id.roomEmojiRow)
        emojiRow?.text = placed.mapNotNull { id ->
            RoomCatalog.items.find { it.id == id }?.emoji
        }.joinToString(" ").ifEmpty { "🐱" }

        val mood = gm.roomState.getMoodBonus()
        val energy = gm.roomState.getEnergyBonus()
        roomBonusText.text = "Room bonuses: +$mood 😺 mood  +$energy ⚡ energy recovery"
    }
}

class RoomItemAdapter(
    private val items: List<RoomItem>,
    private val roomState: RoomState,
    private val catLevel: Int,
    private val onPlace: (RoomItem) -> Unit,
    private val onRemove: (RoomItem) -> Unit
) : RecyclerView.Adapter<RoomItemAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val emoji:  TextView = v.findViewById(R.id.roomItemEmoji)
        val name:   TextView = v.findViewById(R.id.roomItemName)
        val desc:   TextView = v.findViewById(R.id.roomItemDesc)
        val status: TextView = v.findViewById(R.id.roomItemStatus)
        val action: TextView = v.findViewById(R.id.roomItemAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val unlocked  = item.id in roomState.unlockedItems
        val isActive  = item.id == roomState.activeWallpaper
        val isPlaced  = item.id in roomState.placedItems

        holder.emoji.text = item.emoji
        holder.name.text  = item.name
        holder.desc.text  = item.description +
            if (item.moodBonus > 0 || item.energyBonus > 0)
                " (+${item.moodBonus} mood, +${item.energyBonus} energy)"
            else ""

        when {
            !unlocked -> {
                holder.status.text = "🔒 Unlock at level ${item.unlockLevel}"
                holder.action.text = ""
                holder.action.isEnabled = false
                holder.itemView.alpha = 0.5f
            }
            isActive || isPlaced -> {
                holder.status.text = if (item.type == RoomItemType.WALLPAPER) "✅ Active" else "✅ Placed"
                holder.action.text = if (item.type == RoomItemType.WALLPAPER) "" else "Remove"
                holder.action.isEnabled = item.type != RoomItemType.WALLPAPER
                holder.itemView.alpha = 1f
                holder.action.setOnClickListener { onRemove(item) }
            }
            else -> {
                holder.status.text = "Available"
                holder.action.text = if (item.type == RoomItemType.WALLPAPER) "Set Wallpaper" else "Place"
                holder.action.isEnabled = true
                holder.itemView.alpha = 1f
                holder.action.setOnClickListener { onPlace(item) }
            }
        }
    }
}