/**
 * 
 */

package phonon.puppet.resourcepack

import java.io.FileReader
import java.nio.file.Path
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

internal fun loadCustomModelDataFromJson(path: Path): List<String> {
    return FileReader(path.toFile()).use { reader -> 
        val json = JsonParser().parse(reader)
        val jsonObj = json.getAsJsonObject()
        
        val jsonDataArray = jsonObj.get("models")?.getAsJsonArray()
        if ( jsonDataArray === null ) {
            return listOf()
        }

        val models: ArrayList<String> = arrayListOf()
        for ( m in jsonDataArray ) {
            models.add(m.getAsString())
        }

        models.toList()
    }
}