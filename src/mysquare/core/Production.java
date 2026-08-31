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
        model.addColumn("FABRICADO EM");
        model.addColumn("PRODUTO");
        model.addColumn("COR");
        model.addColumn("PESO");
        model.addColumn("QUANTIDADE");

        ResultSet data = null;
        try {
            data = Db.fetchData("prod_records");
            while(data.next()) {
                model.addRow(new Object[]{data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5)});
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        table.setGridColor(Theme.BORDER);
        table.setRowHeight(Theme.TABLE_ROW_HEIGHT);
        table.setFont(Theme.FONT_TABLE);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(Theme.TABLE_HEADER_BG);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        return table;
    }

    public static JPanel getProductionPanel() throws Exception{
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        Utility obj = new Utility();
        JComboBox<String> cb1 = new JComboBox<String>(obj.getProductList());
        JComboBox<String> cb2 = new JComboBox<String>(obj.getColourList());
        JComboBox<String> cb3 = new JComboBox<String>(obj.getWeightList());
        cb1.setEditable(true);
        cb2.setEditable(true);
        cb3.setEditable(true);

        JTextField tf1 = new JTextField(5);
        JTextField codeField = new JTextField(8);
        JTextField descriptionField = new JTextField(14);
        JTextField priceField = new JTextField(6);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 8));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.CENTER_ALIGNMENT);
        row1.add(Theme.labeledField("Produto (novo ou existente)", cb1));
        row1.add(Theme.labeledField("Cor (nova ou existente)", cb2));
        row1.add(Theme.labeledField("Peso (novo ou existente)", cb3));
        row1.add(Theme.labeledField("Quantidade", tf1));

        JButton addBtn = new JButton("Adicionar produto");
        addBtn.setFont(Theme.FONT_BUTTON);
        addBtn.setBackground(Theme.ACCENT);
        addBtn.setForeground(Color.WHITE);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 8));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.CENTER_ALIGNMENT);
        row2.add(Theme.labeledField("Código (opcional)", codeField));
        row2.add(Theme.labeledField("Descrição (opcional)", descriptionField));
        row2.add(Theme.labeledField("Preço (opcional)", priceField));
        row2.add(addBtn);

        column.add(row1);
        column.add(row2);
        panel.add(column);

        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String product = cb1.getEditor().getItem().toString().trim();
                String colour = cb2.getEditor().getItem().toString().trim();
                String weight = cb3.getEditor().getItem().toString().trim();
                String code = codeField.getText().trim();
                String description = descriptionField.getText().trim();
                String priceText = priceField.getText().trim();

                if (product.isEmpty() || colour.isEmpty() || weight.isEmpty()) {
                    JOptionPane.showMessageDialog(IMStart.frame, "Produto, cor e peso são obrigatórios.", "AVISO", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int qty;
                try {
                    qty = Integer.parseInt(tf1.getText().trim());
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(IMStart.frame, "Informe uma quantidade válida.", "AVISO", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                final boolean hasDetails = !code.isEmpty() || !description.isEmpty() || !priceText.isEmpty();
                final double price;
                if (hasDetails) {
                    double parsed;
                    try {
                        parsed = priceText.isEmpty() ? 0 : ModifyProducts.parsePrice(priceText);
                    } catch (Exception err) {
                        JOptionPane.showMessageDialog(IMStart.frame, "Preço inválido.", "AVISO", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    price = parsed;
                } else {
                    price = 0;
                }

                addBtn.setEnabled(false);
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new SwingWorker<java.util.List<Object[]>, Void>() {
                    @Override
                    protected java.util.List<Object[]> doInBackground() throws Exception {
                        Db.ensureListItem("product_list", "pname", product);
                        Db.ensureListItem("colour_list", "pclr", colour);
                        Db.ensureListItem("weight_list", "pwt", weight);
                        ResultSet dataNew = Db.addProduct(product, colour, weight, qty);
                        java.util.List<Object[]> rows = new java.util.ArrayList<>();
                        while (dataNew.next()) {
                            rows.add(new Object[]{dataNew.getString(1), dataNew.getString(2), dataNew.getString(3), dataNew.getString(4), dataNew.getString(5)});
                        }
                        if (hasDetails) {
                            // Leaves code/description/price untouched on restock unless the operator typed something.
                            Db.updateProduct(product, colour, weight, product, colour, weight, code, description, price);
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
                            if (!containsItem(cb1, product)) cb1.addItem(product);
                            if (!containsItem(cb2, colour)) cb2.addItem(colour);
                            if (!containsItem(cb3, weight)) cb3.addItem(weight);
                            tf1.setText("");
                            codeField.setText("");
                            descriptionField.setText("");
                            priceField.setText("");
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        } catch (java.util.concurrent.ExecutionException e) {
                            JOptionPane.showMessageDialog(IMStart.frame, "Não foi possível adicionar o produto.", "ERRO", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        });
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(Theme.sectionBorder("Adicionar produto"));
        return panel;
    }

    private static boolean containsItem(JComboBox<String> box, String value) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (value.equals(box.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }
}
