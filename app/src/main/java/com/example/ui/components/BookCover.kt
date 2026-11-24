package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.PlayfairDisplayFontFamily

data class CoverPreset(
    val key: String,
    val label: String
)

val COVER_PRESETS = listOf(
    CoverPreset("res:starlight", "Starlight Portal"),
    CoverPreset("res:neon_rain", "Neon Rain"),
    CoverPreset("res:autumn_cafe", "Autumn Cafe"),
    CoverPreset("res:clockwork", "Brass & Key"),
    CoverPreset("gradient:amber", "Golden Dusk"),
    CoverPreset("gradient:ocean", "Deep Harbor"),
    CoverPreset("gradient:emerald", "Verdant Mist"),
    CoverPreset("gradient:plum", "Velvet Night")
)

@Composable
fun BookCover(
    coverKey: String,
    title: String,
    authorName: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: Dp = 4.dp,
    showTypographyOverlayOnGradient: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = shape,
        shadowElevation = elevation,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(modifier = Modifier.fillMaxSize().clip(shape)) {
            when {
                coverKey.startsWith("file://") || coverKey.startsWith("content://") -> {
                    AsyncImage(
                        model = coverKey,
                        contentDescription = "Book cover for $title",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                coverKey == "res:starlight" -> {
                    Image(
                        painter = painterResource(id = R.drawable.img_cover_starlight),
                        contentDescription = "Book cover for $title",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                coverKey == "res:neon_rain" -> {
                    Image(
                        painter = painterResource(id = R.drawable.img_cover_neon_rain),
                        contentDescription = "Book cover for $title",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                coverKey == "res:autumn_cafe" -> {
                    Image(
                        painter = painterResource(id = R.drawable.img_cover_autumn_cafe),
                        contentDescription = "Book cover for $title",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                coverKey == "res:clockwork" -> {
                    Image(
                        painter = painterResource(id = R.drawable.img_cover_clockwork),
                        contentDescription = "Book cover for $title",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    GradientBookCover(
                        coverKey = coverKey,
                        title = title,
                        authorName = authorName,
                        showTypography = showTypographyOverlayOnGradient
                    )
                }
            }

            // Subtle book spine crease on the left edge for tactile print realism
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0.0f to Color.Black.copy(alpha = 0.24f),
                            0.04f to Color.White.copy(alpha = 0.14f),
                            0.08f to Color.Transparent
                        )
                    )
            )
        }
    }
}

@Composable
private fun GradientBookCover(
    coverKey: String,
    title: String,
    authorName: String,
    showTypography: Boolean
) {
    val palette = when (coverKey) {
        "gradient:amber" -> listOf(Color(0xFF9C3D12), Color(0xFFE58A2B), Color(0xFF3E1908))
        "gradient:ocean" -> listOf(Color(0xFF0E293B), Color(0xFF216278), Color(0xFF0A1622))
        "gradient:emerald" -> listOf(Color(0xFF10382D), Color(0xFF287359), Color(0xFF0A1F19))
        else -> listOf(Color(0xFF2E1437), Color(0xFF6B2D5C), Color(0xFF1A0B21))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(palette))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.09f),
                radius = size.minDimension * 0.48f,
                center = Offset(size.width * 0.85f, size.height * 0.18f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = size.minDimension * 0.35f,
                center = Offset(size.width * 0.15f, size.height * 0.82f)
            )
        }

        if (showTypography) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontFamily = PlayfairDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    color = Color.White,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = authorName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
