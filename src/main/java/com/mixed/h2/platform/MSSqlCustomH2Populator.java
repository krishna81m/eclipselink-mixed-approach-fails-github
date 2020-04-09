package com.mixed.h2.platform;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class MSSqlCustomH2Populator {
    private File csvImportDir;

    public MSSqlCustomH2Populator(File csvImportDir) {
        this.csvImportDir = csvImportDir;
    }

    public void initSchema(Connection conn) throws SQLException {
        CallableStatement dropSchema = null;
        if (!csvImportDir.getParentFile().exists()) {
            csvImportDir.getParentFile().mkdirs();
        }
        try {
            dropSchema = conn.prepareCall("DROP SCHEMA \"dbo\"");
            dropSchema.execute();
        } catch (SQLException se) {
        } finally {
            if (dropSchema != null) {
                dropSchema.close();
            }
        }

        CallableStatement populateDb = null;
        try {
            populateDb = conn.prepareCall("RUNSCRIPT FROM ?");
            populateDb.setString(1, csvImportDir.getCanonicalPath());
            populateDb.execute();
        } catch (IOException ex) {
            throw new SQLException("unable create the backupFile", ex);
        } finally {
            if (populateDb != null) {
                populateDb.close();
            }
        }

    }
}
