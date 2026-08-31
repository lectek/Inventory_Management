package mysquare.core;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class Production {

    public static DefaultTableModel model;

    public static JTable getProductionView() throws Exception{
        model = new DefaultTableModel();
        JTable table = new JTable(model);
        model.addColumn("MANUFACTURED ON");
        model.addColumn("PRODUCT");
        model.addColumn("COLOUR");
        model.addColumn("WEIGHT");
        model.addColumn("QUANTITY");

        ResultSet data = null;
        try {
            data = Db.fetchData("prod_records");
            while(data.next()) {
                model.addRow(new Object[]{data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5)});
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        table.setGridColor(new Color(239,214,186));
        return table;
    }

    public static JPanel getProductionPanel() throws Exception{
        JPanel panel = new JPanel();
        Utility obj = new Utility();
        JComboBox<String> cb1 = new JComboBox<String>(obj.getProductList());
        JComboBox<String> cb2 = new JComboBox<String>(obj.getColourList());
        JComboBox<String> cb3 = new JComboBox<String>(obj.getWeightList());

        // Components Added using Flow Layout
        JLabel lab1 = new JLabel("Product");
        panel.add(lab1);
        panel.add(cb1);

        JLabel lab2 = new JLabel("Colour");
        panel.add(lab2);
        panel.add(cb2);

        JLabel lab3 = new JLabel("Weight");
        panel.add(lab3);
        panel.add(cb3);

        JLabel lab4 = new JLabel("Quantity");
        JTextField tf1 = new JTextField(5);
        panel.add(lab4);
        panel.add(tf1);

        JButton addBtn = new JButton("Add");
        panel.add(addBtn);

        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String product = cb1.getSelectedItem().toString();
                String colour = cb2.getSelectedItem().toString();
                String weight = cb3.getSelectedItem().toString();
                int qty;
                try {
                    qty = Integer.parseInt(tf1.getText());
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(IMStart.frame, "Enter a valid quantity.", "WARNING", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                addBtn.setEnabled(false);
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new SwingWorker<java.util.List<Object[]>, Void>() {
                    @Override
                    protected java.util.List<Object[]> doInBackground() throws Exception {
                        ResultSet dataNew = Db.addProduct(product, colour, weight, qty);
                        java.util.List<Object[]> rows = new java.util.ArrayList<>();
                        while (dataNew.next()) {
                            rows.add(new Object[]{dataNew.getString(1), dataNew.getString(2), dataNew.getString(3), dataNew.getString(4), dataNew.getString(5)});
                        }
                        return rows;
                    }

                    @Override
                    protected void done() {
                        panel.setCursor(Cursor.getDefaultCursor());
                        addBtn.setEnabled(true);
                        try {
                            model.setRowCount(0);
                            for (Object[] row : get()) {
                                model.addRow(row);
                            }
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        } catch (java.util.concurrent.ExecutionException e) {
                            JOptionPane.showMessageDialog(IMStart.frame, "Unable to add product.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        });
        panel.setBackground(new Color(239,176,137));
        return panel;
    }
}
