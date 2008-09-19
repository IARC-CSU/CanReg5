package canreg.common;

import cachingtableapi.DistributedTableDataSource;
import cachingtableapi.DistributedTableDescription;
import java.util.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * Simulate a JTable with a million rows, but only MAX_PAGE_SIZE rows
 * are paged in at a time and it takes LATENCY_MILLIS to load them.
 * 
 * This simulation is pretty simple. It doesn't do common-sense things
 * like canceling scheduled loads when they aren't needed anymore.
 * 
 * Original found here: http://saloon.javaranch.com/cgi-bin/ubb/ultimatebb.cgi?ubb=get_topic&f=2&t=016435
 * 
 * @author Brian Cole
 */
public class PagingTableModel extends AbstractTableModel {

    private static final int MAX_PAGE_SIZE = 80;
   //private static final int LATENCY_MILLIS = 1500;
    private int dataOffset = 0;
    private ArrayList<Object[]> data = new ArrayList<Object[]>();
    private SortedSet<Segment> pending = new TreeSet<Segment>();
    private DistributedTableDataSource tableDataSource;
    private DistributedTableDescription tableDescription;

    public PagingTableModel(DistributedTableDataSource tableDataSource) throws Exception {
        this.tableDataSource = tableDataSource;
        this.tableDescription = tableDataSource.getTableDescription();
    }

    public int getColumnCount() {
        return tableDescription.getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < tableDescription.getColumnCount()) {
            return tableDescription.getColumnNames()[columnIndex];
        } else {
            return null;
        }
    }

    public int getRowCount() {
        return tableDescription.getRowCount();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex < tableDescription.getColumnCount()) {
            return tableDescription.getColumnClasses()[columnIndex];
        } else {
            return null;
        }
    }

    public Object getValueAt(int row, int col) {
        // check if row is in current page, schedule if not
        ArrayList<Object[]> page = data;
        int pageIndex = row - dataOffset;
        if (pageIndex < 0 || pageIndex >= page.size()) {
            // not loaded
            System.out.println(Runtime.getRuntime().freeMemory()+" free memory.");
            System.out.println("object at " + row + " isn't loaded yet");
            schedule(row);
            return "..";
        }
        Object rowObject = page.get(pageIndex)[col];
        // for this simulation just return the whole rowObject
        System.out.println(Runtime.getRuntime().freeMemory()+" free memory.");
        return rowObject;
    }

    private void schedule(int offset) {
        // schedule the loading of the neighborhood around offset (if not already scheduled)
        if (isPending(offset)) {
            // already scheduled -- do nothing
            return;
        }
        int startOffset = Math.max(0, offset - MAX_PAGE_SIZE / 2);
        int length = offset + MAX_PAGE_SIZE / 2 - startOffset;
        load(startOffset, length);
       
    }

    private boolean isPending(int offset) {
        int sz = pending.size();
        if (sz == 0) {
            return false;
        }
        if (sz == 1) {
            // special case (for speed)
            Segment seg = pending.first();
            return seg.contains(offset);
        }
        Segment lo = new Segment(offset - MAX_PAGE_SIZE, 0);
        Segment hi = new Segment(offset + 1, 0);
        // search pending segments that may contain offset
        for (Segment seg : pending.subSet(lo, hi)) {
            if (seg.contains(offset)) {
                return true;
            }
        }
        return false;
    }

    private void load(final int startOffset, final int length) {
        // simulate something slow like loading from a database
        final Segment seg = new Segment(startOffset, length);
        pending.add(seg);
        // set up code to run in another thread
        Runnable fetch = new Runnable() {

            public void run() {
                Object[][] dataObject;
                try {
                    dataObject = tableDataSource.retrieveRows(startOffset, startOffset+length);
                            
                } catch (Exception ex) {
                    System.out.println("error retrieving page at " + startOffset + ": aborting \n"+ex.getMessage());
                    pending.remove(seg);
                    return;
                }
                final ArrayList<Object[]> page = new ArrayList<Object[]>();
                for (int j = 0; j < dataObject.length; j += 1) {
                    page.add(dataObject[j]);
                }
                // done loading -- make available on the event dispatch thread
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        System.out.println("** loaded " + startOffset + " through " + (startOffset + length - 1));
                        setData(startOffset, page);
                        pending.remove(seg);
                    }
                });
            }
        };
        // run on another thread
        new Thread(fetch).start();
        
    }

    
    private void setData(int offset, ArrayList<Object[]> newData) {
        // This method must be called from the event dispatch thread.
        int lastRow = offset + newData.size() - 1;
        dataOffset = offset;
        data = newData;
        fireTableRowsUpdated(offset, lastRow);
    }

    /*
    public static void main(String[] argv) {
        JTable tab = new JTable(new PagingTableModel());
        JScrollPane sp = new JScrollPane(tab);
        //JScrollPane sp = LazyViewport.createLazyScrollPaneFor(tab);

        JFrame f = new JFrame("PagingTableModel");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(sp);
        f.setSize(200, 148);
        f.setVisible(true);
    }
     * /

     // ---------------- begin static nested class ----------------
    /**
     * This class is used to keep track of which rows have been scheduled for
     * loading, so that rows don't get scheduled twice concurrently. The idea
     * is to store Segments in a sorted data structure for fast searching.
     * 
     * The compareTo() method sorts first by base position, then by length.
     */
    static final class Segment implements Comparable<Segment> {

        private int base = 0,  length = 1;

        public Segment(int base, int length) {
            this.base = base;
            this.length = length;
        }

        public boolean contains(int pos) {
            return (base <= pos && pos < base + length);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Segment && base == ((Segment) o).base && length == ((Segment) o).length;
        }


        public int compareTo(Segment other) {
            //return negative/zero/positive as this object is less-than/equal-to/greater-than other
            int d = base - other.base;
            if (d != 0) {
                return d;
            }
            return length - other.length;
        }
    }
}
