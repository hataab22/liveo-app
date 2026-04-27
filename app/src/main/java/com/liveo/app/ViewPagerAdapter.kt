package com.liveo.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(
    fm: FragmentManager,
    private val allChannels: List<Channel>,
    private val prefsManager: PreferencesManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    
    private val categories = listOf("الكل", "قنوات مباشرة", "أفلام", "مسلسلات", "موسيقى", "المفضلة", "الأخيرة")
    
    override fun getItem(position: Int): Fragment {
        // فلتر القنوات حسب الرقابة الأبوية
        val filteredChannels = if (prefsManager.isParentalUnlocked() || !prefsManager.hasAdultAccess()) {
            allChannels
        } else {
            allChannels.filter { !it.isAdult }
        }
        
        return when (position) {
            0 -> AllChannelsFragment.newInstance(filteredChannels)
            1 -> CategoryWithSubsFragment.newInstance(filteredChannels, "قنوات")
            2 -> CategoryWithSubsFragment.newInstance(filteredChannels, "أفلام")
            3 -> CategoryWithSubsFragment.newInstance(filteredChannels, "مسلسلات")
            4 -> CategoryWithSubsFragment.newInstance(filteredChannels, "موسيقى")
            5 -> FavoritesFragment.newInstance()
            6 -> RecentFragment.newInstance()
            else -> AllChannelsFragment.newInstance(filteredChannels)
        }
    }
    
    override fun getCount(): Int = categories.size
    
    override fun getPageTitle(position: Int): CharSequence = categories[position]
}
