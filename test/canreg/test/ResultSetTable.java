package canreg.test;

/**
 *
 * @author ervikm
 */
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class ResultSetTable {

    public static void main(String[] args) {
        JFrame frame = new ResultSetFrame();
        frame.show();
    }
}

/* this class is the base class for the scrolling and the
caching result set table model. It stores the result set
and its metadata.
 */
abstract class ResultSetTableModel extends AbstractTableModel {

    public ResultSetTableModel(ResultSet aResultSet) {
        rs = aResultSet;
        try {
            rsmd = rs.getMetaData();
        } catch (SQLException e) {
            System.out.println("Error " + e);
        }
    }

    @Override
    public String getColumnName(int c) {
        try {
            return rsmd.getColumnName(c + 1);
        } catch (SQLException e) {
            System.out.println("Error " + e);
            return "";
        }
    }

    @Override
    public int getColumnCount() {
        try {
            return rsmd.getColumnCount();
        } catch (SQLException e) {
            System.out.println("Error " + e);
            return 0;
        }
    }

    protected ResultSet getResultSet() {
        return rs;
    }
    private ResultSet rs;
    private ResultSetMetaData rsmd;
}

/* this class uses a scrolling cursor, a JDBC 2 feature
 */
class ScrollingResultSetTableModel extends ResultSetTableModel {

    public ScrollingResultSetTableModel(ResultSet aResultSet) {
        super(aResultSet);
    }

    @Override
    public Object getValueAt(int r, int c) {
        try {
            ResultSet rs = getResultSet();
            rs.absolute(r + 1);
            return rs.getObject(c + 1);
        } catch (SQLException e) {
            System.out.println("Error " + e);
            return null;
        }
    }

    @Override
    public int getRowCount() {
        try {
            ResultSet rs = getResultSet();
            rs.last();
            return rs.getRow();
        } catch (SQLException e) {
            System.out.println("Error " + e);
            return 0;
        }
    }
}

/* this class caches the result set data; it can be used
if scrolling cursors are not supported
 */
class CachingResultSetTableModel extends ResultSetTableModel {

    public CachingResultSetTableModel(ResultSet aResultSet) {
        super(aResultSet);
        try {
            cache = new ArrayList();
            int cols = getColumnCount();
            ResultSet rs = getResultSet();

            /* place all data in an array list of Object[] arrays
            We don't use an Object[][] because we don't know
            how many rows are in the result set
             */

            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int j = 0; j < row.length; j++) {
                    row[j] = rs.getObject(j + 1);
                }
                cache.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error " + e);
        }
    }

    @Override
    public Object getValueAt(int r, int c) {
        if (r < cache.size()) {
            return ((Object[]) cache.get(r))[c];
        } else {
            return null;
        }
    }

    @Override
    public int getRowCount() {
        return cache.size();
    }
    private ArrayList cache;
}

class ResultSetFrame extends JFrame
        implements ActionListener {

    public ResultSetFrame() {
        setTitle("ResultSet");
        setSize(300, 200);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        /* find all tables in the database and add them to
        a combo box
         */

        Container contentPane = getContentPane();
        tableNames = new JComboBox();
        tableNames.addActionListener(this);
        JPanel p = new JPanel();
        p.add(tableNames);
        contentPane.add(p, "North");

        try {
            Class.forName("com.pointbase.jdbc.jdbcDriver");
            // force loading of driver
            String url = "jdbc:pointbase:corejava";
            String user = "PUBLIC";
            String password = "PUBLIC";
            con = DriverManager.getConnection(url, user,
                    password);
            if (SCROLLABLE) {
                stmt = con.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
            } else {
                stmt = con.createStatement();
            }
            DatabaseMetaData md = con.getMetaData();
            ResultSet mrs = md.getTables(null, null, null,
                    new String[]{"TABLE"});
            while (mrs.next()) {
                tableNames.addItem(mrs.getString(3));
            }
            mrs.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Error " + e);
        } catch (SQLException e) {
            System.out.println("Error " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == tableNames) {  // show the selected table from the combo box

            if (scrollPane != null) {
                getContentPane().remove(scrollPane);
            }
            try {
                String tableName = (String) tableNames.getSelectedItem();
                if (rs != null) {
                    rs.close();
                }
                String query = "SELECT * FROM " + tableName;
                rs = stmt.executeQuery(query);
                if (SCROLLABLE) {
                    model = new ScrollingResultSetTableModel(rs);
                } else {
                    model = new CachingResultSetTableModel(rs);
                }

                JTable table = new JTable(model);
                scrollPane = new JScrollPane(table);
                getContentPane().add(scrollPane, "Center");
                pack();
                doLayout();
            } catch (SQLException e) {
                System.out.println("Error " + e);
            }
        }
    }
    private JScrollPane scrollPane;
    private ResultSetTableModel model;
    private JComboBox tableNames;
    private JButton nextButton;
    private JButton previousButton;
    private ResultSet rs;
    private Connection con;
    private Statement stmt;
    private static boolean SCROLLABLE = false;
    // set to true if your database supports scrolling cursors
}
