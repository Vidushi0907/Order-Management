
import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static Scanner sc = new Scanner(System.in);

    // Database connection details
    static String url =
            "jdbc:mysql://localhost:3306/ecommerce_db";

    static String username = "root";
    static String password = "Vidushi_09#";


    // A) Insert at least three orders
    static void insertOrders(Connection con) throws SQLException {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        int[] ids = {101, 102, 103};
        String[] names = {"Rahul", "Aman", "Priya"};
        String[] products = {"Laptop", "Mobile", "Mouse"};

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < 3; i++) {

                try {
                    ps.setInt(1, ids[i]);
                    ps.setString(2, names[i]);
                    ps.setString(3, products[i]);
                    ps.setInt(4, 3);
                    ps.setDouble(5, 50000);
                    ps.setString(6, "Pending");

                    ps.executeUpdate();

                    System.out.println("Order inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {

                    System.out.println("Duplicate Order ID: " + ids[i]);
                }
            }
        }
    }


    // B) Display orders by status and quantity > 2
    static void displayOrders(Connection con) throws SQLException {

        System.out.print("Enter order status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders " +
                "WHERE status = ? AND quantity > 2 " +
                "ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    System.out.println(
                            rs.getInt("order_id") + " " +
                                    rs.getString("customer_name") + " " +
                                    rs.getString("product_name") + " " +
                                    rs.getInt("quantity") + " " +
                                    rs.getDouble("unit_price") + " " +
                                    rs.getString("status")
                    );
                }

                if (!found) {
                    System.out.println("No matching orders found");
                }
            }
        }
    }


    // C) Increase quantity of an existing order
    static void increaseQuantity(Connection con) throws SQLException {

        System.out.print("Enter order ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter additional quantity: ");
        int qty = Integer.parseInt(sc.nextLine());

        if (qty <= 0) {
            System.out.println("Quantity must be positive");
            return;
        }

        String sql = "UPDATE Orders SET quantity = quantity + ? " +
                "WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, qty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Quantity updated successfully!");
            } else {
                System.out.println("Order does not exist");
            }
        }
    }


    // D) Update order status after validation
    static void updateStatus(Connection con) throws SQLException {

        System.out.print("Enter order ID: ");
        int id = Integer.parseInt(sc.nextLine());

        String selectSql = "SELECT * FROM Orders WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(selectSql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    System.out.println("Order does not exist");
                    return;
                }

                System.out.println("Current Order Details:");

                System.out.println(
                        rs.getInt("order_id") + " " +
                                rs.getString("customer_name") + " " +
                                rs.getString("product_name") + " " +
                                rs.getInt("quantity") + " " +
                                rs.getDouble("unit_price") + " " +
                                rs.getString("status")
                );
            }
        }

        System.out.print("Enter new status: ");
        String newStatus = sc.nextLine();

        if (!(newStatus.equalsIgnoreCase("Pending") ||
                newStatus.equalsIgnoreCase("Shipped") ||
                newStatus.equalsIgnoreCase("Delivered"))) {

            System.out.println("Invalid status!");
            return;
        }

        String updateSql =
                "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(updateSql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, id);

            ps.executeUpdate();

            System.out.println("Status updated successfully!");
        }

        // Display updated order details
        String showSql = "SELECT * FROM Orders WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(showSql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    System.out.println("Updated Order Details:");

                    System.out.println(
                            rs.getInt("order_id") + " " +
                                    rs.getString("customer_name") + " " +
                                    rs.getString("product_name") + " " +
                                    rs.getInt("quantity") + " " +
                                    rs.getDouble("unit_price") + " " +
                                    rs.getString("status")
                    );
                }
            }
        }
    }


    // E) Display total value and delete cancelled order
    static void totalValueAndDelete(Connection con) throws SQLException {

        String sumSql =
                "SELECT SUM(quantity * unit_price) AS total FROM Orders";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sumSql)) {

            if (rs.next()) {
                System.out.println(
                        "Total Order Value: " + rs.getDouble("total")
                );
            }
        }

        System.out.print("Enter order ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String selectSql =
                "SELECT status FROM Orders WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(selectSql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    System.out.println("Order does not exist");
                    return;
                }

                String status = rs.getString("status");

                if (!status.equalsIgnoreCase("Cancelled")) {

                    System.out.println(
                            "Order cannot be deleted. Status is not Cancelled."
                    );

                    return;
                }
            }
        }

        String deleteSql = "DELETE FROM Orders WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(deleteSql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Order deleted successfully!");
            } else {
                System.out.println("Order could not be deleted");
            }
        }
    }


    // Main method - Menu driven application
    public static void main(String[] args) {

        try (Connection con =
                     DriverManager.getConnection(url, username, password)) {

            System.out.println("Database Connected Successfully!");

            while (true) {

                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");

                System.out.println("1. Insert Three Orders");
                System.out.println("2. Display Orders by Status");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Total Order Value and Delete Order");
                System.out.println("6. Exit");

                System.out.print("Enter your choice: ");

                int choice;

                try {
                    choice = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Enter a valid menu number");
                    continue;
                }

                switch (choice) {

                    case 1:
                        insertOrders(con);
                        break;

                    case 2:
                        displayOrders(con);
                        break;

                    case 3:
                        increaseQuantity(con);
                        break;

                    case 4:
                        updateStatus(con);
                        break;

                    case 5:
                        totalValueAndDelete(con);
                        break;

                    case 6:
                        System.out.println("Thank you!");
                        return;

                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (SQLException e) {

            System.out.println("Database Error: " + e.getMessage());
        }
    }
}

