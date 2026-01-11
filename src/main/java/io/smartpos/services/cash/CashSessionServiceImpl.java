package io.smartpos.services.cash;

import io.smartpos.core.domain.cash.CashSession;
import io.smartpos.infrastructure.dao.CashSessionDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class CashSessionServiceImpl implements CashSessionService {

    private final CashSessionDao cashSessionDao;
    private final DataSourceProvider dataSource;

    public CashSessionServiceImpl(CashSessionDao cashSessionDao, DataSourceProvider dataSource) {
        this.cashSessionDao = cashSessionDao;
        this.dataSource = dataSource;
    }

    @Override
    public CashSession openSession(int userId, BigDecimal openingBalance) {
        Optional<CashSession> existing = cashSessionDao.findOpenSessionByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        CashSession session = new CashSession();
        session.setUserId(userId);
        session.setOpeningBalance(openingBalance);
        cashSessionDao.save(session);
        return session;
    }

    @Override
    public void closeSession(int sessionId, BigDecimal actualCash) {
        CashSession session = cashSessionDao.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"OPEN".equals(session.getStatus())) {
            throw new RuntimeException("Session already closed");
        }

        BigDecimal totalSales = calculateSalesInSession(session);
        session.setTotalSales(totalSales);
        session.setActualCash(actualCash);
        session.setClosedAt(LocalDateTime.now());
        session.setStatus("CLOSED");

        cashSessionDao.update(session);
    }

    @Override
    public Optional<CashSession> getActiveSession(int userId) {
        return cashSessionDao.findOpenSessionByUserId(userId);
    }

    @Override
    public BigDecimal calculateSalesInSession(CashSession session) {
        String sql = "SELECT SUM(total_amount) FROM sale WHERE user_id = ? AND sale_date >= ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, session.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(session.getOpenedAt()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating sales in session", e);
        }
        return BigDecimal.ZERO;
    }
}
