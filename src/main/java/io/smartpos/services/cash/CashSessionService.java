package io.smartpos.services.cash;

import io.smartpos.core.domain.cash.CashSession;
import java.math.BigDecimal;
import java.util.Optional;

public interface CashSessionService {
    CashSession openSession(int userId, BigDecimal openingBalance);

    void closeSession(int sessionId, BigDecimal actualCash);

    Optional<CashSession> getActiveSession(int userId);

    BigDecimal calculateSalesInSession(CashSession session);
}
