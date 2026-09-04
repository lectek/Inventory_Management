package mysquare.core;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Chat com os clientes que compram no site (SaaS). As mensagens vivem na
 * mesma rbp.db, na tabela chat_messages, criada pelo SaaS na primeira vez
 * que ele roda — não pelo IMS. Se o SaaS nunca rodou ainda, a lista de
 * conversas fica vazia (ver Db.fetchChatConversations).
 */
public class Chat {

    private static JList<String> conversationList;
    private static DefaultListModel<String> conversationModel;
    private static ArrayList<Db.ChatConversation> conversations = new ArrayList<Db.ChatConversation>();
    private static JTextArea threadArea;
    private static JTextField replyField;
    private static JButton sendBtn;
    private static JLabel selectedNameLabel;
    private static Long selectedCustomerId;

    public static JPanel getChatPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        conversationModel = new DefaultListModel<String>();
        conversationList = new JList<String>(conversationModel);
        conversationList.setFont(Theme.FONT_LABEL);
        conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conversationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                abrirConversaSelecionada();
            }
        });

        JScrollPane listScroll = new JScrollPane(conversationList);
        listScroll.setBorder(Theme.sectionBorder("Conversas"));
        listScroll.setPreferredSize(new Dimension(320, 500));

        JButton refreshBtn = new JButton("Atualizar conversas");
        refreshBtn.setFont(Theme.FONT_LABEL);
        refreshBtn.addActionListener(e -> carregarConversas());

        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setOpaque(false);
        leftPanel.add(listScroll, BorderLayout.CENTER);
        leftPanel.add(refreshBtn, BorderLayout.SOUTH);

        selectedNameLabel = new JLabel("Selecione uma conversa à esquerda");
        selectedNameLabel.setFont(Theme.FONT_TITLE);
        selectedNameLabel.setForeground(Theme.ACCENT_DARK);
        selectedNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));

        threadArea = new JTextArea();
        threadArea.setEditable(false);
        threadArea.setLineWrap(true);
        threadArea.setWrapStyleWord(true);
        threadArea.setFont(Theme.FONT_FIELD);
        threadArea.setBackground(Theme.SURFACE);
        JScrollPane threadScroll = new JScrollPane(threadArea);

        replyField = new JTextField();
        replyField.setFont(Theme.FONT_FIELD);
        replyField.setEnabled(false);
        replyField.addActionListener(e -> enviarResposta());

        sendBtn = new JButton("Enviar");
        sendBtn.setFont(Theme.FONT_BUTTON);
        sendBtn.setBackground(Theme.ACCENT);
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setEnabled(false);
        sendBtn.addActionListener(e -> enviarResposta());

        JPanel replyRow = new JPanel(new BorderLayout(8, 0));
        replyRow.setOpaque(false);
        replyRow.add(replyField, BorderLayout.CENTER);
        replyRow.add(sendBtn, BorderLayout.EAST);
        replyRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(Theme.sectionBorder("Conversa"));
        rightPanel.add(selectedNameLabel, BorderLayout.NORTH);
        rightPanel.add(threadScroll, BorderLayout.CENTER);
        rightPanel.add(replyRow, BorderLayout.SOUTH);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        carregarConversas();
        return root;
    }

    private static void carregarConversas() {
        new SwingWorker<ArrayList<Db.ChatConversation>, Void>() {
            @Override
            protected ArrayList<Db.ChatConversation> doInBackground() {
                return Db.fetchChatConversations();
            }

            @Override
            protected void done() {
                try {
                    conversations = get();
                } catch (Exception ex) {
                    conversations = new ArrayList<Db.ChatConversation>();
                }
                conversationModel.clear();
                if (conversations.isEmpty()) {
                    conversationModel.addElement("(nenhuma conversa ainda)");
                }
                for (Db.ChatConversation c : conversations) {
                    String rotulo = c.nome + (c.naoLidas > 0 ? "  (" + c.naoLidas + " nova" + (c.naoLidas > 1 ? "s" : "") + ")" : "");
                    conversationModel.addElement(rotulo);
                }
            }
        }.execute();
    }

    private static void abrirConversaSelecionada() {
        int index = conversationList.getSelectedIndex();
        if (index < 0 || index >= conversations.size()) {
            return;
        }
        Db.ChatConversation conversa = conversations.get(index);
        selectedCustomerId = conversa.customerId;
        selectedNameLabel.setText(conversa.nome + " (" + conversa.email + ")");
        replyField.setEnabled(true);
        sendBtn.setEnabled(true);
        threadArea.setText("Carregando...");

        new SwingWorker<ArrayList<Db.ChatMessage>, Void>() {
            @Override
            protected ArrayList<Db.ChatMessage> doInBackground() {
                Db.marcarChatComoLido(conversa.customerId);
                return Db.fetchChatMessages(conversa.customerId);
            }

            @Override
            protected void done() {
                try {
                    renderizarMensagens(get());
                } catch (Exception ex) {
                    threadArea.setText("Não foi possível carregar as mensagens.\n" + ex.getMessage());
                }
            }
        }.execute();
    }

    private static void renderizarMensagens(ArrayList<Db.ChatMessage> mensagens) {
        StringBuilder sb = new StringBuilder();
        for (Db.ChatMessage m : mensagens) {
            String remetente = "loja".equals(m.remetente) ? "Loja" : "Cliente";
            sb.append("[").append(m.criadoEm).append("] ").append(remetente).append(": ").append(m.mensagem).append("\n\n");
        }
        threadArea.setText(sb.toString());
        threadArea.setCaretPosition(threadArea.getDocument().getLength());
    }

    private static void enviarResposta() {
        if (selectedCustomerId == null) {
            return;
        }
        String mensagem = replyField.getText().trim();
        if (mensagem.isEmpty()) {
            return;
        }
        sendBtn.setEnabled(false);
        replyField.setEnabled(false);
        final long customerId = selectedCustomerId;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Db.enviarRespostaChat(customerId, mensagem);
                return null;
            }

            @Override
            protected void done() {
                sendBtn.setEnabled(true);
                replyField.setEnabled(true);
                try {
                    get();
                    replyField.setText("");
                    abrirConversaSelecionada();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(IMStart.frame,
                            "Não foi possível enviar a resposta.\nERRO:" + ex.getMessage(), "ERRO", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
