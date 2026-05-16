
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.snapcompany.snapsafe.utilities.AESEncryption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoView() {
    val aes = remember { AESEncryption() }
    val fixedSecretKey = remember { "0123456789012345".toByteArray() }

    var plainText by remember { mutableStateOf("") }
    var encryptedTextResult by remember { mutableStateOf("") }
    var decryptedTextResult by remember { mutableStateOf("") }
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

            Button(
                onClick = {
                    if (plainText.isNotBlank()) {
                        val iv = aes.generateIv()
                        lastGeneratedIv = iv
                        val encrypted = aes.encryptStringWithSharedKey(plainText, fixedSecretKey, iv)
                        encryptedTextResult = encrypted ?: "Error al cifrar"
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

            // --- BLOQUE 2: RESULTADO CIFRADO & CÓDIGO QR ---
            if (encryptedTextResult.isNotEmpty() && encryptedTextResult != "Error al cifrar") {

                // Mostrar Código QR del texto encriptado
                Text(
                    text = "CÓDIGO QR GENERADO (CONTENIDO ENCRIPTADO)",
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
                        content = encryptedTextResult,
                        modifier = Modifier.fillMaxSize(),
                        qrColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "String encriptado (Base64) en el QR:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = encryptedTextResult,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = {
                        val iv = lastGeneratedIv
                        if (iv != null) {
                            val decrypted = aes.decryptStringWithSharedKey(encryptedTextResult, fixedSecretKey, iv)
                            decryptedTextResult = decrypted ?: "Error al descifrar (Tag inválido)"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Descifrar desde memoria", fontWeight = FontWeight.Medium)
                }
            }

            // --- BLOQUE 3: TEXTO RECUPERADO ---
            if (decryptedTextResult.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MENSAJE ORIGINAL RECUPERADO:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = decryptedTextResult,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
    qrColor: Color = Color.Black
) {
    val qrCodeMatrix = remember(content) {
        try {
            val writer = QRCodeWriter()
            // Codifica el contenido encriptado en una matriz binaria de ZXing
            writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        } catch (e: Exception) {
            null
        }
    }

    if (qrCodeMatrix != null) {
        Canvas(modifier = modifier) {
            val matrixWidth = qrCodeMatrix.width
            val matrixHeight = qrCodeMatrix.height

            // Calculamos el tamaño proporcional de cada píxel ("módulo") en el Canvas
            val pixelWidth = size.width / matrixWidth
            val pixelHeight = size.height / matrixHeight

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    // Si el bit es verdadero, se dibuja un cuadrado
                    if (qrCodeMatrix.get(x, y)) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(x * pixelWidth, y * pixelHeight),
                            size = Size(pixelWidth + 0.5f, pixelHeight + 0.5f) // El +0.5f evita líneas de separación por redondeo
                        )
                    }
                }
            }
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