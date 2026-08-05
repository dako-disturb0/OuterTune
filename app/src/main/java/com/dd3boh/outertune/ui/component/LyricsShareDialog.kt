package com.dd3boh.outertune.ui.component

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.dd3boh.outertune.R
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.ui.dialog.DefaultDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.akanework.gramophone.logic.utils.displayLyricsText
import java.io.File
import java.io.FileOutputStream

fun shareLyricsAsText(
    context: Context,
    title: String,
    artist: String,
    lyricsText: String,
    songId: String? = null,
) {
    val songLink = songId?.takeIf { it.isNotBlank() && !it.startsWith("local:") }
        ?.let { "https://music.youtube.com/watch?v=$it" }
    
    val cleanLyrics = displayLyricsText(lyricsText)
    val shareBody = buildString {
        append("\"")
        append(cleanLyrics)
        append("\"\n\n")
        append(title)
        append(" - ")
        append(artist)
        if (songLink != null) {
            append("\n")
            append(songLink)
        }
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(
        Intent.createChooser(shareIntent, context.getString(R.string.share))
    )
}

suspend fun generateLyricsImage(
    context: Context,
    title: String,
    artist: String,
    lyricsText: String,
    coverArtUrl: String?,
): Bitmap = withContext(Dispatchers.IO) {
    val width = 1080
    val height = 1080
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Load artwork if available
    var artBitmap: Bitmap? = null
    if (coverArtUrl != null) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(coverArtUrl)
                .allowHardware(false)
                .build()
            artBitmap = loader.execute(request).image?.toBitmap()
        } catch (_: Exception) {}
    }

    // Draw background
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    if (artBitmap != null) {
        val scaledArt = Bitmap.createScaledBitmap(artBitmap, width, height, true)
        canvas.drawBitmap(scaledArt, 0f, 0f, bgPaint)
        // Dark overlay
        val overlayPaint = Paint().apply {
            color = AndroidColor.parseColor("#CC121212")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
    } else {
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            AndroidColor.parseColor("#1E1E2E"), AndroidColor.parseColor("#0F0F1A"),
            Shader.TileMode.CLAMP
        )
        bgPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    }

    // Card background
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#33FFFFFF")
    }
    val cardRect = RectF(60f, 60f, (width - 60).toFloat(), (height - 60).toFloat())
    canvas.drawRoundRect(cardRect, 40f, 40f, cardPaint)

    // Title
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textSize = 48f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(title.take(35), 100f, 150f, titlePaint)

    // Artist
    val artistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#B3FFFFFF")
        textSize = 34f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    canvas.drawText(artist.take(40), 100f, 205f, artistPaint)

    // Divider line
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#44FFFFFF")
        strokeWidth = 3f
    }
    canvas.drawLine(100f, 240f, (width - 100).toFloat(), 240f, linePaint)

    // Lyrics Text
    val cleanLyrics = displayLyricsText(lyricsText)
    val lyricLines = cleanLyrics.lines().take(8)
    val lyricsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textSize = 38f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    var startY = 320f
    for (line in lyricLines) {
        val trimmed = line.trim()
        if (trimmed.isNotEmpty()) {
            canvas.drawText(trimmed.take(45), 100f, startY, lyricsPaint)
            startY += 65f
        }
    }

    // App Branding Footer
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#80FFFFFF")
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText("OuterTune", 100f, (height - 100).toFloat(), footerPaint)

    bitmap
}

fun shareLyricsImage(
    context: Context,
    bitmap: Bitmap,
) {
    val cacheDir = File(context.cacheDir, "shared_lyrics")
    cacheDir.mkdirs()
    val imageFile = File(cacheDir, "lyrics_${System.currentTimeMillis()}.png")

    FileOutputStream(imageFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val contentUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.FileProvider",
        imageFile
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(shareIntent, context.getString(R.string.share))
    )
}

@Composable
fun LyricsShareDialog(
    mediaMetadata: MediaMetadata,
    rawLyricsText: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cleanLyrics = remember(rawLyricsText) { displayLyricsText(rawLyricsText) }

    DefaultDialog(
        onDismiss = onDismiss,
        icon = { Icon(imageVector = Icons.Rounded.Share, contentDescription = null) },
        title = { Text(text = stringResource(R.string.share)) },
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (mediaMetadata.thumbnailUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(mediaMetadata.thumbnailUrl)
                                    .build()
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = mediaMetadata.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = mediaMetadata.artists.joinToString { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = cleanLyrics.lines().take(5).joinToString("\n"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = "OuterTune",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        shareLyricsAsText(
                            context = context,
                            title = mediaMetadata.title,
                            artist = mediaMetadata.artists.joinToString { it.name },
                            lyricsText = rawLyricsText,
                            songId = mediaMetadata.id
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Teks")
                }

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val bitmap = generateLyricsImage(
                                    context = context,
                                    title = mediaMetadata.title,
                                    artist = mediaMetadata.artists.joinToString { it.name },
                                    lyricsText = rawLyricsText,
                                    coverArtUrl = mediaMetadata.thumbnailUrl
                                )
                                onDismiss()
                                shareLyricsImage(context, bitmap)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membuat gambar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Gambar Card")
                }
            }
        }
    }
}
