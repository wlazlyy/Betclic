import java.sql.*;
import java.util.Scanner;

public class Betclic {
    // Stałe do konfiguracji połączenia z bazą danych
    private static final String DB_URL = "jdbc:mysql://localhost:3306/betclic";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static Connection connection = null; // Zmienna do przechowywania połączenia z bazą danych

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Obiekt Scanner do odczytu danych wejściowych od użytkownika
        try {
            connectToDatabase();

            while (true) { // Pętla głównego menu aplikacji
                System.out.println("\nWitamy w symulacji Betclic");
                System.out.println("1. Panel użytkownika");
                System.out.println("2. Panel administratora");
                System.out.println("3. Wyjście");
                System.out.print("Wybierz opcję: ");

                int choice;
                try {
                    choice = scanner.nextInt(); // Pobranie wyboru od użytkownika
                } catch (Exception e) {
                    System.out.println("Nieprawidłowy wybór. Wprowadź liczbę.");
                    scanner.nextLine();
                    continue;
                }

                switch (choice) {  // Obsługa wyboru użytkownika
                    case 1:
                        userPanel(scanner);
                        break;
                    case 2:
                        adminPanel(scanner);
                        break;
                    case 3:
                        System.out.println("Zakończenie programu...");
                        return;
                    default:
                        System.out.println("Nieprawidłowa opcja. Spróbuj ponownie.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd bazy danych: " + e.getMessage());  // Obsługa błędów bazy danych
        } finally {
            closeConnection(); // Zamknięcie połączenia z bazą danych
        }
    }
    // Funkcja do nawiązania połączenia z bazą danych
    private static void connectToDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("Połączono z bazą danych.");
    }
    // Funkcja do zamykania połączenia z bazą danych
    private static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Połączenie z bazą danych zostało zamknięte.");
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas zamykania połączenia: " + e.getMessage());
        }
    }
    // Panel użytkownika - obsługa opcji dla użytkownika
    private static void userPanel(Scanner scanner) {
        try {
            System.out.print("Podaj swój ID użytkownika: ");
            int userId = scanner.nextInt(); // Pobranie ID użytkownika

            while (true) { // Pętla opcji panelu użytkownika
                System.out.println("\nPanel użytkownika");
                System.out.println("1. Wyświetl szczegóły konta");
                System.out.println("2. Doładuj saldo");
                System.out.println("3. Wyświetl dostępne zakłady");
                System.out.println("4. Postaw zakład");
                System.out.println("5. Powrót do głównego menu");
                System.out.print("Wybierz opcję: ");

                int choice = scanner.nextInt(); // Pobranie wyboru od użytkownika
                switch (choice) {
                    case 1:
                        viewUserDetails(userId);
                        break;
                    case 2:
                        rechargeBalance(userId, scanner);
                        break;
                    case 3:
                        viewBets();
                        break;
                    case 4:
                        placeBet(scanner, userId);
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Nieprawidłowa opcja. Spróbuj ponownie.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd w panelu użytkownika: " + e.getMessage());
        }
    }
    // Panel administratora - obsługa opcji dla administratora
    private static void adminPanel(Scanner scanner) {
        try {
            while (true) { // Pętla opcji panelu administratora
                System.out.println("\nPanel administratora");
                System.out.println("1. Wyświetl wszystkie zakłady");
                System.out.println("2. Dodaj nowy zakład");
                System.out.println("3. Usuń zakład");
                System.out.println("4. Zamknij zakład");
                System.out.println("5. Powrót do głównego menu");
                System.out.print("Wybierz opcję: ");

                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        viewAllBets();
                        break;
                    case 2:
                        addBet(scanner);
                        break;
                    case 3:
                        deleteBet(scanner);
                        break;
                    case 4:
                        closeBet(scanner);
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Nieprawidłowa opcja. Spróbuj ponownie.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd w panelu administratora: " + e.getMessage());
        }
    }
    // Funkcja do wyświetlania szczegółów konta użytkownika
    private static void viewUserDetails(int userId) throws SQLException {
        String query = "SELECT * FROM Users WHERE id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                System.out.printf("\nID: %d | Nazwa: %s | Saldo: %.2f\n",
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("balance"));
            } else {
                System.out.println("Nie znaleziono użytkownika.");
            }
        }
    }
    // Funkcja do doładowania salda użytkownika
    private static void rechargeBalance(int userId, Scanner scanner) throws SQLException {
        System.out.print("Podaj kwotę doładowania: ");
        double amount = scanner.nextDouble();

        if (amount <= 0) { // Sprawdzenie poprawności kwoty
            System.out.println("Kwota musi być większa niż 0.");
            return;
        }

        String updateQuery = "UPDATE Users SET balance = balance + ? WHERE id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setDouble(1, amount);
            statement.setInt(2, userId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Saldo zostało pomyślnie doładowane.");
            } else {
                System.out.println("Nie znaleziono użytkownika.");
            }
        }
    }
    // Wyświetlenie wszystkich zakładów w bazie danych
    private static void viewAllBets() throws SQLException {
        String query = "SELECT * FROM Bets;";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("\nWszystkie zakłady:");
            // Iteracja po wynikach zapytania i wyświetlanie informacji o zakładach
            while (resultSet.next()) {
                System.out.printf("ID: %d | Wydarzenie: %s | Kurs: %.2f | Status: %s\n",
                        resultSet.getInt("id"),
                        resultSet.getString("event"),
                        resultSet.getDouble("odds"),
                        resultSet.getString("status"));
            }
        }
    }
    // Usunięcie zakładu o podanym ID
    private static void deleteBet(Scanner scanner) throws SQLException {
        System.out.print("Podaj ID zakładu do usunięcia: ");
        int betId = scanner.nextInt();

        String deleteQuery = "DELETE FROM Bets WHERE id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, betId);
            int rowsAffected = statement.executeUpdate();
// Sprawdzenie, czy zakład został usunięty
            if (rowsAffected > 0) {
                System.out.println("Zakład został usunięty.");
            } else {
                System.out.println("Nie znaleziono zakładu o podanym ID.");
            }
        }
    }
    // Zamknięcie zakładu poprzez zmianę jego statusu na 'closed'
    private static void closeBet(Scanner scanner) throws SQLException {
        System.out.print("Podaj ID zakładu do zamknięcia: ");
        int betId = scanner.nextInt();

        String updateQuery = "UPDATE Bets SET status = 'closed' WHERE id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, betId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Zakład został zamknięty.");
            } else {
                System.out.println("Nie znaleziono zakładu o podanym ID.");
            }
        }
    }
    // Wyświetlenie wszystkich dostępnych zakładów (status = 'open')
    private static void viewBets() throws SQLException {
        String query = "SELECT * FROM Bets WHERE status = 'open';";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("\nDostępne zakłady:");
            while (resultSet.next()) {
                System.out.printf("ID: %d | Wydarzenie: %s | Kurs: %.2f\n",
                        resultSet.getInt("id"),
                        resultSet.getString("event"),
                        resultSet.getDouble("odds"));
            }
        }
    }
    // Postawienie zakładu przez użytkownika
    private static void placeBet(Scanner scanner, int userId) {
        try {
            System.out.print("Podaj ID zakładu: ");
            int betId = scanner.nextInt();

            System.out.print("Podaj kwotę zakładu: ");
            double amount = scanner.nextDouble();

            if (amount <= 0) {
                System.out.println("Kwota zakładu musi być większa niż 0.");
                return;
            }

            String balanceQuery = "SELECT balance FROM Users WHERE id = ?;";
            try (PreparedStatement checkBalance = connection.prepareStatement(balanceQuery)) {
                checkBalance.setInt(1, userId);
                ResultSet balanceResult = checkBalance.executeQuery();

                if (balanceResult.next()) {
                    double balance = balanceResult.getDouble("balance");
                    // Sprawdzenie, czy użytkownik ma wystarczające środki na koncie
                    if (balance >= amount) {
                        String updateQuery = "UPDATE Users SET balance = balance - ? WHERE id = ?;";
                        try (PreparedStatement updateBalance = connection.prepareStatement(updateQuery)) {
                            updateBalance.setDouble(1, amount);
                            updateBalance.setInt(2, userId);
                            updateBalance.executeUpdate();
                            System.out.println("Zakład został pomyślnie postawiony!");
                        }
                    } else {
                        System.out.println("Niewystarczające środki na koncie.");
                    }
                } else {
                    System.out.println("Nie znaleziono użytkownika.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas stawiania zakładu: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Nieprawidłowe dane wejściowe. Spróbuj ponownie.");
            scanner.nextLine();
        }
    }
    // Dodanie nowego zakładu
    private static void addBet(Scanner scanner) {
        try {
            System.out.print("Podaj nazwę wydarzenia: ");
            scanner.nextLine();
            String event = scanner.nextLine();

            System.out.print("Podaj kurs wydarzenia: ");
            double odds = scanner.nextDouble();

            if (odds <= 0) {
                System.out.println("Kurs musi być większy niż 0.");
                return;
            }
            // Dodanie nowego zakładu do bazy danych
            String insertQuery = "INSERT INTO Bets (event, odds, status) VALUES (?, ?, 'open');";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, event);
                statement.setDouble(2, odds);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Nowy zakład został dodany pomyślnie.");
                } else {
                    System.out.println("Nie udało się dodać zakładu.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania zakładu: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Nieprawidłowe dane wejściowe. Spróbuj ponownie.");
            scanner.nextLine();
        }
    }
}
