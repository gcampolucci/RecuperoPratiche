import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVReader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecuperoPratiche extends javax.swing.JFrame{
    private JButton caricaFileCSVButton;
    private JButton cancellaTabellaButton;
    private JButton rimuoviPraticheButton;
    private JTable csvTable;
    private JTextField txtOutput;
    private JButton selezionaButton;
    private JButton creaListaQueryButton;
    private JButton creaFileJSONButton;
    private JButton chiudiButton;
    private JLabel lblOutput;
    private JToolBar supBar;
    private JPanel rootPanel;

    private static String PATH = "";
    private static final List<String> queryUpdate = new ArrayList<>();
    public static final String lista_query = "ListaQuery.csv";
    public static final String queryUpdate1 = "UPDATE FD.FDT_004_PROC_FD SET CD_STATO_PROC_FD = 'ARCHIVED' WHERE PR_ID_PROC_FD = ";
    public static final String queryUpdate2 = " AND CD_PRATICA = ";
    public static final String queryUpdate3 = " AND CD_CORRELATION_KEY_BPM = '";

    private static List<String> JSON = new ArrayList<>();
    public static final String json_file = "JSON_Postman.json";
    public static String JSON1 = "{\"datiSessione\": {\"processo\": \"NPI\",\"caCoobbligato\": null,\"caCliente\": \"ICR_ENV\",\"codicePratica\": ";
    public static String JSON2 = " ,\"societa\": null,\"idSessione\": \"";
    public static final String JSON3 = "\"},";

    public RecuperoPratiche() {

        rootPanel.setSize(new Dimension(900,400));

        caricaFileCSVButton.addActionListener(e -> {
            JFileChooser filechooser = new JFileChooser();
            FileFilter csvFilter = new FileTypeFilter(".csv", "CSV File");
            FileFilter txtFilter = new FileTypeFilter(".txt", "Text File");
            filechooser.addChoosableFileFilter(csvFilter);
            filechooser.addChoosableFileFilter(txtFilter);
            filechooser.setFileFilter(csvFilter);
            File f = null;

            if (filechooser.showOpenDialog(rootPanel) == JFileChooser.APPROVE_OPTION) {
                f = filechooser.getSelectedFile();
                String filepath = f.getPath();
                ApriCSV(filepath);
            }
        });

        chiudiButton.addActionListener(e -> {
            // Close the application
            System.exit(0);
        });

        cancellaTabellaButton.addActionListener(e -> {
            DefaultTableModel nullModel = new DefaultTableModel();
            csvTable.setModel(nullModel);
        });

        rimuoviPraticheButton.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) csvTable.getModel();
            int[] rows = csvTable.getSelectedRows();
            for(int i=0;i<rows.length;i++){
                model.removeRow(rows[i]-i);
            }
        });

        selezionaButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(rootPanel);
            if(option == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                txtOutput.setText(file.getPath());
                PATH = file.getPath();
            }
        });

        creaListaQueryButton.addActionListener(e -> {
            try{
                DefaultTableModel model = (DefaultTableModel) csvTable.getModel();
                for (int i = 0; i < model.getRowCount(); i++){
                    queryUpdate.add(queryUpdate1 + model.getValueAt(i, 1) + queryUpdate2 + model.getValueAt(i, 0) + queryUpdate3 + model.getValueAt(i, 2) + "';");
                    queryUpdate.add("\n");
                }
                scritturaFile(queryUpdate, lista_query);
            }catch(IOException ex){
                System.out.println("Nessun dato nella tabella");
            }
        });

        creaFileJSONButton.addActionListener(e -> {
            try{
                DefaultTableModel model = (DefaultTableModel) csvTable.getModel();
                JSON = new ArrayList();
                JSON.add("[");
                for (int i = 0; i < model.getRowCount(); i++){

                    JSON1 = "\n{\n \"codicePratica\": " + model.getValueAt(i, 0) + ",\n";
                    if (i == (model.getRowCount()-1)){
                        JSON2 = " \"KeyBPM\": " + "\"" + model.getValueAt(i, 2) + "\"" +"\n}";
                    }else{
                        JSON2 = " \"KeyBPM\": " +  "\"" + model.getValueAt(i, 2) +  "\"" +"\n},";
                    }
                    JSON.add(JSON1);
                    JSON.add(JSON2);
                }
                JSON.add("\n]");
                scritturaFile(JSON, json_file);
            }catch(IOException ex){
                System.out.println("Nessun dato nella tabella");
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Recupero Pratiche v2.0");
        frame.setContentPane(new RecuperoPratiche().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                System.out.println(info.getName());
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RecuperoPratiche.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        frame.setVisible(true);

    }

    public void ApriCSV(String csvFile) {
        Object[] columnnames;
        CSVReader CSVFileReader;
        try {
            CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(csvParser).build();
            List<String[]> myEntries = reader.readAll();
            columnnames = (String[]) myEntries.get(0);
            DefaultTableModel tableModel = new DefaultTableModel(columnnames, myEntries.size()-1){
                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells false
                    return false;
                }
            };
            int rowcount = tableModel.getRowCount();
            for (int x = 0; x<rowcount+1; x++)
            {
                int colNumber = 0;
                // if x = 0 this is the first row...skip it... data used for columnnames
                if (x>0)
                {
                    for (String thiscellvalue : (String[])myEntries.get(x))
                    {
                        tableModel.setValueAt(thiscellvalue, x-1, colNumber);
                        colNumber++;
                    }
                }
            }
            this.csvTable.setModel(tableModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void scritturaFile (List<String> queryUpdate, String file) throws IOException {
        FileWriter fileWriter = new FileWriter(PATH + "//" + file);
        try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            Iterator<String> iterator = queryUpdate.iterator();
            while (iterator.hasNext())
            {
                String str = iterator.next();
                bufferedWriter.write(str);
                bufferedWriter.flush();
            }
            JOptionPane.showMessageDialog(this, "Scrittura completata!");
        }
    }

}
