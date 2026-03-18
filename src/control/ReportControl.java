package control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import entity.Consts;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class ReportControl {
    // Singleton
    private static ReportControl instance;

    public static ReportControl getInstance() {
        if (instance == null) {
            instance = new ReportControl();
        }
        return instance;
    }

    private ReportControl() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("UCanAccess driver not found. Check build path / jars.", e);
        }
    }

    // Produces the Unregistered Class Report
    public JFrame produceUnregisteredClassReport(Date start, Date end)throws SQLException, JRException, IOException {
        LocalDate s = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate e = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        HashMap<String, Object> params = new HashMap<>();
        params.put("pStart", java.sql.Date.valueOf(s));
        params.put("pEnd",   java.sql.Date.valueOf(e));

        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             InputStream in = ReportControl.class.getResourceAsStream(Consts.UNREGISTERED_REPORT_JASPER)) {

            if (in == null) {
                throw new IOException("Report resource not found: " + Consts.UNREGISTERED_REPORT_JASPER);
            }

            JasperPrint print = JasperFillManager.fillReport(in, params, conn);

            JFrame frame = new JFrame("Unregistered Class Report");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(new JRViewer(print));
            frame.setSize(1200, 900);
            frame.setLocationRelativeTo(null);

            return frame;
        }
    }
    
    // Produces the Inventory Report
    public JFrame produceInventoryReport() throws SQLException, JRException, IOException {
        try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
             InputStream in = ReportControl.class.getResourceAsStream(Consts.INVENTORY_REPORT_JASPER)) {
            if (in == null)
                throw new IOException("Report resource not found: " + Consts.INVENTORY_REPORT_JASPER);
            JasperPrint print = JasperFillManager.fillReport(in, new HashMap<>(), conn);
            JFrame frame = new JFrame("Equipment Inventory Report");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(new JRViewer(print));
            frame.setSize(1200, 900);
            frame.setLocationRelativeTo(null);
            return frame;
        }
    }
  
    // Export for XML (SwiftFit system)
    public boolean exportInventoryToXML(String path) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element root = doc.createElement("SwiftFitInventoryReport");
            doc.appendChild(root);

            Element base = doc.createElement("EquipmentList");
            root.appendChild(base);

            try (Connection conn = DriverManager.getConnection(Consts.getDbJdbcUrl());
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INVENTORY_FOR_XML);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Element equipment = doc.createElement("EquipmentItem");
                    base.appendChild(equipment);

                    Attr attr = doc.createAttribute("category");
                    attr.setValue(rs.getString("category"));
                    equipment.setAttributeNode(attr);

                    Element name = doc.createElement("TypeName");
                    name.appendChild(doc.createTextNode(rs.getString("typeName")));
                    equipment.appendChild(name);

                    Element usage = doc.createElement("AnnualUsage");
                    usage.appendChild(doc.createTextNode(String.valueOf(rs.getInt("usageCount"))));
                    equipment.appendChild(usage);
                }
            }

            File outputFile = new File(path);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outputFile);
            
            TransformerFactory.newInstance().newTransformer().transform(source, result);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}