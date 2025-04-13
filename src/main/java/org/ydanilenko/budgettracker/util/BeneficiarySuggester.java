package org.ydanilenko.budgettracker.util;

import org.ydanilenko.budgettracker.model.TransactionDAO;

public class BeneficiarySuggester {
    public static String suggestBeneficiaryForCategory(String categoryName, TransactionDAO dao) {
        if (categoryName == null || categoryName.isBlank()) return null;
        return dao.getTopBeneficiaryByCategory(categoryName.trim());
    }
}
