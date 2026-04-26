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
            0 -> {
                // الكل (الفلترة داخل Fragment)
                AllChannelsFragment.newInstance(allChannels, prefsManager)
            }
            1 -> {
                // بث مباشر (بدون فلترة +18، الفلترة داخل Fragment)
                val filtered = allChannels.filter { channel ->
                    !isMovie(channel) &&
                    !isSeries(channel) &&
                    !isMusic(channel)
                }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "بث مباشر")
            }
            2 -> {
                // أفلام (نمرر كل الأفلام بما فيها +18)
                val filtered = allChannels.filter { isMovie(it) }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "أفلام")
            }
            3 -> {
                // مسلسلات (نمرر كل المسلسلات بما فيها +18)
                val filtered = allChannels.filter { isSeries(it) }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "مسلسلات")
            }
            4 -> {
                // موسيقى
                val filtered = allChannels.filter { isMusic(it) }
                CategoryWithSubsFragment.newInstance(filtered, prefsManager, "موسيقى")
            }
            5 -> {
                // المفضلة
                FavoritesFragment.newInstance(allChannels, prefsManager)
            }
            6 -> {
                // الأخيرة
                RecentFragment.newInstance(allChannels, prefsManager)
            }
            else -> AllChannelsFragment.newInstance(allChannels, prefsManager)
        }
    }
    
    override fun getCount(): Int = tabs.size
    
    override fun getPageTitle(position: Int): CharSequence = tabs[position]
    
    // ✅ دوال التصنيف
    private fun isAdultContent(channel: Channel): Boolean {
        val category = channel.category.lowercase()
        val name = channel.name.lowercase()
        return category.contains("+18") ||
               category.contains("adult") ||
               category.contains("للكبار") ||
               name.contains("+18")
    }
    
    private fun isMovie(channel: Channel): Boolean {
        val category = channel.category.lowercase()
        return category.contains("أفلام") ||
               category.contains("movies") ||
               category.contains("cinema") ||
               category.contains("film")
    }
    
    private fun isSeries(channel: Channel): Boolean {
        val category = channel.category.lowercase()
        return category.contains("مسلسل") ||
               category.contains("series") ||
               category.contains("show")
    }
    
    private fun isMusic(channel: Channel): Boolean {
        val category = channel.category.lowercase()
        return category.contains("موسيقى") ||
               category.contains("music") ||
               category.contains("أغاني")
    }
}
