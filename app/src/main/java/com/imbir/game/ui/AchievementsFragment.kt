package com.imbir.game.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imbir.game.R
import com.imbir.game.data.Achievement
import com.imbir.game.data.AchievementTier

class AchievementsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_achievements, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gm = (requireActivity() as MainActivity).gameManager

        val rv = view.findViewById<RecyclerView>(R.id.achievementsRecycler)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = AchievementAdapter(gm.achievements)

        val unlocked = gm.achievements.count { it.unlocked }
        view.findViewById<TextView>(R.id.achievementsCountText).text =
            "$unlocked / ${gm.achievements.size} unlocked"
    }
}

class AchievementAdapter(private val items: List<Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val emoji:  TextView = v.findViewById(R.id.achEmoji)
        val name:   TextView = v.findViewById(R.id.achName)
        val desc:   TextView = v.findViewById(R.id.achDesc)
        val prog:   TextView = v.findViewById(R.id.achProgress)
        val card:   View     = v.findViewById(R.id.achCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ach = items[position]
        holder.emoji.text = if (ach.unlocked) ach.iconEmoji else "🔒"
        holder.name.text  = ach.name
        holder.desc.text  = ach.description
        holder.prog.text  = if (ach.goal > 1) "${ach.progress}/${ach.goal}" else ""

        val tierColor = when (ach.tier) {
            AchievementTier.BASIC        -> 0xFFE8F5E9.toInt()
            AchievementTier.INTERMEDIATE -> 0xFFFFF3E0.toInt()
            AchievementTier.ADVANCED     -> 0xFFFCE4EC.toInt()
        }
        val alpha = if (ach.unlocked) 1f else 0.5f
        holder.card.setBackgroundColor(tierColor)
        holder.card.alpha = alpha
    }
}