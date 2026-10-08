package com.appfinanzas.prototype.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val INVESTMENTS = "investments"
    const val PORTFOLIO = "portfolio"
    const val PROJECTION = "projection"
    const val MORE = "more"

    const val INVESTMENT_DETAIL = "investmentDetail/{investmentId}"
    const val ADD_INVESTMENT = "addInvestment"
    const val EDIT_INVESTMENT = "editInvestment/{investmentId}"
    const val ADD_TRANSACTION = "addTransaction/{investmentId}"
    const val EDIT_TRANSACTION = "editTransaction/{investmentId}/{transactionId}"
    const val INSTITUTIONS = "institutions"
    const val SETTINGS = "settings"
    const val SECURITY = "security"
    const val SECURITY_PIN = "securityPin/{mode}"
    const val SECURITY_FORGOT = "securityForgot"

    const val ARG_INVESTMENT_ID = "investmentId"
    const val ARG_TRANSACTION_ID = "transactionId"
    const val ARG_MODE = "mode"

    fun investmentDetail(investmentId: Long): String = "investmentDetail/$investmentId"

    fun editInvestment(investmentId: Long): String = "editInvestment/$investmentId"

    fun addTransaction(investmentId: Long): String = "addTransaction/$investmentId"

    fun editTransaction(investmentId: Long, transactionId: Long): String =
        "editTransaction/$investmentId/$transactionId"

    const val MODE_CREATE = "create"
    const val MODE_CHANGE = "change"

    fun securityPin(mode: String): String = "securityPin/$mode"
}
