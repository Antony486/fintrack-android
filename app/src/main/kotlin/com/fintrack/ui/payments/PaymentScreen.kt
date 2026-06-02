package com.fintrack.ui.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.domain.model.Result
import com.fintrack.domain.repository.PaymentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// ─── ViewModel ───────────────────────────────────────────────────────────────

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Loading : PaymentStatus()
    data class StkSent(val checkoutRequestId: String) : PaymentStatus()
    data class StripeReady(val clientSecret: String) : PaymentStatus()
    data class MpesaPolling(val checkoutRequestId: String, val status: String) : PaymentStatus()
    data class Success(val message: String) : PaymentStatus()
    data class Error(val message: String) : PaymentStatus()
}

data class PaymentUiState(
    val status: PaymentStatus = PaymentStatus.Idle,
    val selectedTab: Int = 0   // 0 = M-Pesa, 1 = Stripe
)

class PaymentViewModel(private val repository: PaymentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState

    fun initiateStk(phone: String, amount: Double) {
        if (phone.isBlank() || amount <= 0) {
            _uiState.value = _uiState.value.copy(status = PaymentStatus.Error("Enter a valid phone and amount"))
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = PaymentStatus.Loading)
            when (val result = repository.initiateStkPush(phone, amount, "FinTrack", "FinTrack Payment")) {
                is Result.Success -> {
                    val checkoutId = result.data
                    _uiState.value = _uiState.value.copy(status = PaymentStatus.StkSent(checkoutId))
                    pollMpesaStatus(checkoutId)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(status = PaymentStatus.Error(result.message))
                else -> {}
            }
        }
    }

    private fun pollMpesaStatus(checkoutRequestId: String) {
        viewModelScope.launch {
            repeat(10) { attempt ->
                delay(5000) // poll every 5s, max 10 times (50s)
                when (val result = repository.getMpesaStatus(checkoutRequestId)) {
                    is Result.Success -> {
                        val status = result.data
                        when (status) {
                            "SUCCESS" -> {
                                _uiState.value = _uiState.value.copy(status = PaymentStatus.Success("M-Pesa payment successful!"))
                                return@launch
                            }
                            "FAILED" -> {
                                _uiState.value = _uiState.value.copy(status = PaymentStatus.Error("M-Pesa payment failed or cancelled"))
                                return@launch
                            }
                            else -> {
                                _uiState.value = _uiState.value.copy(
                                    status = PaymentStatus.MpesaPolling(checkoutRequestId, "Waiting for PIN... (${attempt + 1}/10)")
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
            // Timed out
            if (_uiState.value.status !is PaymentStatus.Success) {
                _uiState.value = _uiState.value.copy(
                    status = PaymentStatus.Error("Payment timed out. Check your M-Pesa messages.")
                )
            }
        }
    }

    fun createStripeIntent(amount: Double, currency: String) {
        if (amount <= 0) {
            _uiState.value = _uiState.value.copy(status = PaymentStatus.Error("Enter a valid amount"))
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = PaymentStatus.Loading)
            when (val result = repository.createStripePaymentIntent(amount, currency)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    status = PaymentStatus.StripeReady(result.data)
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(status = PaymentStatus.Error(result.message))
                else -> {}
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index, status = PaymentStatus.Idle)
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(status = PaymentStatus.Idle)
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    viewModel: PaymentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payments") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Tab row
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("M-Pesa") },
                    icon = { Icon(Icons.Default.PhoneAndroid, null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Card (Stripe)") },
                    icon = { Icon(Icons.Default.CreditCard, null) }
                )
            }

            Spacer(Modifier.height(16.dp))

            when (uiState.selectedTab) {
                0 -> MpesaPanel(
                    status = uiState.status,
                    onInitiate = { phone, amount -> viewModel.initiateStk(phone, amount) },
                    onReset = { viewModel.reset() }
                )
                1 -> StripePanel(
                    status = uiState.status,
                    onInitiate = { amount, currency -> viewModel.createStripeIntent(amount, currency) },
                    onReset = { viewModel.reset() }
                )
            }
        }
    }
}

@Composable
fun MpesaPanel(
    status: PaymentStatus,
    onInitiate: (String, Double) -> Unit,
    onReset: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00A651).copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhoneAndroid, null, tint = Color(0xFF00A651), modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("M-Pesa STK Push", fontWeight = FontWeight.Bold)
                    Text("Enter your Safaricom number and amount", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            placeholder = { Text("e.g. 0712345678 or 254712345678") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (KES)") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Status display
        when (val s = status) {
            is PaymentStatus.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text("Sending STK Push...", color = MaterialTheme.colorScheme.primary)
                }
            }
            is PaymentStatus.StkSent -> {
                StatusInfoCard("📱 Check your phone and enter your M-Pesa PIN", Color(0xFF1565C0))
            }
            is PaymentStatus.MpesaPolling -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text(s.status, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
            is PaymentStatus.Success -> {
                StatusInfoCard("✅ ${s.message}", Color(0xFF2E7D32))
            }
            is PaymentStatus.Error -> {
                StatusInfoCard("❌ ${s.message}", MaterialTheme.colorScheme.error)
            }
            else -> {}
        }

        if (status is PaymentStatus.Success || status is PaymentStatus.Error) {
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text("New Payment")
            }
        } else {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    onInitiate(phone, amt)
                },
                enabled = status !is PaymentStatus.Loading
                        && status !is PaymentStatus.StkSent
                        && status !is PaymentStatus.MpesaPolling
                        && phone.isNotBlank()
                        && amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A651))
            ) {
                Icon(Icons.Default.PhoneAndroid, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Send STK Push", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StripePanel(
    status: PaymentStatus,
    onInitiate: (Double, String) -> Unit,
    onReset: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("kes") }
    var currencyExpanded by remember { mutableStateOf(false) }
    val currencies = listOf("kes", "usd", "eur", "gbp")

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF635BFF).copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, null, tint = Color(0xFF635BFF), modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Stripe Card Payment", fontWeight = FontWeight.Bold)
                    Text("Secure payment via Stripe", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(expanded = currencyExpanded, onExpandedChange = { currencyExpanded = it }) {
            OutlinedTextField(
                value = currency.uppercase(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Currency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(currencyExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                currencies.forEach {
                    DropdownMenuItem(
                        text = { Text(it.uppercase()) },
                        onClick = { currency = it; currencyExpanded = false }
                    )
                }
            }
        }

        // Status display
        when (val s = status) {
            is PaymentStatus.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text("Creating payment intent...", color = MaterialTheme.colorScheme.primary)
                }
            }
            is PaymentStatus.StripeReady -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("✅ Payment Intent Created", fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Client Secret: ${s.clientSecret.take(30)}...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Use this client secret in the Stripe Payment Sheet to complete payment.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            is PaymentStatus.Success -> {
                StatusInfoCard("✅ ${s.message}", Color(0xFF2E7D32))
            }
            is PaymentStatus.Error -> {
                StatusInfoCard("❌ ${s.message}", MaterialTheme.colorScheme.error)
            }
            else -> {}
        }

        if (status is PaymentStatus.Success || status is PaymentStatus.Error || status is PaymentStatus.StripeReady) {
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text("New Payment")
            }
        } else {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    onInitiate(amt, currency)
                },
                enabled = status !is PaymentStatus.Loading && amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF))
            ) {
                Icon(Icons.Default.CreditCard, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Create Payment Intent", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun StatusInfoCard(message: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
