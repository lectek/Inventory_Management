package mysquare.core;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** A checkout screen: add items to a running sale, see the total, then confirm (deducts stock) or cancel. */
public class Sale {

    private static class CartLine {
        final String product;
        final String colour;
        final String weight;
        final int qty;
        final double unitPrice;

        CartLine(String product, String colour, String weight, int qty, double unitPrice) {
            this.product = product;
            this.colour = colour;
            this.weight = weight;
            this.qty = qty;
            this.unitPrice = unitPrice;
        }

        double subtotal() {
            return qty * unitPrice;
        }
    }

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public static JPanel getSalePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        List<CartLine> cart = new ArrayList<CartLine>();

        DefaultTableModel cartModel = new DefaultTableModel(new Object[]{"Produto", "Cor", "Peso", "Qtd", "Preço unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable cartTable = new JTable(cartModel);
        cartTable.setRowHeight(Theme.TABLE_ROW_HEIGHT);
        cartTable.setFont(Theme.FONT_TABLE);
        cartTable.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        cartTable.getTableHeader().setBackground(Theme.TABLE_HEADER_BG);
        cartTable.setSelectionBackground(Theme.ACCENT);
        cartTable.setSelectionForeground(Color.WHITE);
        cartTable.setGridColor(Theme.BORDER);

        Utility util = new Utility();
        JComboBox<String> productBox = new JComboBox<String>(util.getProductList());
        JComboBox<String> colourBox = new JComboBox<String>(util.getColourList());
        JComboBox<String> weightBox = new JComboBox<String>(util.getWeightList());
        JTextField qtyField = new JTextField(5);

        JLabel totalLabel = new JLabel("Total: 0,00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        totalLabel.setForeground(Theme.ACCENT_DARK);

        Runnable recalcTotal = () -> {
            double total = 0;
            for (CartLine line : cart) {
                total += line.subtotal();
            }
            totalLabel.setText(String.format(PT_BR, "Total: %.2f", total));
        };

        JButton addBtn = new JButton("Adicionar à venda");
        addBtn.setFont(Theme.FONT_BUTTON);
        JButton removeLineBtn = new JButton("Remover linha selecionada");
        JButton confirmBtn = new JButton("Confirmar venda");
        confirmBtn.setFont(Theme.FONT_BUTTON);
        confirmBtn.setBackground(Theme.ACCENT);
        confirmBtn.setForeground(Color.WHITE);
        JButton cancelBtn = new JButton("Cancelar venda");

        addBtn.addActionListener(e -> {
            String product = (String) productBox.getSelectedItem();
            String colour = (String) colourBox.getSelectedItem();
            String weight = (String) weightBox.getSelectedItem();
            if (product == null || colour == null || weight == null) {
                JOptionPane.showMessageDialog(IMStart.frame, "Selecione um produto, cor e peso.", "AVISO", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int qty;
            try {
                qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(IMStart.frame, "Informe uma quantidade válida.", "AVISO", JOptionPane.WARNING_MESSAGE);
                return;
            }

            addBtn.setEnabled(false);
            root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<Object[], Void>() {
                @Override
                protected Object[] doInBackground() throws Exception {
                    ResultSet rs = Db.fetchProduct(product, colour, weight);
                    if (!rs.next()) {
                        throw new Exception("Produto não encontrado.");
                    }
                    int stock = Integer.parseInt(rs.getString("pqt"));
                    String priceStr = rs.getString("pprice");
                    double price = priceStr == null || priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);
                    return new Object[]{stock, price};
                }

                @Override
                protected void done() {
                    root.setCursor(Cursor.getDefaultCursor());
                    addBtn.setEnabled(true);
                    try {
                        Object[] result = get();
                        int stock = (Integer) result[0];
                        double price = (Double) result[1];

                        int alreadyInCart = 0;
                        for (CartLine line : cart) {
                            if (line.product.equals(product) && line.colour.equals(colour) && line.weight.equals(weight)) {
                                alreadyInCart += line.qty;
                            }
                        }
                        if (alreadyInCart + qty > stock) {
                            JOptionPane.showMessageDialog(IMStart.frame,
                                    "Restam apenas " + (stock - alreadyInCart) + " em estoque.", "AVISO", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        CartLine line = new CartLine(product, colour, weight, qty, price);
                        cart.add(line);
                        cartModel.addRow(new Object[]{product, colour, weight, qty,
                                String.format(PT_BR, "%.2f", price), String.format(PT_BR, "%.2f", line.subtotal())});
                        recalcTotal.run();
                        qtyField.setText("");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(IMStart.frame, "Não foi possível adicionar o item.\n" + ex.getMessage(), "ERRO", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        removeLineBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                return;
            }
            cart.remove(row);
            cartModel.removeRow(row);
            recalcTotal.run();
        });

        cancelBtn.addActionListener(e -> {
            cart.clear();
            cartModel.setRowCount(0);
            recalcTotal.run();
        });

        confirmBtn.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(IMStart.frame, "Adicione pelo menos um item primeiro.", "AVISO", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(IMStart.frame,
                    "Confirmar venda de " + cart.size() + " item(ns) no valor total de " + totalLabel.getText().substring("Total: ".length()) + "?",
                    "Confirmar venda", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            confirmBtn.setEnabled(false);
            root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            List<CartLine> toSell = new ArrayList<CartLine>(cart);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (CartLine line : toSell) {
                        Db.sellProduct(line.product, line.colour, line.weight, line.qty);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    root.setCursor(Cursor.getDefaultCursor());
                    confirmBtn.setEnabled(true);
                    try {
                        get();
                        cart.clear();
                        cartModel.setRowCount(0);
                        recalcTotal.run();
                        JOptionPane.showMessageDialog(IMStart.frame, "Venda confirmada. Estoque atualizado.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(IMStart.frame, "Não foi possível confirmar a venda.\n" + ex.getMessage(), "ERRO", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 8));
        formRow.setOpaque(false);
        formRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        formRow.add(Theme.labeledField("Produto", productBox));
        formRow.add(Theme.labeledField("Cor", colourBox));
        formRow.add(Theme.labeledField("Peso", weightBox));
        formRow.add(Theme.labeledField("Quantidade", qtyField));
        formRow.add(addBtn);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 8));
        actionsRow.setOpaque(false);
        actionsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionsRow.add(removeLineBtn);
        actionsRow.add(totalLabel);
        actionsRow.add(confirmBtn);
        actionsRow.add(cancelBtn);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(Theme.SURFACE);
        bottom.setBorder(Theme.sectionBorder("Nova venda"));
        bottom.add(formRow);
        bottom.add(actionsRow);

        root.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        return root;
    }
}
