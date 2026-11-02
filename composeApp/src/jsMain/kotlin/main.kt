import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.rshotsbilling.App
import com.example.rshotsbilling.utils.initializeFirebase
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeFirebase()
    ComposeViewport(document.body!!) {
        App()
    }
}
