package bangla.English.bekaron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import bangla.English.bekaron.engine.OfflineGrammarEngine
import bangla.English.bekaron.ui.BekaronApp

class MainActivity : ComponentActivity() {

    private lateinit var grammarEngine: OfflineGrammarEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        grammarEngine = OfflineGrammarEngine(applicationContext)

        setContent {
            MaterialTheme {
                Surface {
                    BekaronApp(engine = grammarEngine)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        grammarEngine.close()
    }
}
