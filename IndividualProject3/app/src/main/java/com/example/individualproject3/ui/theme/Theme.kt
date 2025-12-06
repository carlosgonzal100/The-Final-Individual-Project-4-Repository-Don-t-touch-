import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.individualproject3.ui.theme.Pink40
import com.example.individualproject3.ui.theme.Pink80
import com.example.individualproject3.ui.theme.PixelTypography
import com.example.individualproject3.ui.theme.Purple40
import com.example.individualproject3.ui.theme.Purple80
import com.example.individualproject3.ui.theme.PurpleGrey40
import com.example.individualproject3.ui.theme.PurpleGrey80

@Composable
fun IndividualProject3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80
    )

    val lightColorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PixelTypography,   // <-- apply Tiny5 here
        content = content
    )
}

val DungeonDarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8BC34A),
    secondary = androidx.compose.ui.graphics.Color(0xFFFFC107),
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    background = androidx.compose.ui.graphics.Color(0xFF0B0F12),
    surface = androidx.compose.ui.graphics.Color(0xFF11161A),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
)

@Composable
fun DungeonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DungeonDarkColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
