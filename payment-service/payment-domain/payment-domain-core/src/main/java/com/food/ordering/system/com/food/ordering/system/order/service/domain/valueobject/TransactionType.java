package com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject;

/**
 * 取引種別を表すEnum。
 * <p>
 * - DEBIT: 顧客のクレジット残高から減算（支払い実行時）
 * - CREDIT: 顧客のクレジット残高へ加算（返金・キャンセル時）
 * </p>
 * CreditHistoryのtransactionTypeで使用されます。
 */
public enum TransactionType {
    /** 顧客クレジット残高からの減算（支払い実行時） */
    DEBIT,
    /** 顧客クレジット残高への加算（返金・キャンセル時） */
    CREDIT
}
