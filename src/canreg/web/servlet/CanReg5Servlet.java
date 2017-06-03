package canreg.web.servlet;

import canreg.client.CanRegClientApp;
import canreg.client.DistributedTableDataSourceClient;
import canreg.client.LocalSettings;
import canreg.client.ServerDescription;
import canreg.common.DatabaseFilter;
import canreg.common.GlobalToolBox;
import canreg.common.cachingtableapi.DistributedTableDescription;
import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.common.cachingtableapi.DistributedTableModel;
import canreg.common.database.Patient;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.exceptions.WrongCanRegVersionException;
import canreg.server.database.RecordLockedException;
import canreg.server.database.UnknownTableException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.security.auth.login.LoginException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.table.TableModel;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.sql.SQLException;

@WebServlet(
        name = "CanReg",
        urlPatterns = {"/canReg"}
)
public class CanReg5Servlet extends HttpServlet {
    private final String PARAM_ACTION = "action";
    private final String PARAM_LOGIN_USER = "j_username";
    private final String PARAM_LOGIN_PASS = "j_password";
    private final String DEFAULT_ServerObjectString = "rmi://127.0.0.1:1199/CanRegLoginTRN";
    private final String PARAM_BROWS_REGNO = "RegNo";
    private final String PARAM_BROWS_FamN = "FamN";
    private final String PARAM_BROWS_FirstN = "FirstN";
    private final String PARAM_BROWS_BirthD = "BirthD";

    private <T> T getParameter(String name, T defaultValue, HttpServletRequest req) {
        T value = (T) req.getParameter(name);
        return (value != null ? value : defaultValue);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setCharacterEncoding("UTF-8");
            String response = "";
            String action = getParameter(PARAM_ACTION, "", req);
            if (action.startsWith("login")) {
                String user = getParameter(PARAM_LOGIN_USER, "", req);
                String pass = getParameter(PARAM_LOGIN_PASS, "", req);
                if ("".equals(user) || "".equals(pass)) {
                    response = "Invalid usernamse and/or password";
                } else {
                    CanRegClientApp app = CanRegClientApp.getApplication();
                    try {
                        LocalSettings localSettings = app.getLocalSettings();

                        String serverObjectString = DEFAULT_ServerObjectString;

                        String lastServerIDString = localSettings.getProperty(LocalSettings.LAST_SERVER_ID_KEY);

                        ServerDescription sd = localSettings.getServerDescription(Integer.parseInt(lastServerIDString));
                        serverObjectString = String.format("rmi://%s:%d/CanRegLogin%s", sd.getUrl(), sd.getPort(), sd.getCode());
                        app.loginRMI(serverObjectString, user, pass.toCharArray());
                        req.getSession().setAttribute(CanRegClientApp.class.getName(), app);

                        RequestDispatcher requestDispatcher = req.getRequestDispatcher("brows.html");
                        requestDispatcher.forward(req, resp);
                    } catch (LoginException e) {
                        response = "Invalid usernamse and/or password";
                    } catch (NotBoundException e) {
                        response = e.getMessage();
                    } catch (WrongCanRegVersionException e) {
                        response = e.getMessage();
                    }
                }
            } else if (action.startsWith("brows")) {
                CanRegClientApp app = (CanRegClientApp) req.getSession().getAttribute(CanRegClientApp.class.getName());
                if (app == null) {
                    response = "Please login first";
                } else {
                    DatabaseFilter filter = new DatabaseFilter();
                    String regNo = getParameter(PARAM_BROWS_REGNO, "", req);
                    if (!"".equals(regNo))
                        filter.setFilterString("RegNo LIKE '" + regNo + "%'");
                    String FamN = getParameter(PARAM_BROWS_FamN, "", req);
                    if (!"".equals(FamN))
                        filter.setFilterString(("".equals(filter.getFilterString())?"":" AND ")+"FamN LIKE '" + FamN + "%'");
                    String FirstN = getParameter(PARAM_BROWS_FirstN, "", req);
                    if (!"".equals(FirstN))
                        filter.setFilterString(filter.getFilterString() + ("".equals(filter.getFilterString())?"":" AND ")+"FirstN LIKE '" + FirstN + "%'");
                    String BirthD = getParameter(PARAM_BROWS_BirthD, "", req);
                    if (!"".equals(BirthD))
                        filter.setFilterString(filter.getFilterString() + ("".equals(filter.getFilterString())?"":" AND ")+"BirthD >= '" + BirthD + "'");

                    try {
                        DistributedTableDescription tableDatadescription = app.getDistributedTableDescription(filter, "Patient");
                        if (tableDatadescription != null) {
                            DistributedTableDataSourceClient tableDataSource = new DistributedTableDataSourceClient(tableDatadescription);
                            if (tableDataSource != null) {
                                TableModel tableDataModel = new DistributedTableModel(tableDataSource);
                                JSONArray array = new JSONArray();

                                for (int row = 0; row < tableDataModel.getRowCount(); ++row) {
                                    JSONObject object = new JSONObject();
                                    for (int col = 0; col < tableDataModel.getColumnCount(); ++col) {
                                        String colName = tableDataModel.getColumnName(col);
                                        Object value = tableDataModel.getValueAt(row, col);
                                        object.put(colName, value);
                                    }
                                    array.add(object);
                                }
                                response = array.toString();
                            }
                        }
                    } catch (SQLException e) {
                        response = e.getMessage();
                    } catch (UnknownTableException e) {
                        response = e.getMessage();
                    } catch (DistributedTableDescriptionException e) {
                        response = e.getMessage();
                    }
                }
            } else if (action.startsWith("view")) {
                CanRegClientApp app = (CanRegClientApp) req.getSession().getAttribute(CanRegClientApp.class.getName());
                if (app == null) {
                    response = "Please login first";
                } else {
                    String regNo = getParameter(PARAM_BROWS_REGNO, "", req);
                    if ("".equals(regNo)) {
                        response = "Please use search form";
                    } else {
                        try {
                            GlobalToolBox globalToolBox = app.getGlobalToolBox();
                            Patient[] patients = app.getPatientRecordsByID(regNo, false);
                            JSONArray array = new JSONArray();


//                            List<DatabaseVariablesListElement> variables = globalToolBox.getStandardVariables();

                            for (Patient patient : patients) {
                                JSONObject jPatient = new JSONObject();

/*                                for (DatabaseVariablesListElement listElement : variables) {
                                    if (listElement.getTable().equals("Patient")) {
                                        if (listElement.getGroupID() > -1) {
                                            if (listElement.getVariableType().equalsIgnoreCase(Globals.VARIABLE_TYPE_DICTIONARY_NAME)) {
                                                jPatient.put(listElement.getStandardVariableName(), patient.getVariable(listElement.getDatabaseVariableName()));
                                            } else {
                                                jPatient.put(listElement.getStandardVariableName(), patient.getVariable(listElement.getDatabaseVariableName()));
                                            }
                                        }
                                    }
                                }
*/

                                for (String varName : patient.getVariableNames()) {
                                    jPatient.put(varName, patient.getVariable(varName));
                                }

                                Tumour[] tumours = app.getTumourRecordsBasedOnPatientID(regNo, false);
                                for (Tumour tumour : tumours) {
                                    JSONObject jTumour = new JSONObject();
                                    for (String varName : tumour.getVariableNames()) {
                                        jTumour.put(varName, tumour.getVariable(varName));
                                    }
                                    for (Source source : tumour.getSources()) {
                                        JSONObject jSource = new JSONObject();
                                        for (String varName : source.getVariableNames()) {
                                            jSource.put(varName, source.getVariable(varName));
                                        }
                                        jTumour.put("sources", jSource);
                                    }
                                    jPatient.put("tumours", jTumour);
                                }
                                array.add(jPatient);
                            }
                            response = array.toString();
                        } catch (SQLException e) {
                            response = e.getMessage();
                        } catch (RecordLockedException e) {
                            response = e.getMessage();
                        } catch (UnknownTableException e) {
                            response = e.getMessage();
                        } catch (DistributedTableDescriptionException e) {
                            response = e.getMessage();
                        }
                    }
                }
            }
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json");
            ServletOutputStream out = resp.getOutputStream();
            out.write(response.getBytes("UTF-8"));
            out.flush();
            out.close();
        } catch (IllegalArgumentException ex) {
            ServletOutputStream out = resp.getOutputStream();
            out.write("Please use standard web ui".getBytes());
            out.flush();
            out.close();
        }
    }
}