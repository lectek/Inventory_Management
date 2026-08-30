package mysquare.core;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;

public class Stock {

    public static JTable getStockView() throws Exception{
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        model.addColumn("PRODUCT");
        model.addColumn("COLOUR");
        model.addColumn("WEIGHT");
        model.addColumn("QUANTITY");
        model.addColumn("CODE");
        model.addColumn("DESCRIPTION");
        model.addColumn("PRICE");
        ResultSet data = null;
        try {
            data = Db.fetchProducts();
            while(data.next()) {
                model.addRow(new Object[]{data.getString("pname"), data.getString("pclr"), data.getString("pwt"),
                        data.getString("pqt"), data.getString("pcode"), data.getString("pdesc"), data.getString("pprice")});
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        table.setGridColor(new Color(239,214,186));
        return table;
    }
}


