package mysquare.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class ModifyProducts {

    public static JPanel getPanel(){
        JPanel panel = new JPanel();
        JTextField product = new JTextField(40);
        product.setToolTipText("Enter unique product");
        product.setBounds(15, 25, 250, 20);
        JTextField colour = new JTextField(40);
        colour.setToolTipText("Enter unique colour");
        colour.setBounds(15, 60, 250, 20);
        JTextField weight = new JTextField(40);
        weight.setToolTipText("Enter unique weight");
        weight.setBounds(15, 95, 250, 20);
        JButton addProduct = new JButton("Add Product");
        addProduct.setBounds(280, 25, 120, 20);
        JButton addColour = new JButton("Add Colour");
        addColour.setBounds(280, 60, 120, 20);
        JButton addWeight = new JButton("Add Weight");
        addWeight.setBounds(280, 95, 120, 20);
        JButton rmvProduct = new JButton("Remove Product");
        rmvProduct.setBounds(410, 25, 120, 20);
        JButton rmvColour = new JButton("Remove Colour");
        rmvColour.setBounds(410, 60, 120, 20);
        JButton rmvWeight = new JButton("Remove Weight");
        rmvWeight.setBounds(410, 95, 120, 20);
        JLabel editTitle = new JLabel("Edit Existing Product");
        editTitle.setBounds(15, 145, 250, 20);
        JComboBox<ProductOption> products = new JComboBox<ProductOption>(getProductKeys());
        products.setBounds(15, 175, 515, 20);
        JButton loadProduct = new JButton("Load");
        loadProduct.setBounds(545, 175, 90, 20);
        JLabel newProductLabel = new JLabel("Product");
        newProductLabel.setBounds(15, 215, 90, 20);
        JTextField newProduct = new JTextField(40);
        newProduct.setBounds(110, 215, 200, 20);
        JLabel newColourLabel = new JLabel("Colour");
        newColourLabel.setBounds(325, 215, 90, 20);
        JTextField newColour = new JTextField(40);
        newColour.setBounds(420, 215, 200, 20);
        JLabel newWeightLabel = new JLabel("Weight");
        newWeightLabel.setBounds(15, 250, 90, 20);
        JTextField newWeight = new JTextField(40);
        newWeight.setBounds(110, 250, 200, 20);
        JLabel codeLabel = new JLabel("Code");
        codeLabel.setBounds(325, 250, 90, 20);
        JTextField code = new JTextField(40);
        code.setBounds(420, 250, 200, 20);
        JLabel descriptionLabel = new JLabel("Description");
        descriptionLabel.setBounds(15, 285, 90, 20);
        JTextField description = new JTextField(80);
        description.setBounds(110, 285, 510, 20);
        JLabel priceLabel = new JLabel("Price");
        priceLabel.setBounds(15, 320, 90, 20);
        JTextField price = new JTextField(20);
        price.setBounds(110, 320, 200, 20);
        JButton saveProduct = new JButton("Save Changes");
        saveProduct.setBounds(325, 320, 140, 20);
        panel.setLayout(null);
        panel.add(product);
        panel.add(colour);
        panel.add(weight);
        panel.add(addProduct);
        panel.add(rmvProduct);
        panel.add(addColour);
        panel.add(rmvColour);
        panel.add(addWeight);
        panel.add(rmvWeight);
        panel.add(editTitle);
        panel.add(products);
        panel.add(loadProduct);
        panel.add(newProductLabel);
        panel.add(newProduct);
        panel.add(newColourLabel);
        panel.add(newColour);
        panel.add(newWeightLabel);
        panel.add(newWeight);
        panel.add(codeLabel);
        panel.add(code);
        panel.add(descriptionLabel);
        panel.add(description);
        panel.add(priceLabel);
        panel.add(price);
        panel.add(saveProduct);

        addProduct.addActionListener(e -> {
            try {
                Db.addItem("product_list", product.getText());
                product.setText("");
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Product already added.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });
        addColour.addActionListener(e -> {
            try {
                Db.addItem("colour_list", colour.getText());
                colour.setText("");
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Colour already added.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });
        addWeight.addActionListener(e -> {
            try {
                Db.addItem("weight_list", weight.getText());
                weight.setText("");
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Weight already added.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });
        rmvProduct.addActionListener(e -> {
            try {
                Db.removeItem("product_list","pname", product.getText());
                product.setText("");
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Product not found.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });
        rmvColour.addActionListener(e -> {
            try {
                Db.removeItem("colour_list", "pclr", colour.getText());
                colour.setText("");
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Colour not found.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });
        rmvWeight.addActionListener(e -> {
            try {
                Db.removeItem("weight_list", "pwt", weight.getText());
                weight.setText("");
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Weight not found.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });

        loadProduct.addActionListener(e -> {
            try {
                String[] key = parseProductKey(products.getSelectedItem());
                ResultSet data = Db.fetchProduct(key[0], key[1], key[2]);
                if (data.next()) {
                    newProduct.setText(data.getString("pname"));
                    newColour.setText(data.getString("pclr"));
                    newWeight.setText(data.getString("pwt"));
                    code.setText(nullToEmpty(data.getString("pcode")));
                    description.setText(nullToEmpty(data.getString("pdesc")));
                    String value = data.getString("pprice");
                    price.setText(value == null ? "0" : value);
                }
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Unable to load product.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });

        saveProduct.addActionListener(e -> {
            try {
                String[] key = parseProductKey(products.getSelectedItem());
                String updatedProduct = newProduct.getText().trim();
                String updatedColour = newColour.getText().trim();
                String updatedWeight = newWeight.getText().trim();
                if (updatedProduct.isEmpty() || updatedColour.isEmpty() || updatedWeight.isEmpty()) {
                    throw new Exception("Product, colour and weight are required.");
                }
                double updatedPrice = parsePrice(price.getText());
                Db.updateProduct(key[0], key[1], key[2], updatedProduct, updatedColour, updatedWeight,
                        code.getText().trim(), description.getText().trim(), updatedPrice);
                products.setModel(new DefaultComboBoxModel<ProductOption>(getProductKeys()));
                JOptionPane.showMessageDialog(IMStart.frame, "Product updated successfully.");
            } catch (Exception err) {
                JOptionPane.showConfirmDialog(IMStart.frame, "Unable to update product.\nERROR:"+err.getMessage(),"WARNING",JOptionPane.WARNING_MESSAGE );
            }
        });
        panel.setBackground(new Color(239,214,186));
        return panel;
    }

    private static ProductOption[] getProductKeys() {
        try {
            ResultSet data = Db.fetchProducts();
            java.util.ArrayList<ProductOption> keys = new java.util.ArrayList<ProductOption>();
            while (data.next()) {
                keys.add(new ProductOption(data.getString("pname"), data.getString("pclr"), data.getString("pwt")));
            }
            return keys.toArray(new ProductOption[keys.size()]);
        } catch (Exception e) {
            return new ProductOption[0];
        }
    }

    private static String[] parseProductKey(Object selectedItem) throws Exception {
        if (!(selectedItem instanceof ProductOption)) {
            throw new Exception("Select a product.");
        }
        ProductOption option = (ProductOption) selectedItem;
        return new String[]{option.product, option.colour, option.weight};
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static double parsePrice(String value) throws Exception {
        String cleanValue = value.trim().replace(",", ".");
        if (cleanValue.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid price.");
        }
    }

    private static class ProductOption {
        private String product;
        private String colour;
        private String weight;

        private ProductOption(String product, String colour, String weight) {
            this.product = product;
            this.colour = colour;
            this.weight = weight;
        }

        public String toString() {
            return product + " | " + colour + " | " + weight;
        }
    }
        /*if(btnCode.equals("Change Data Source")) {
            String str = JOptionPane.showInputDialog("Enter New Data Source:");
            Utility.setSource(str);
            System.out.print(str);
        }
        if(btnCode.equals("Home")) {
            IMStart.changePanel(IMStart.mp);
        }*/
}
