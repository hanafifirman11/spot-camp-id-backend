package com.spotcamp.module.reporting.service;

import com.spotcamp.module.campsite.repository.CampsiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.core.BaseConnection;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportExportServiceTest {

    @Test
    void exportBookingReportsCsv_UsesJdbcFallbackForNonPostgres() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        CampsiteRepository campsiteRepository = mock(CampsiteRepository.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMeta = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("H2");
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMeta);
        when(resultSetMeta.getColumnCount()).thenReturn(2);
        when(resultSetMeta.getColumnLabel(1)).thenReturn("booking_id");
        when(resultSetMeta.getColumnLabel(2)).thenReturn("status");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenReturn("CONFIRMED");

        ReportExportService service = new ReportExportService(dataSource, campsiteRepository);
        ReportExportService.ReportExportRequest request = new ReportExportService.ReportExportRequest(
                null, null, null, null, null, "csv"
        );

        ReportExportService.ExportedFile exported = service.exportBookingReports(request);
        Path path = exported.path();
        List<String> lines = Files.readAllLines(path);

        assertEquals("booking_id,status", lines.get(0));
        assertTrue(lines.get(1).contains("1"));
        assertTrue(lines.get(1).contains("CONFIRMED"));

        verify(connection, never()).unwrap(eq(BaseConnection.class));

        Files.deleteIfExists(path);
    }
}
