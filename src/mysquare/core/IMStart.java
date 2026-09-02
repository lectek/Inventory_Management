package mysquare.core;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class IMStart {

	public static JFrame frame = new JFrame();
	public static JMenuBar mb = new JMenuBar();
	public static JMenu m1,m2,m3,m4;
	public static JMenuItem m1i1, m2i1, m2i2, m2i3, m2i4, m3i1,m4i1;
	public static JScrollPane jScrollPane;

	private static final String AJUDA_VENDA = "Escolha o produto, a cor, o peso e a quantidade e clique em \"Adicionar à venda\". "
			+ "Repita para cada item que o cliente está levando — o total é somado automaticamente. "
			+ "Quando terminar, clique em \"Confirmar venda\" para dar baixa no estoque, "
			+ "ou \"Cancelar venda\" para descartar sem alterar o estoque.";
	private static final String AJUDA_PRODUCAO = "Registre aqui um novo lote de produção: informe produto, cor e peso "
			+ "(pode digitar um valor novo, não precisa já existir), a quantidade fabricada e, se quiser, "
			+ "código, descrição e preço. Clique em \"Adicionar produto\" para registrar. "
			+ "A tabela acima mostra o histórico de tudo que já foi produzido.";
	private static final String AJUDA_DESPACHO = "Use esta tela para dar baixa manual no estoque (por exemplo, perda, "
			+ "doação ou transferência) sem passar pela tela de Venda. Escolha o produto existente, a quantidade "
			+ "e clique em \"Despachar\".";
	private static final String AJUDA_ESTOQUE = "Esta tela mostra a quantidade atual de cada produto em estoque. "
			+ "Para alterar o estoque, use as telas Venda, Produção (entrada) ou Despacho (saída), "
			+ "ou edite diretamente em Modificar produtos.";
	private static final String AJUDA_VENDAS_POR_DIA = "Resumo de tudo que foi vendido ou despachado, agrupado por dia: "
			+ "número de vendas, total de itens e valor total. O valor só é somado para vendas feitas pela tela "
			+ "Venda (que registra o preço); despachos manuais sem preço entram na contagem de itens mas não no total em R$.";
	private static final String AJUDA_MODIFICAR = "Selecione um produto existente na lista para carregar seus dados. "
			+ "Altere o que precisar (nome, cor, peso, quantidade, código, preço ou descrição) e clique em \"Salvar alterações\". "
			+ "Para remover um produto do catálogo, selecione-o e clique em \"Excluir produto\" — "
			+ "isso não apaga o histórico de produção/despacho dele.";

	IMStart(){
		m1 = new JMenu("Venda");
		m1.setMnemonic(KeyEvent.VK_V);
		m2 = new JMenu("Exibir");
		m2.setMnemonic(KeyEvent.VK_E);
		m3 = new JMenu("Operações");
		m3.setMnemonic(KeyEvent.VK_O);
		m4 = new JMenu("Ajuda");
		m4.setMnemonic(KeyEvent.VK_A);
		m1i1 = new JMenuItem("Nova venda");
		m2i1 = new JMenuItem("Produção");
		m2i2 = new JMenuItem("Despacho");
		m2i3 = new JMenuItem("Estoque");
		m2i4 = new JMenuItem("Vendas por dia");
		m3i1 = new JMenuItem("Modificar produtos");
		m4i1 = new JMenuItem("Sobre o software");
		m1.add(m1i1);
		m2.add(m2i1);
		m2.add(m2i2);
		m2.add(m2i3);
		m2.add(m2i4);
		m3.add(m3i1);
		m4.add(m4i1);
		mb.add(m1);
		mb.add(m2);
		mb.add(m3);
		mb.add(m4);
		m1i1.setBackground(Theme.TABLE_HEADER_BG);
		m2i1.setBackground(Theme.TABLE_HEADER_BG);
        m2i2.setBackground(Theme.TABLE_HEADER_BG);
        m2i3.setBackground(Theme.TABLE_HEADER_BG);
        m2i4.setBackground(Theme.TABLE_HEADER_BG);
        m3i1.setBackground(Theme.TABLE_HEADER_BG);
        m4i1.setBackground(Theme.TABLE_HEADER_BG);
        mb.setBackground(Theme.SURFACE);
	}

	/** Nests an existing screen's content above the shared "Como usar" / Instagram footer. */
	private static JPanel withFooter(Component content, String helpTitle, String helpBody) {
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.add(content, BorderLayout.CENTER);
		wrap.add(Theme.footer(helpTitle, helpBody), BorderLayout.SOUTH);
		return wrap;
	}

	public static void main(String[] args) {
        Theme.applyLookAndFeel();
        SwingUtilities.updateComponentTreeUI(frame);
        SwingUtilities.updateComponentTreeUI(mb);

        final String dev_msg = "Sistema de Gerenciamento de Estoque (IMS) v1.0.5"
				+ "\n\nSoftware legado originalmente criado por Yash Modi."
				+ "\nAplicação atual continuada e desenvolvida por lectek."
				+ "\n\nAcesse https://github.com/lectek/Inventory_Management";
		frame.setTitle("Raj Blow Plast");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1400, 800);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Theme.BACKGROUND);
        frame.setJMenuBar(mb);

        try {
            new IMStart();
            jScrollPane = new JScrollPane(Stock.getStockView());
            frame.getContentPane().add(BorderLayout.CENTER, jScrollPane);
            frame.getContentPane().add(BorderLayout.SOUTH, Theme.footer("Estoque", AJUDA_ESTOQUE));
            frame.setVisible(true);

            m1i1.addActionListener(e -> {
                try {
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(BorderLayout.CENTER, Sale.getSalePanel());
                    frame.getContentPane().add(BorderLayout.SOUTH, Theme.footer("Venda", AJUDA_VENDA));
                    frame.getContentPane().doLayout();
                    frame.update(frame.getGraphics());
                    frame.setVisible(true);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame,"Não foi possível abrir a tela de venda.\nERRO:"+ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
                }
            });

            m2i1.addActionListener(e -> {
                try {
                    frame.getContentPane().removeAll();
                    jScrollPane = new JScrollPane(Production.getProductionView());
                    frame.getContentPane().add(BorderLayout.CENTER, jScrollPane);
                    frame.getContentPane().add(BorderLayout.SOUTH, withFooter(Production.getProductionPanel(), "Produção", AJUDA_PRODUCAO));
                    frame.getContentPane().doLayout();
                    frame.update(frame.getGraphics());
                    frame.setVisible(true);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame,"Não foi possível obter os produtos fabricados.\nERRO:"+ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
                }
            });

            m2i2.addActionListener(e -> {
                try {
                    frame.getContentPane().removeAll();
                    jScrollPane = new JScrollPane(Dispatch.getDispatchView());
                    frame.getContentPane().add(BorderLayout.CENTER, jScrollPane);
                    frame.getContentPane().add(BorderLayout.SOUTH, withFooter(Dispatch.getDispatchPanel(), "Despacho", AJUDA_DESPACHO));
                    frame.getContentPane().doLayout();
                    frame.update(frame.getGraphics());
                    frame.setVisible(true);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame,"Não foi possível obter os produtos despachados.\nERRO:"+ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
                }
            });

            m2i3.addActionListener(e -> {
                try {
                    frame.getContentPane().removeAll();
                    jScrollPane = new JScrollPane(Stock.getStockView());
                    frame.getContentPane().add(BorderLayout.CENTER, jScrollPane);
                    frame.getContentPane().add(BorderLayout.SOUTH, Theme.footer("Estoque", AJUDA_ESTOQUE));
                    frame.getContentPane().doLayout();
                    frame.update(frame.getGraphics());
                    frame.setVisible(true);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame,"Não foi possível obter o estoque.\nERRO:"+ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
                }
            });

            m2i4.addActionListener(e -> {
                try {
                    frame.getContentPane().removeAll();
                    jScrollPane = new JScrollPane(SalesReport.getSalesReportView());
                    frame.getContentPane().add(BorderLayout.CENTER, jScrollPane);
                    frame.getContentPane().add(BorderLayout.SOUTH, Theme.footer("Vendas por dia", AJUDA_VENDAS_POR_DIA));
                    frame.getContentPane().doLayout();
                    frame.update(frame.getGraphics());
                    frame.setVisible(true);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame,"Não foi possível obter o relatório de vendas.\nERRO:"+ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
                }
            });

            m3i1.addActionListener(e -> {
                try {
                    frame.getContentPane().removeAll();
                    jScrollPane = new JScrollPane(ModifyProducts.getPanel());
                    frame.getContentPane().add(BorderLayout.CENTER, jScrollPane);
                    frame.getContentPane().add(BorderLayout.SOUTH, Theme.footer("Modificar produtos", AJUDA_MODIFICAR));
                    frame.getContentPane().doLayout();
                    frame.update(frame.getGraphics());
                    frame.setVisible(true);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame,"Não foi possível obter o catálogo de produtos.\nERRO:"+ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
                }
            });

            m4i1.addActionListener(e -> {
                JOptionPane.showMessageDialog(frame, dev_msg, "Sobre o software", JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Desculpe! Algo deu errado.\nERRO:" + e.getMessage(), "ERRO", JOptionPane.ERROR_MESSAGE);
        }
    }
}
