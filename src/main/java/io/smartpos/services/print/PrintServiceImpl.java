package io.smartpos.services.print;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.core.domain.sale.SaleItem;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PrintServiceImpl implements PrintService {

    @Override
    public void printSaleTicket(Sale sale) {
        VBox ticket = new VBox(5);
        ticket.setPrefWidth(300); // Standard thermal width approx
        ticket.setStyle("-fx-background-color: white; -fx-padding: 20;");
        ticket.setAlignment(Pos.TOP_CENTER);

        // Header
        Label title = new Label("SMART POS SYSTEM");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

        Label address = new Label("123 Tech Avenue, City\nTel: 555-0199");
        address.setFont(Font.font("Courier New", 10));
        address.setAlignment(Pos.CENTER);

        ticket.getChildren().addAll(title, address);

        // Divider
        ticket.getChildren().add(new Label("--------------------------------"));

        // Details
        Label info = new Label("Date: " + sale.getSaleDate() + "\nTicket: #" + sale.getId());
        info.setFont(Font.font("Courier New", 10));
        ticket.getChildren().add(info);

        ticket.getChildren().add(new Label("--------------------------------"));

        // Items
        for (SaleItem item : sale.getItems()) {
            String line = String.format("%-15s %4.1f x %6.2f",
                    item.getProductName() != null ? item.getProductName() : "Product",
                    item.getQuantity(),
                    item.getUnitPrice());
            Label itemLabel = new Label(line);
            itemLabel.setFont(Font.font("Courier New", 10));
            ticket.getChildren().add(itemLabel);
        }

        ticket.getChildren().add(new Label("--------------------------------"));

        // Total
        Label total = new Label("TOTAL: $" + sale.getTotalAmount());
        total.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        ticket.getChildren().add(total);

        ticket.getChildren().add(new Label("\nTHANK YOU FOR YOUR PURCHASE!"));

        // Print Logic
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // job.showPrintDialog(null); // Optional: show dialog
            boolean success = job.printPage(ticket);
            if (success) {
                job.endJob();
            }
        } else {
            System.err.println("No default printer found.");
        }
    }

    @Override
    public void printZReport(io.smartpos.core.domain.cash.CashSession session, String username) {
        VBox ticket = new VBox(5);
        ticket.setPrefWidth(300);
        ticket.setStyle("-fx-background-color: white; -fx-padding: 20;");
        ticket.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Z-REPORT (SHIFT CLOSURE)");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        ticket.getChildren().add(title);

        ticket.getChildren().add(new Label("--------------------------------"));

        Label info = new Label("User: " + username + "\n" +
                "Opened: " + session.getOpenedAt() + "\n" +
                "Closed: " + session.getClosedAt());
        info.setFont(Font.font("Courier New", 10));
        ticket.getChildren().add(info);

        ticket.getChildren().add(new Label("--------------------------------"));

        String body = String.format(
                "Opening Bal:   %10.2f\n" +
                        "Total Sales:   %10.2f\n" +
                        "Expected:      %10.2f\n" +
                        "Actual Cash:   %10.2f\n" +
                        "--------------------------------\n" +
                        "DIFFERENCE:    %10.2f",
                session.getOpeningBalance(),
                session.getTotalSales(),
                session.getExpectedCash(),
                session.getActualCash(),
                session.getDifference());

        Label bodyLabel = new Label(body);
        bodyLabel.setFont(Font.font("Courier New", 10));
        ticket.getChildren().add(bodyLabel);

        ticket.getChildren().add(new Label("\n*** END OF REPORT ***"));

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.printPage(ticket);
            if (success) {
                job.endJob();
            } else {
                System.err.println("Print page failed.");
            }
        } else {
            System.err.println("Could not create PrinterJob. Check default printer.");
        }
    }

    @Override
    public void printCancellationTicket(Sale sale, String username) {
        VBox ticket = new VBox(5);
        ticket.setPrefWidth(300);
        ticket.setStyle("-fx-background-color: white; -fx-padding: 20;");
        ticket.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("!!! SALE CANCELLED !!!");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        title.setTextFill(javafx.scene.paint.Color.RED);
        ticket.getChildren().add(title);

        ticket.getChildren().add(new Label("--------------------------------"));
        Label info = new Label("Original Sale #: " + sale.getId() + "\n" +
                "Cancelled by: " + username + "\n" +
                "Date: " + java.time.LocalDateTime.now());
        info.setFont(Font.font("Courier New", 10));
        ticket.getChildren().add(info);

        ticket.getChildren().add(new Label("--------------------------------"));
        Label totalLabel = new Label("Reversed Amount: " + String.format("%.2f", sale.getTotalAmount()));
        totalLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        ticket.getChildren().add(totalLabel);

        ticket.getChildren().add(new Label("\nStock has been restored."));

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            job.printPage(ticket);
            job.endJob();
        }
    }

    @Override
    public void printSalesReport(java.util.List<io.smartpos.core.reporting.SaleReport> sales, java.time.LocalDate start,
            java.time.LocalDate end) {
        VBox report = new VBox(5);
        report.setPrefWidth(500);
        report.setStyle("-fx-background-color: white; -fx-padding: 30;");
        report.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("SALES REPORT");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        Label range = new Label("Period: " + start + " to " + end);
        range.setFont(Font.font("Courier New", 12));
        report.getChildren().addAll(title, range, new Label("--------------------------------------------------"));

        // Header table
        String header = String.format("%-10s %-15s %15s", "ID", "Date", "Amount");
        Label headerLabel = new Label(header);
        headerLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        report.getChildren().add(headerLabel);
        report.getChildren().add(new Label("--------------------------------------------------"));

        java.math.BigDecimal grandTotal = java.math.BigDecimal.ZERO;
        for (io.smartpos.core.reporting.SaleReport s : sales) {
            String row = String.format("%-10d %-15s %15.2f", s.getSaleId(), s.getSaleDate().toString(), s.getTotal());
            Label rowLabel = new Label(row);
            rowLabel.setFont(Font.font("Courier New", 11));
            report.getChildren().add(rowLabel);
            grandTotal = grandTotal.add(s.getTotal());
        }

        report.getChildren().add(new Label("--------------------------------------------------"));
        Label totalLabel = new Label(String.format("GRAND TOTAL: $%.2f", grandTotal));
        totalLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        report.getChildren().add(totalLabel);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.printPage(report);
            if (success) {
                job.endJob();
            }
        }
    }
}
