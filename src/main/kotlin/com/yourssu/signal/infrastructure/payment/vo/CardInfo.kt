package com.yourssu.signal.infrastructure.payment.vo

import com.fasterxml.jackson.annotation.JsonProperty

data class CardInfo(
    @JsonProperty("purchase_corp")
    val purchaseCorp: String,
    @JsonProperty("purchase_corp_code")
    val purchaseCorpCode: String,
    @JsonProperty("issuer_corp")
    val issuerCorp: String,
    @JsonProperty("issuer_corp_code")
    val issuerCorpCode: String,
    @JsonProperty("kakaopay_purchase_corp")
    val kakaopayPurchaseCorp: String,
    @JsonProperty("kakaopay_purchase_corp_code")
    val kakaopayPurchaseCorpCode: String,
    @JsonProperty("kakaopay_issuer_corp")
    val kakaopayIssuerCorp: String,
    @JsonProperty("kakaopay_issuer_corp_code")
    val kakaopayIssuerCorpCode: String,
    val bin: String,
    @JsonProperty("card_type")
    val cardType: String,
    @JsonProperty("install_month")
    val installMonth: String,
    @JsonProperty("approved_id")
    val approvedId: String,
    @JsonProperty("card_mid")
    val cardMid: String,
    @JsonProperty("interest_free_install")
    val interestFreeInstall: String,
    @JsonProperty("card_item_code")
    val cardItemCode: String
)