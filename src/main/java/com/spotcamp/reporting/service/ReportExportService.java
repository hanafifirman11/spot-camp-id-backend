package com.spotcamp.reporting.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.spotcamp.campsite.domain.Campsite;
import com.spotcamp.campsite.repository.CampsiteRepository;
import com.spotcamp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportExportService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PAYMENT_PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"
    );
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_DATE;

    private final DataSource dataSource;
    private final CampsiteRepository campsiteRepository;

    public ExportedFile exportBookingReports(ReportExportRequest request) {
        Path csvPath = null;
        try {
            csvPath = exportBookingReportsCsv(request);
            if ("csv".equalsIgnoreCase(request.format())) {
                return ExportedFile.builder()
                        .path(csvPath)
                        .contentType("text/csv")
                        .filename(buildFilename("booking-reports", "csv"))
                        .build();
            }

            Path outputPath = switch (request.format().toLowerCase()) {
                case "xlsx" -> exportBookingReportsXlsx(csvPath);
                case "pdf" -> exportBookingReportsPdf(csvPath);
                default -> throw new BusinessException("Unsupported export format");
            };

            Files.deleteIfExists(csvPath);

            String contentType = "application/octet-stream";
            if ("xlsx".equalsIgnoreCase(request.format())) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if ("pdf".equalsIgnoreCase(request.format())) {
                contentType = "application/pdf";
            }

            return ExportedFile.builder()
                    .path(outputPath)
                    .contentType(contentType)
                    .filename(buildFilename("booking-reports", request.format()))
                    .build();
        } catch (IOException ex) {
            throw new BusinessException("Failed to export booking reports");
        } catch (Exception ex) {
            log.error("Export booking reports failed", ex);
            throw new BusinessException("Failed to export booking reports");
        } finally {
            if (csvPath != null && !"csv".equalsIgnoreCase(request.format())) {
                try {
                    Files.deleteIfExists(csvPath);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private Path exportBookingReportsCsv(ReportExportRequest request) throws Exception {
        Path csvPath = Files.createTempFile("booking-reports-", ".csv");
        String selectSql = buildSelectSql(request);
        String copySql = "COPY (" + selectSql + ") TO STDOUT WITH (FORMAT CSV, HEADER TRUE)";

        try (Connection connection = dataSource.getConnection();
             OutputStream outputStream = Files.newOutputStream(csvPath)) {
            if (isPostgres(connection)) {
                try {
                    BaseConnection pgConnection = connection.unwrap(BaseConnection.class);
                    CopyManager copyManager = new CopyManager(pgConnection);
                    copyManager.copyOut(copySql, outputStream);
                } catch (SQLException ex) {
                    log.warn("COPY export failed, falling back to JDBC streaming CSV.", ex);
                    writeCsvFromQuery(connection, selectSql, csvPath);
                }
            } else {
                log.info("COPY export skipped (non-PostgreSQL database), using JDBC streaming CSV.");
                writeCsvFromQuery(connection, selectSql, csvPath);
            }
        }

        return csvPath;
    }

    private String buildSelectSql(ReportExportRequest request) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");

        if (request.campsiteId() != null) {
            where.append(" AND br.campsite_id = ").append(request.campsiteId());
        }

        if (request.businessId() != null) {
            where.append(" AND c.owner_id = ").append(request.businessId());
        }

        if (request.status() != null && !request.status().isBlank()) {
            String status = request.status().trim().toUpperCase();
            if (!ALLOWED_STATUSES.contains(status)) {
                throw new BusinessException("Invalid status filter");
            }
            where.append(" AND br.status = '").append(status).append("'");
        }

        if (request.fromDate() != null) {
            where.append(" AND br.booking_date >= '").append(request.fromDate().format(DATE)).append("'");
        }

        if (request.toDate() != null) {
            where.append(" AND br.booking_date <= '").append(request.toDate().format(DATE)).append("'");
        }

        String query = """
                SELECT
                  br.booking_id AS "booking_id",
                  br.campsite_name AS "campsite",
                  br.user_email AS "guest_email",
                  br.spot_name AS "spot_name",
                  br.spot_id AS "spot_id",
                  br.check_in_date AS "check_in",
                  br.check_out_date AS "check_out",
                  br.nights AS "nights",
                  br.status AS "status",
                  br.total_amount AS "total_amount",
                  br.payment_method AS "payment_method",
                  br.created_at AS "created_at"
                FROM booking_reports br
                LEFT JOIN campsites c ON c.id = br.campsite_id
                """;

        return query + where + " ORDER BY br.booking_date DESC";
    }

    private void writeCsvFromQuery(Connection connection, String query, Path csvPath) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery();
             CSVPrinter printer = new CSVPrinter(
                     Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT)) {

            ResultSetMetaData meta = resultSet.getMetaData();
            int columnCount = meta.getColumnCount();
            List<String> headers = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                headers.add(meta.getColumnLabel(i));
            }
            printer.printRecord(headers);

            while (resultSet.next()) {
                List<String> row = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    row.add(value != null ? value.toString() : "");
                }
                printer.printRecord(row);
            }
        }
    }

    private boolean isPostgres(Connection connection) {
        try {
            String product = connection.getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgres");
        } catch (SQLException ex) {
            return false;
        }
    }

    private Path exportBookingReportsXlsx(Path csvPath) throws Exception {
        Path outputPath = Files.createTempFile("booking-reports-", ".xlsx");

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader);
             SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             FileOutputStream out = new FileOutputStream(outputPath.toFile())) {

            Sheet sheet = workbook.createSheet("Booking Reports");
            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());

            int rowIndex = 0;
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            for (CSVRecord record : parser) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < headers.size(); i++) {
                    String value = record.get(i);
                    row.createCell(i).setCellValue(value);
                }
            }

            workbook.write(out);
            workbook.dispose();
        }

        return outputPath;
    }

    private Path exportBookingReportsPdf(Path csvPath) throws Exception {
        Path outputPath = Files.createTempFile("booking-reports-", ".pdf");

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader);
             FileOutputStream out = new FileOutputStream(outputPath.toFile())) {

            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 8);
            document.add(new Paragraph("Booking Reports", titleFont));
            document.add(new Paragraph(" "));

            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
            int columnCount = headers.size();

            PdfPTable table = createPdfTable(headers, headerFont);
            int rowCounter = 0;

            for (CSVRecord record : parser) {
                for (int i = 0; i < columnCount; i++) {
                    table.addCell(new PdfPCell(new Phrase(record.get(i), bodyFont)));
                }
                rowCounter++;
                if (rowCounter % 40 == 0) {
                    document.add(table);
                    table = createPdfTable(headers, headerFont);
                }
            }

            if (table.size() > 0) {
                document.add(table);
            }

            document.close();
        }

        return outputPath;
    }

    private PdfPTable createPdfTable(List<String> headers, Font headerFont) {
        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        return table;
    }

    private String buildFilename(String base, String extension) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return base + "-" + date + "." + extension;
    }

    public record ReportExportRequest(
            Long campsiteId,
            Long businessId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            String format
    ) {}

    @lombok.Builder
    public record ExportedFile(Path path, String contentType, String filename) {}
}
