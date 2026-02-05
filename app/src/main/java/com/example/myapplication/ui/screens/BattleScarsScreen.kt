package com.example.myapplication.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.BattleScar
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.viewmodel.GymViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.*

@Composable
fun BattleScarsScreen(vm: GymViewModel) {
    val scars by vm.allBattleScars.collectAsState(initial = emptyList())
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val localUri = saveImageLocally(context, it)
            localUri?.let { path ->
                vm.addBattleScar(path, "Mais um dia de glória.")
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Cicatrizes", subtitle = "Mural de Honra")
            Button(
                onClick = { launcher.launch("image/*") },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, null)
                Spacer(Modifier.width(8.dp))
                Text("REGISTRAR")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (scars.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Sem marcas de batalha ainda.\nOnde está seu esforço?",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(scars) { scar ->
                    MangaScarCard(scar) { 
                        // Optional: delete local file when deleting record
                        val file = File(Uri.parse(scar.imageUri).path ?: "")
                        if (file.exists()) file.delete()
                        vm.deleteBattleScar(scar) 
                    }
                }
            }
        }
    }
}

fun saveImageLocally(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "scar_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun MangaScarCard(scar: BattleScar, onDelete: () -> Unit) {
    val mangaMatrix = remember {
        val contrast = 1.5f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val array = floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        ColorMatrix(array)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .border(2.dp, Color.White, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = scar.imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.colorMatrix(mangaMatrix)
            )
            
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                Text(
                    scar.date.takeLast(5),
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(Color.Black.copy(0.5f))
            ) {
                Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            
            Text(
                "ゼンカイ",
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                color = Color.White.copy(0.7f),
                fontWeight = FontWeight.Light,
                fontSize = 10.sp
            )
        }
    }
}
