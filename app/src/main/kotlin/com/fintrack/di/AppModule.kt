package com.fintrack.di

import androidx.room.Room
import com.fintrack.data.local.FinTrackDatabase
import com.fintrack.data.local.SessionManager
import com.fintrack.data.remote.api.*
import com.fintrack.domain.repository.*
import com.fintrack.ui.auth.AuthViewModel
import com.fintrack.ui.bills.BillViewModel
import com.fintrack.ui.budgets.BudgetViewModel
import com.fintrack.ui.dashboard.DashboardViewModel
import com.fintrack.ui.payments.PaymentViewModel
import com.fintrack.ui.transactions.TransactionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Session
    single { SessionManager(androidContext()) }

    // Database
    single {
        Room.databaseBuilder(androidContext(), FinTrackDatabase::class.java, "fintrack.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<FinTrackDatabase>().transactionDao() }
    single { get<FinTrackDatabase>().budgetDao() }
    single { get<FinTrackDatabase>().billDao() }

    // Network
    single { provideJson() }
    single { provideOkHttpClient(get()) }
    single { provideRetrofit(get(), get()) }

    // APIs
    single { provideAuthApi(get()) }
    single { provideTransactionApi(get()) }
    single { provideBudgetApi(get()) }
    single { provideBillApi(get()) }
    single { provideCurrencyApi(get()) }
    single { provideMpesaApi(get()) }
    single { provideStripeApi(get()) }

    // Repositories
    single { AuthRepository(get(), get()) }
    single { TransactionRepository(get(), get()) }
    single { BudgetRepository(get(), get()) }
    single { BillRepository(get(), get()) }
    single { PaymentRepository(get(), get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { TransactionViewModel(get()) }
    viewModel { BudgetViewModel(get()) }
    viewModel { BillViewModel(get()) }
    viewModel { PaymentViewModel(get()) }
}
