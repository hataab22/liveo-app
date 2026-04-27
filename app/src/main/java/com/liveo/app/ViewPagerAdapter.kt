package com.liveo.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(
    fm: FragmentManager,
    private val allChannels: List<Channel>,
    private val prefsManager: PreferencesManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    
    private val tabs = listOf(
        "الكل",
        "بث مباشر",
        "أفلام",
        "مسلسلات",
        "موسيقى",
        "المفضلة",
        "الأخيرة"
    )
    
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AllChannelsFragment.newInstance(allChannels, prefsManager)
            1 -> {
                val filtered = allChannels.filter { !isMovie(it) && !isSeries(it) && !isMusic(it) }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "بث مباشر")
            }
            2 -> {
                val filtered = allChannels.filter { isMovie(it) }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "أفلام")
            }
            3 -> {
                val filtered = allChannels.filter { isSeries(it) }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "مسلسلات")
            }
            4 -> {
                val filtered = allChannels.filter { isMusic(it) }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "موسيقى")
            }
            5 -> FavoritesFragment.newInstance(prefsManager)
            6 -> RecentFragment.newInstance(prefsManager)
            else -> AllChannelsFragment.newInstance(allChannels, prefsManager)
        }
    }
    
    override fun getCount(): Int = tabs.size
    
    override fun getPageTitle(position: Int): CharSequence = tabs[position]
    
    private fun isMovie(channel: Channel): Boolean {
        val cat = channel.category.lowercase()
        return cat.contains("أفلام") || cat.contains("movies") || cat.contains("cinema") || cat.contains("film")
    }
    
    private fun isSeries(channel: Channel): Boolean {
        val cat = channel.category.lowercase()
        return cat.contains("مسلسل") || cat.contains("series") || cat.contains("show")
    }
    
    private fun isMusic(channel: Channel): Boolean {
        val cat = channel.category.lowercase()
        return cat.contains("موسيقى") || cat.contains("music") || cat.contains("أغاني")
    }
}