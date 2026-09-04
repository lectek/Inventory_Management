package mysquare.core;

import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Cria/atualiza o login administrativo do site (SaaS) — de propósito, o único
 * lugar que faz isso é aqui, no IMS. O SaaS nunca se auto-cadastra como admin,
 * então só quem tem acesso a este programa (o dono da loja) consegue criar ou
 * trocar esse acesso.
 */
public class AdminSaas {

    private static JList<String> adminList;
    private static DefaultListModel<String> adminListModel;
    private static JTextField nomeField;
    private static JTextField emailField;
    private static JPasswordField senhaField;
    private static JPasswordField confirmarField;
    private static JLabel statusLabel;

    public static JPanel getPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        adminListModel = new DefaultListModel<String>();
        adminList = new JList<String>(adminListModel);
        adminList.setFont(Theme.FONT_LABEL);
        JScrollPane listScroll = new JScrollPane(adminList);
        listScroll.setBorder(Theme.sectionBorder("Acessos administrativos atuais"));
        listScroll.setPreferredSize(new Dimension(320, 300));

        JButton refreshBtn = new JButton("Atualizar lista");
        refreshBtn.setFont(Theme.FONT_LABEL);
        refreshBtn.addActionListener(e -> carregarAdmins());

        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setOpaque(false);
        leftPanel.add(listScroll, BorderLayout.CENTER);
        leftPanel.add(refreshBtn, BorderLayout.SOUTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(Theme.sectionBorder("Criar / atualizar acesso"));

        nomeField = new JTextField();
        emailField = new JTextField();
        senhaField = new JPasswordField();
        confirmarField = new JPasswordField();

        form.add(campo("Nome", nomeField));
        form.add(Box.createVerticalStrut(10));
        form.add(campo("E-mail (login)", emailField));
        form.add(Box.createVerticalStrut(10));
        form.add(campo("Senha", senhaField));
        form.add(Box.createVerticalStrut(10));
        form.add(campo("Confirmar senha", confirmarField));
        form.add(Box.createVerticalStrut(16));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.FONT_LABEL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton salvarBtn = new JButton("Salvar acesso administrativo");
        salvarBtn.setFont(Theme.FONT_BUTTON);
        salvarBtn.setBackground(Theme.ACCENT);
        salvarBtn.setForeground(Color.WHITE);
        salvarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        salvarBtn.addActionListener(e -> salvar(salvarBtn));

        JLabel aviso = new JLabel("<html><body style='width:280px'>Se o e-mail já existir, a senha e o nome "
                + "são atualizados. Se não existir, um novo acesso é criado.</body></html>");
        aviso.setFont(Theme.FONT_LABEL);
        aviso.setForeground(Theme.TEXT_MUTED);
        aviso.setAlignmentX(Component.LEFT_ALIGNMENT);
        aviso.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        form.add(aviso);
        form.add(salvarBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(statusLabel);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(new JScrollPane(form), BorderLayout.CENTER);

        carregarAdmins();
        return root;
    }

    private static JPanel campo(String rotulo, JTextField field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(360, 70));

        JLabel label = new JLabel(rotulo);
        label.setFont(Theme.FONT_LABEL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(Theme.FONT_FIELD);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(360, 36));

        p.add(label);
        p.add(field);
        return p;
    }

    private static void carregarAdmins() {
        new SwingWorker<ArrayList<String>, Void>() {
            @Override
            protected ArrayList<String> doInBackground() {
                return Db.fetchAdminEmails();
            }

            @Override
            protected void done() {
                ArrayList<String> emails;
                try {
                    emails = get();
                } catch (Exception ex) {
                    emails = new ArrayList<String>();
                }
                adminListModel.clear();
                if (emails.isEmpty()) {
                    adminListModel.addElement("(nenhum acesso criado ainda)");
                }
                for (String email : emails) {
                    adminListModel.addElement(email);
                }
            }
        }.execute();
    }

    private static void salvar(JButton salvarBtn) {
        String nome = nomeField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        char[] senha = senhaField.getPassword();
        char[] confirmar = confirmarField.getPassword();

        if (nome.isEmpty() || email.isEmpty() || senha.length == 0) {
            statusLabel.setForeground(Theme.ACCENT);
            statusLabel.setText("Preencha nome, e-mail e senha.");
            return;
        }
        if (senha.length < 6) {
            statusLabel.setForeground(Theme.ACCENT);
            statusLabel.setText("A senha precisa ter pelo menos 6 caracteres.");
            return;
        }
        if (!new String(senha).equals(new String(confirmar))) {
            statusLabel.setForeground(Theme.ACCENT);
            statusLabel.setText("As senhas não coincidem.");
            return;
        }

        String senhaHash = BCrypt.hashpw(new String(senha), BCrypt.gensalt());
        java.util.Arrays.fill(senha, '\0');
        java.util.Arrays.fill(confirmar, '\0');

        salvarBtn.setEnabled(false);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setText("Salvando...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Db.salvarAdminSaas(nome, email, senhaHash);
                return null;
            }

            @Override
            protected void done() {
                salvarBtn.setEnabled(true);
                try {
                    get();
                    statusLabel.setForeground(Theme.ACCENT_DARK);
                    statusLabel.setText("Acesso salvo para " + email + ".");
                    senhaField.setText("");
                    confirmarField.setText("");
                    carregarAdmins();
                } catch (Exception ex) {
                    statusLabel.setForeground(Theme.ACCENT);
                    statusLabel.setText("Não foi possível salvar. O site (SaaS) já rodou nesta máquina alguma vez?");
                    JOptionPane.showMessageDialog(IMStart.frame,
                            "Não foi possível salvar o acesso administrativo.\nERRO:" + ex.getMessage(),
                            "ERRO", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
