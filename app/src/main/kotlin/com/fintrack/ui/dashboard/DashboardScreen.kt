package com.fintrack.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.domain.model.Bill
import com.fintrack.domain.model.BudgetStatus
import com.fintrack.domain.model.TransactionSummary
import com.fintrack.ui.theme.ExpenseRed
import com.fintrack.ui.theme.IncomeGreen
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val now = LocalDate.now()
    val monthName = now.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FinTrack", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = Color.White)
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Summary card
                item {
                    SummaryCard(
                        summary = uiState.summary,
                        monthName = monthName,
                        year = now.year
                    )
                }

                // Quick actions
                item {
                    Text("Quick Actions", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.List,
                            label = "Transactions",
                            onClick = onNavigateToTransactions,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.PieChart,
                            label = "Budgets",
                            onClick = onNavigateToBudgets,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Receipt,
                            label = "Bills",
                            onClick = onNavigateToBills,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Payment,
                            label = "Payments",
                            onClick = onNavigateToPayments,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Budget status
                if (uiState.budgetStatuses.isNotEmpty()) {
                    item {
                        Text("Budget Status", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    items(uiState.budgetStatuses.take(3)) { status ->
                        BudgetStatusCard(status)
                    }
                }

                // Upcoming bills
                if (uiState.upcomingBills.isNotEmpty()) {
                    item {
                        Text("Upcoming Bills (7 days)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    items(uiState.upcomingBills) { bill ->
                        UpcomingBillCard(bill)
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SummaryCard(summary: TransactionSummary?, monthName: String, year: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "$monthName $year",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "KES ${String.format("%,.2f", summary?.netBalance ?: 0.0)}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Net Balance",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Income", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        "KES ${String.format("%,.2f", summary?.totalIncome ?: 0.0)}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expenses", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        "KES ${String.format("%,.2f", summary?.totalExpense ?: 0.0)}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun BudgetStatusCard(status: BudgetStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(status.budget.category, fontWeight = FontWeight.Medium)
                Text(
                    "${String.format("%.0f", status.percentage)}%",
                    color = if (status.percentage >= 90) ExpenseRed else IncomeGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (status.percentage / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (status.percentage >= 90) ExpenseRed else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "KES ${String.format("%,.2f", status.spent)} / ${String.format("%,.2f", status.budget.limitAmount)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun UpcomingBillCard(bill: Bill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bill.title, fontWeight = FontWeight.Medium)
                Text(
                    "Due: ${bill.nextDueDate.take(10)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                "${bill.currency} ${String.format("%,.2f", bill.amount)}",
                fontWeight = FontWeight.Bold,
                color = ExpenseRed
            )
        }
    }
}
