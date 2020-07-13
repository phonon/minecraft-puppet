/**
 * String filter utils
 */

package phonon.puppet.utils

import org.bukkit.Bukkit

// matches any string in a list of strings that 
// begins with start
public fun filterByStart(list: List<String>, start: String): List<String> {
    val startLowerCase = start.toLowerCase()
    return list.filter { s -> s.toLowerCase().startsWith(startLowerCase) }
}

// match player name from online players
public fun filterPlayer(start: String): List<String> {
    val startLowerCase = start.toLowerCase()
    val players = Bukkit.getOnlinePlayers()
    val filtered = players
        .asSequence()
        .map{ p -> p.name }
        .filter{ s -> s.toLowerCase().startsWith(startLowerCase) }
        .toList()
    
    return filtered
}
