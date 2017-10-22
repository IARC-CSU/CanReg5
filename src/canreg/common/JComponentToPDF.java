package canreg.common;

import canreg.client.CanRegClientApp;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.DictionaryEntry;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ErvikM
 */
public class JComponentToPDF {

    private final static int inch = Toolkit.getDefaultToolkit().getScreenResolution();
    private final static float pixelToPoint = (float) 72 / (float) inch;

    public static void printComponentToPDF(Component component, String fileName) {
        try {
            Document d = new Document();
            PdfWriter writer = PdfWriter.getInstance(d, new FileOutputStream(fileName));
            d.open();

            float width = component.getWidth();
            float height = component.getHeight();

            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate template = cb.createTemplate(PageSize.A4.getHeight(), PageSize.A4.getWidth());
            PdfGraphics2D pg2d = new PdfGraphics2D(cb, width, height);
            AffineTransform at = new AffineTransform();
            at.translate(convertToPixels(20), convertToPixels(20));
            at.scale(pixelToPoint, pixelToPoint);
            pg2d.transform(at);
            component.paintAll(pg2d);
            pg2d.dispose();
            cb.addTemplate(template, 0, 0);
            d.close();
        } catch (DocumentException ex) {
            Logger.getLogger(JComponentToPDF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JComponentToPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void databaseRecordsToPDF(Set<DatabaseRecord> databaseRecords, String fileName, GlobalToolBox globalToolBox) {
        DatabaseVariablesListElement[] variables = globalToolBox.getVariables();
        Map<Integer, Dictionary> dictionary = CanRegClientApp.getApplication().getDictionary();
        Arrays.sort(variables, new DatabaseVariablesListElementPositionSorter());        
        int sourceNumber = 0;
        String previousTumour = "";
        try {
            String tableName = new String();
            LineSeparator line = new LineSeparator(1, 100, null, Element.ALIGN_CENTER, -2);
            Document d = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(d, new FileOutputStream(fileName));
            d.open();
            // PdfContentByte cb = writer.getDirectContent();
            // PdfTemplate template = cb.createTemplate(PageSize.A4.getHeight(), PageSize.A4.getWidth());
            for (DatabaseRecord databaseRecord : databaseRecords) {
                String header = "";
                LinkedList<DatabaseVariablesListElement> variablesInTable = new LinkedList<DatabaseVariablesListElement>();
                if (databaseRecord instanceof Patient) {
                    tableName = Globals.PATIENT_TABLE_NAME;
                    DatabaseVariablesListElement dvle = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString());
                    if (dvle != null) {
                         header += databaseRecord.getVariable(dvle.getDatabaseVariableName());
                    }
                } else if (databaseRecord instanceof Tumour) {
                    tableName = Globals.TUMOUR_TABLE_NAME;
                    DatabaseVariablesListElement dvle = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimSeq.toString());
                    if (dvle != null) {
                        header += databaseRecord.getVariable(dvle.getDatabaseVariableName());
                        previousTumour = databaseRecord.getVariableAsString(dvle.getDatabaseVariableName());
                    }
                    sourceNumber = 0;
                } else if (databaseRecord instanceof Source) {
                    tableName = Globals.SOURCE_TABLE_NAME;
                    header += (++sourceNumber);
                    if (previousTumour.length()>0){
                        header += "(Tumour: " + previousTumour + ")";
                    }
                }
                
                header = tableName + ": " + header;
                
                for (DatabaseVariablesListElement element : variables) {
                    if (element.getDatabaseTableName().equalsIgnoreCase(tableName)) {
                        variablesInTable.add(element);
                    }
                }

                Map<String, PdfPTable> groups = new TreeMap();
                Map<Integer, String> groupIDs = new TreeMap();

                for (DatabaseVariablesListElement element : variablesInTable) {
                    if (element.getGroupID() > 0) {
                        PdfPTable table = groups.get(element.getGroupName());
                        if (table == null) {
                            table = new PdfPTable(new float[]{1, 2});
                            table.setWidthPercentage(100);
                            groups.put(element.getGroupName(), table);
                            groupIDs.put(element.getGroupID(), element.getGroupName());
                        }
                        //<ictl.co>
                        //table.addCell(element.getFullName());
                        PdfPCell cell = new PdfPCell(new Phrase(element.getFullName(), JComponentToPDFHelper.getDefaultTTFont()));
                        if (!StringUtils.isAscii(element.getFullName())) {
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                        }
                        table.addCell(cell);
                        //</ictl.co>
                        table.addCell(element.getFullName());
                        Object value = databaseRecord.getVariable(element.getDatabaseVariableName());
                        if (value != null) {
                            String code = value.toString();
                            int dictionaryID = element.getDictionaryID();
                            if (dictionaryID >= 0 && code.trim().length() > 0) {
                                Dictionary dic = dictionary.get(dictionaryID);
                                String label = "Dictionary Error";
                                if (dic != null) {
                                    DictionaryEntry dictionaryEntry = dic.getDictionaryEntry(code);
                                    if (dictionaryEntry != null) {
                                        label = dictionaryEntry.getDescription();
                                    }
                                }
                                //<ictl.co>
//                                table.addCell(value.toString() + " (" + label + ")");
                                cell = new PdfPCell(new Phrase(value.toString() + " (" + label + ")", JComponentToPDFHelper.getDefaultTTFont()));
                                if (!StringUtils.isAscii(value.toString() + " (" + label + ")")) {
                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                    cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                                }
                                table.addCell(cell);
                                //</ictl.co>
                            } else {
                                //<ictl.co>
                                //table.addCell(value.toString());
                                if (LocalizationHelper.isRtlLanguageActive() && DateHelper.analyseContentForDateValue(value.toString())) {
                                    value = DateHelper.gregorianDateStringToLocaleDateString(value.toString(), Globals.DATE_FORMAT_STRING);
                                    cell = new PdfPCell(new Phrase(value.toString()));
                                } else {
                                    if (!StringUtils.isNumeric(value.toString())) {
                                        cell = new PdfPCell(new Phrase(value.toString(), JComponentToPDFHelper.getDefaultTTFont()));
                                        if (!StringUtils.isAscii(value.toString())) {
                                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                                        }
                                    } else {
                                        cell = new PdfPCell(new Phrase(value.toString()));
                                    }
                                }
                                table.addCell(cell);

                                //</ictl.co>
                            }
                        } else {
                            table.addCell(" ");
                        }
                    }
                }
                Set<Integer> set = new TreeSet<Integer>(groupIDs.keySet());
                
                d.add(new Paragraph(header));
                for (Integer groupID : set) {
                    String groupName = groupIDs.get(groupID);
                    Paragraph p = new Paragraph(groupName);
                    d.add(p);
                    p = new Paragraph();
                    p.add(groups.get(groupName));
                    d.add(p);
                }
                // Paragraph p = new Paragraph();                
                // p.add(line);
                // d.add(p);
            }
            // cb.addTemplate(template, 0, 0);
            d.close();
        } catch (DocumentException ex) {
            Logger.getLogger(JComponentToPDF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JComponentToPDF.class.getName()).log(Level.SEVERE, null, ex);
        //<ictl.co>
        } catch (IOException ex) {
            Logger.getLogger(JComponentToPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</ictl.co>
    }

    private static float convertToPixels(int points) {
        return (float) (points / pixelToPoint);

    }
}