import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.snapcompany.snapsafe.utilities.AESEncryption
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoView() {
    val aes = remember { AESEncryption() }
    val fixedSecretKey = remember { "0123456789012345".toByteArray() }

    var plainText by remember { mutableStateOf("") }
    var encryptedTextResult by remember { mutableStateOf("") }
    var decryptedTextResult by remember { mutableStateOf("") }
    var fullQrContent by remember { mutableStateOf("") }  // Contenido completo del QR
    var lastGeneratedIv by remember { mutableStateOf<ByteArray?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laboratorio Criptográfico + QR", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- BLOQUE 1: ENTRADA DE TEXTO ---
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. TEXTO PLANO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                CryptoTextField(
                    value = plainText,
                    onValueChange = { plainText = it },
                    placeholder = "Escribe el mensaje a cifrar aquí..."
                )
            }

            // Botón para CIFRAR y generar QR (formato completo)
            Button(
                onClick = {
                    if (plainText.isNotBlank()) {
                        val iv = aes.generateIv()
                        lastGeneratedIv = iv
                        val encrypted = aes.encryptStringWithSharedKey(plainText, fixedSecretKey, iv)

                        if (encrypted != null) {
                            encryptedTextResult = encrypted
                            // 🔑 FORMATO COMPATIBLE CON ControlModel.decryptQrData()
                            // Usamos espacio como separador (como espera qrCodeToGateData)
                            val ivBase64 = aes.byteArrayToString(iv)
                            fullQrContent = "$encrypted $ivBase64"  // ← Espacio como separador
                        } else {
                            encryptedTextResult = "Error al cifrar"
                            fullQrContent = ""
                        }
                        decryptedTextResult = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Cifrar y Generar QR", fontWeight = FontWeight.Bold)
            }

            // --- BLOQUE 2: QR Y RESULTADO CIFRADO ---
            if (fullQrContent.isNotEmpty() && encryptedTextResult != "Error al cifrar") {

                // Mostrar Código QR con el contenido completo
                Text(
                    text = "CÓDIGO QR (CONTENIDO: CIFRADO + IV)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrCodeView(
                        content = fullQrContent,  // ← QR con formato completo
                        modifier = Modifier.fillMaxSize(),
                        qrColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Tarjeta informativa
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "📦 CONTENIDO COMPLETO DEL QR:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = fullQrContent,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )

                        Divider()

                        Text(
                            text = "🔐 PARTE 1 (Cifrado Base64):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = fullQrContent.substringBefore(" ").take(80) + "...",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )

                        Text(
                            text = "🔑 PARTE 2 (IV Base64):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = fullQrContent.substringAfter(" "),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                // Botón para descifrar usando el contenido del QR
                Button(
                    onClick = {
                        try {
                            // Extraer el IV del contenido completo del QR
                            val parts = fullQrContent.split(" ")
                            if (parts.size == 2) {
                                val cipherText = parts[0]
                                val ivBase64 = parts[1]
                                val iv = aes.stringToByteArray(ivBase64)
                                val decrypted = aes.decryptStringWithSharedKey(cipherText, fixedSecretKey, iv)
                                decryptedTextResult = decrypted ?: "Error al descifrar (Tag inválido)"
                            } else {
                                decryptedTextResult = "Formato de QR inválido"
                            }
                        } catch (e: Exception) {
                            decryptedTextResult = "Error: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("🔓 Descifrar desde QR", fontWeight = FontWeight.Medium)
                }
            }

            // --- BLOQUE 3: TEXTO RECUPERADO ---
            if (decryptedTextResult.isNotEmpty() && decryptedTextResult != "Error al descifrar (Tag inválido)") {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "✅ MENSAJE ORIGINAL RECUPERADO:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = decryptedTextResult,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (decryptedTextResult == "Error al descifrar (Tag inválido)") {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "❌ ERROR DE DESCRIFRADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "El IV o la clave no coinciden con los usados en el cifrado.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTE DE RENDERIZADO NATIVO DE QR ---
@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    qrColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val qrCodeMatrix = remember(content) {
        try {
            val writer = QRCodeWriter()
            writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        } catch (e: Exception) {
            null
        }
    }

    if (qrCodeMatrix != null) {
        Canvas(modifier = modifier.background(backgroundColor)) {
            val matrixWidth = qrCodeMatrix.width
            val matrixHeight = qrCodeMatrix.height

            val pixelWidth = size.width / matrixWidth
            val pixelHeight = size.height / matrixHeight

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    if (qrCodeMatrix.get(x, y)) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(x * pixelWidth, y * pixelHeight),
                            size = Size(pixelWidth + 0.5f, pixelHeight + 0.5f)
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error generando QR",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CryptoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}