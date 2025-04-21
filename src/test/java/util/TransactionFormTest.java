package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.ydanilenko.budgettracker.view.TransactionForm;

public class TransactionFormTest {

    @Test
    void testIsValidAmount() {
        assertTrue(TransactionForm.isValidAmount("100.0"));
        assertFalse(TransactionForm.isValidAmount("-50"));
        assertFalse(TransactionForm.isValidAmount("abc"));
        assertFalse(TransactionForm.isValidAmount(""));
        assertFalse(TransactionForm.isValidAmount(" "));
        assertFalse(TransactionForm.isValidAmount("100.00.1"));
        assertTrue(TransactionForm.isValidAmount("0"));
    }

    @Test
    void testIsValidCategory() {
        assertTrue(TransactionForm.isValidCategory("Food"));
        assertFalse(TransactionForm.isValidCategory(""));
        assertFalse(TransactionForm.isValidCategory(null));
    }

    @Test
    void testIsValidPaymentType() {
        assertTrue(TransactionForm.isValidPaymentType("Card"));
        assertFalse(TransactionForm.isValidPaymentType(null));
    }

    @Test
    void testIsValidPlaceAndBeneficiary() {
        assertTrue(TransactionForm.isValidPlaceAndBeneficiary("Berlin", "Lidl"));
        assertFalse(TransactionForm.isValidPlaceAndBeneficiary("", "Lidl"));
        assertFalse(TransactionForm.isValidPlaceAndBeneficiary("Berlin", ""));
    }
}
