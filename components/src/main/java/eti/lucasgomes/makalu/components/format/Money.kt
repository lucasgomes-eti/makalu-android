package eti.lucasgomes.makalu.components.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import eti.lucasgomes.makalu.components.R
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private val appCurrency: Currency = Currency.getInstance("USD")

internal class MoneyFormatter(locale: Locale) {
    private val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        currency = appCurrency
        minimumFractionDigits = appCurrency.defaultFractionDigits
        maximumFractionDigits = appCurrency.defaultFractionDigits
    }

    fun format(amount: BigDecimal): String = numberFormat.format(amount)
}

@Composable
private fun rememberMoneyFormatter(): MoneyFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { MoneyFormatter(locale) }
}

@Composable
fun formatMoney(amount: BigDecimal): String = rememberMoneyFormatter().format(amount)

@Composable
fun formatMoneyAddition(amount: BigDecimal): String =
    stringResource(R.string.money_addition, formatMoney(amount))
