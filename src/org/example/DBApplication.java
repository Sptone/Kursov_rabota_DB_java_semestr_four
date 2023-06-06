package org.example;
import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DBApplication extends JFrame{
    private String url = "jdbc:postgresql://localhost:5433/postgres";
    private String user = "postgres";
    private String password = "spt";

    JTabbedPane tabbedPane = new JTabbedPane();

    public DBApplication(){

        setTitle("DataBase raspredelenie dop raboti");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.CYAN);

        JMenuBar menuBar = new JMenuBar();

        //2 кнопки на начальном экране

        JMenuItem operationItem = new JMenuItem("Операции");
        JMenuItem procedureItem = new JMenuItem("Обновление сотрудника");

        menuBar.add(operationItem);
        menuBar.add(procedureItem);

        setJMenuBar(menuBar);

        //таблицы на начальном окне
        JPanel panel1 = new JPanel();
        tabbedPane.addTab("Сотрудника", panel1);
        addTableToPanel(panel1, "SELECT * FROM sotrudnik");

        JPanel panel2 = new JPanel();
        tabbedPane.addTab("Работа", panel2);
        addTableToPanel(panel2, "SELECT * FROM rabota");

        JPanel panel3 = new JPanel();
        tabbedPane.addTab("Вид работы", panel3);
        addTableToPanel(panel3, "SELECT * FROM vid_raboti");

        JPanel panel4 = new JPanel();
        tabbedPane.addTab("Преподаватели", panel4);
        addTableToPanel(panel4, "SELECT * FROM prepodavatel");

        JPanel panel5 = new JPanel();
        tabbedPane.addTab("Вспомогательный персонал", panel5);
        addTableToPanel(panel5, "SELECT * FROM vspomog_personal");

        getContentPane().add(tabbedPane);

        setVisible(true);

        operationItem.addActionListener(e -> choiceTable());
        procedureItem.addActionListener(e -> {
            procedureUpd();
            refreshTable();
        });
    }


    private void addTableToPanel(JPanel panel, String query){
        try {
            //подключаемся к БД выводит таблицу
            DBConnection dbConnection = new DBConnection(url, user, password);
            Connection connection = dbConnection.getConnection();
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            //Создаем модель таблицы
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }
            Object[][] data = new Object[100][columnCount];
            int row = 0;
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    data[row][i - 1] = resultSet.getObject(i);
                }
                row++;
            }
            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            //Выводим таьблицу на панель
            panel.add(scrollPane);
            revalidate();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void choiceTable(){
        JButton sotrudnikButton = new JButton("Сотрудинки");
        sotrudnikButton.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            tableSotrudnik();
        });

        JButton jobButton = new JButton("Работы");
        jobButton.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            tableJob();
        });

        JButton VidJobButton = new JButton("Виды работы");
        VidJobButton.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            tableVidJob();
        });

        JButton PrepodButton = new JButton("Преподаватели");
        PrepodButton.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            tablePrepod();
        });

        JButton VspomogPersonalButton = new JButton("Вспомогательный персонал");
        VspomogPersonalButton.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            tableVspomogPersonal();
        });

        JComponent[] inputs = new JComponent[] {
                new JLabel("Выберите таблицу"),
                sotrudnikButton,
                jobButton,
                VidJobButton,
                PrepodButton,
                VspomogPersonalButton
        };

        JOptionPane.showMessageDialog(null, inputs, "Выбор таблицы", JOptionPane.WARNING_MESSAGE);

    }

    //Данные таблицы "уникальные ключи таблиц"
    private void tableSotrudnik(){
        String tableName = "sotrudnik";
        String primaryKeyColumnName = "id_sotrudnika";
        operations(tableName, primaryKeyColumnName);
        refreshTable();
    }
    private void tableJob(){
        String tableName = "rabota";
        String primaryKeyColumnName = "id_raboti";
        operations(tableName, primaryKeyColumnName);
        refreshTable();
    }
    private void tableVidJob(){
        String tableName = "vid_raboti";
        String primaryKeyColumnName = "id_vida";
        operations(tableName, primaryKeyColumnName);
        refreshTable();
    }
    private void tablePrepod(){
        String tableName = "prepodavatel";
        String primaryKeyColumnName = "id_sotrudnika";
        operations(tableName, primaryKeyColumnName);
        refreshTable();
    }
    private void tableVspomogPersonal(){
        String tableName = "vspomog_personal";
        String primaryKeyColumnName = "id_sotrudnika";
        operations(tableName, primaryKeyColumnName);
        refreshTable();
    }

    //Меню операций
    private void operations(String tableName, String primaryKeyColumnName){
        JButton insert = new JButton("Вставка");
        insert.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            insertRecord(tableName);
        });


        JButton update = new JButton("Обновить");
        update.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            updateRecord(tableName, primaryKeyColumnName);
        });

        JButton delete = new JButton("Удалить");
        delete.addActionListener(e -> {
            ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
            deleteRecord(tableName);
        });

        JButton search = new JButton("Поиск по ID");
        search.addActionListener(e -> {
            try {
                searchRecord(tableName, primaryKeyColumnName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        JComponent[] inputs = new JComponent[] {
                new JLabel("Выберите операцию"),
                insert,
                update,
                delete,
                search
        };

        JOptionPane.showMessageDialog(null, inputs, "Выбор операций для " + tableName, JOptionPane.WARNING_MESSAGE);
    }

    //Операции
    private void insertRecord(String tableName) {
        try {
            DBConnection dbConnection = new DBConnection(url, user, password);
            Connection connection = dbConnection.getConnection();
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " LIMIT 0");

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Object> values = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String inputValue = JOptionPane.showInputDialog("Введите значение для столбца " + columnName);
                Object value = null;
                // Перевод введенного значения для ячейки таблицы
                // (типо если для даты вводить текст то будет ошибка)
                switch (metaData.getColumnType(i)) {
                    case Types.BIGINT -> value = Long.parseLong(inputValue);
                    case Types.DATE -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date parsedDate = dateFormat.parse(inputValue);
                        Date sqlDate = new Date(parsedDate.getTime());
                        value = sqlDate;
                    }
                    case Types.NUMERIC, Types.DOUBLE -> value = Double.parseDouble(inputValue);
                    case Types.VARCHAR -> value = inputValue;
                    default -> {
                        JOptionPane.showMessageDialog(this, "Неизвестный тип данных для столбца " + columnName, "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return; // Выход если не верно ведено
                    }
                }
                values.add(value);
            }

            String insertQuery = "INSERT INTO " + tableName + " VALUES (";
            for (int i = 0; i < columnCount; i++) {
                insertQuery += "?,";
            }
            insertQuery = insertQuery.substring(0, insertQuery.length() - 1);
            insertQuery += ")";

            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                preparedStatement.setObject(i + 1, value);
            }

            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Запись успешно добавлена в таблицу " + tableName);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при добавлении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateRecord(String tableName, String primaryKeyColumnName) {
        try {
            JTextField id = new JTextField();

            JComponent[] inputs = new JComponent[]{
                    new JLabel("Введите код:"), id
            };

            JOptionPane.showConfirmDialog(null, inputs, "Код", JOptionPane.OK_CANCEL_OPTION);

            int recordId = Integer.parseInt(id.getText());


            DBConnection dbConnection = new DBConnection(url, user, password);
            Connection connection = dbConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE " + primaryKeyColumnName + " = " + recordId);
            resultSet.next();

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Object> values = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                if (i == 1) { // Первичный ключ столбца
                    values.add(resultSet.getObject(columnName));
                } else {
                    String inputValue = JOptionPane.showInputDialog("Введите новое значение для столбца " + columnName);
                    Object value = null;
                    // Перевод введенного значения для ячейки таблицы
                    // (типо если для даты вводить текст то будет ошибка)
                    switch (metaData.getColumnType(i)) {
                        case Types.BIGINT -> value = Long.parseLong(inputValue);
                        case Types.DATE -> value = Date.valueOf(inputValue);
                        case Types.NUMERIC, Types.DOUBLE -> value = Double.parseDouble(inputValue);
                        case Types.VARCHAR -> value = inputValue;
                        default -> {
                            JOptionPane.showMessageDialog(this, "Неизвестный тип данных для столбца " + columnName, "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return; // Выходит если не правильно введены значения
                        }
                    }
                    values.add(value);
                }
            }

            String updateQuery = "UPDATE " + tableName + " SET ";
            for (int i = 2; i <= columnCount; i++) { // Начинается с 2 колонны чтоб пропустить первичный ключ
                String columnName = metaData.getColumnName(i);
                updateQuery += columnName + " = ?, ";
            }
            updateQuery = updateQuery.substring(0, updateQuery.length() - 2);
            updateQuery += " WHERE " + primaryKeyColumnName + " = " + recordId;

            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            for (int i = 2; i <= columnCount; i++) { // Начинается с 2 колонны чтоб пропустить первичный ключ
                Object value = values.get(i - 1);
                preparedStatement.setObject(i - 1, value);
            }

            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Запись успешно обновлена в таблице " + tableName);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при обновлении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord(String tableName) {
        try {
            DBConnection dbConnection = new DBConnection(url, user, password);
            Connection connection = dbConnection.getConnection();
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " LIMIT 0");

            ResultSetMetaData metaData = resultSet.getMetaData();
            metaData.getColumnCount();

            String inputValue = "";
            String deleteQuery = "";

            String columnName = metaData.getColumnName(1); // Получаем название первого столбца

                inputValue = JOptionPane.showInputDialog("Введите значение для столбца " + columnName);

                deleteQuery = "DELETE FROM " + tableName + " WHERE " + columnName + " = ?";

                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setLong(1, Long.parseLong(inputValue));

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Запись успешно удалена из таблицы " + tableName);
                } else {
                    JOptionPane.showMessageDialog(this, "Не удалось найти запись для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }


        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при удалении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void searchRecord(String tableName, String primaryKeyColumnName) throws SQLException {
        JTextField id = new JTextField();

        JComponent[] inputs = new JComponent[]{
                new JLabel("Введите код:"), id
        };

        JOptionPane.showConfirmDialog(null, inputs, "Код", JOptionPane.OK_CANCEL_OPTION);

        int recordId = Integer.parseInt(id.getText());

        DBConnection dbConnection = new DBConnection(url, user, password);
        Connection connection = dbConnection.getConnection();

        Statement statement = connection.createStatement();

        // Выборка записи с заданным первичным ключом
        String query = "SELECT * FROM " + tableName + " WHERE " + primaryKeyColumnName + " = " + recordId;
        ResultSet resultSet = statement.executeQuery(query);

        // Создание таблицы с одной строкой результата
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }
        Object[][] data = new Object[1][columnCount];
        if (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                data[0][i - 1] = resultSet.getObject(i);
            }

            JTable table = new JTable(data, columnNames);

            // Выделение строки в таблице
            table.getSelectionModel().setSelectionInterval(0, 0);

            JScrollPane scrollPane = new JScrollPane(table);

            JOptionPane.showMessageDialog(null, scrollPane, "Результат поиска", JOptionPane.PLAIN_MESSAGE);
        }else {
            JOptionPane.showMessageDialog(null, "По данному коду ничего не найдено", "Результат поиска", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void procedureUpd() {
        String id_sotrudnik_str = JOptionPane.showInputDialog("Введите id сотрудника:");
        if (id_sotrudnik_str == null || id_sotrudnik_str.isEmpty()) {
            return; // отменили ввод или не ввел значение
        }
        int id_sotrudnika = Integer.parseInt(id_sotrudnik_str);
        String surname = JOptionPane.showInputDialog("Введите фамилию:");
        String forename = JOptionPane.showInputDialog("Введите имя:");
        String patronymic = JOptionPane.showInputDialog("Введите отчество:");

        try {
            DBConnection dbConnection = new DBConnection(url, user, password);
            Connection connection = dbConnection.getConnection();

            String sql = "call update_sotrudnik(?, ?, ?, ?)";
            CallableStatement statement = connection.prepareCall(sql);
            statement.setInt(1, id_sotrudnika);
            statement.setString(2, surname);
            statement.setString(3, forename);
            statement.setString(4, patronymic);

            statement.execute();

            JOptionPane.showMessageDialog(this, "Данные сотрудника успешно изменены");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при изменении данных сотрудника: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Данные таблиц
    private String getQueryForTabIndex(int tabIndex) {
        String query = switch (tabIndex) {
            case 0 -> "SELECT * FROM sotrudnik";
            case 1 -> "SELECT * FROM rabota";
            case 2 -> "SELECT * FROM vid_raboti";
            case 3 -> "SELECT * FROM prepodavatel";
            case 4 -> "SELECT * FROM vspomog_personal";
            default -> throw new IllegalArgumentException("Не правильный табличный индекс: " + tabIndex);
        };
        return query;
    }

    //Обновление таблицы (Показывает уже измененную таблицу)
    private void refreshTable() {
        int tabIndex = tabbedPane.getSelectedIndex();
        JPanel panel = (JPanel) tabbedPane.getComponentAt(tabIndex);
        String query = getQueryForTabIndex(tabIndex);

        //Удаляем предыдущую таблицу из панели
        for (Component component : panel.getComponents()) {
            if (component instanceof JScrollPane) {
                panel.remove(component);
                break;
            }
        }
        //Добавляем обновленную таблицу
        addTableToPanel(panel, query);
    }
}
