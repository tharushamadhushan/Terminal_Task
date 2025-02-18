package com.example.inventory_management;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class InventoryManagementApplication {
	private static final String FILE_NAME = "inventory.txt";
	private static final Map<String, Item> inventory = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		loadInventoryFromFile();
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("\n--- Inventory Management System ---");
			System.out.println("1. Add New Item");
			System.out.println("2. View Inventory");
			System.out.println("3. Search Item by Name or ID");
			System.out.println("4. Update Item");
			System.out.println("5. Delete Item");
			System.out.println("6. Export Inventory to File");
			System.out.println("7. Exit");
			System.out.print("Choose an option: ");

			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
				case 1 -> addItem(scanner);
				case 2 -> viewInventory();
				case 3 -> searchItem(scanner);
				case 4 -> updateItem(scanner);
				case 5 -> deleteItem(scanner);
				case 6 -> exportInventoryToFile();
				case 7 -> {
					System.out.println("Exiting... Goodbye!");
					return;
				}
				default -> System.out.println("Invalid choice. Please try again.");
			}
		}
	}

	private static void addItem(Scanner scanner) {
		System.out.print("Enter Item ID: ");
		String id = scanner.nextLine();
		System.out.print("Enter Item Name: ");
		String name = scanner.nextLine();
		System.out.print("Enter Quantity: ");
		int quantity = scanner.nextInt();
		System.out.print("Enter Price: ");
		double price = scanner.nextDouble();
		scanner.nextLine();

		if (inventory.containsKey(id)) {
			System.out.println("Item with this ID already exists.");
			return;
		}

		inventory.put(id, new Item(id, name, quantity, price));
		System.out.println("Item added successfully!");
	}

	private static void viewInventory() {
		if (inventory.isEmpty()) {
			System.out.println("Inventory is empty.");
			return;
		}

		System.out.println("\n--- Inventory ---");
		for (Item item : inventory.values()) {
			System.out.println(item);
		}
	}

	private static void searchItem(Scanner scanner) {
		System.out.print("Enter Item Name or ID to search: ");
		String query = scanner.nextLine();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			boolean found = false;
			for (Item item : inventory.values()) {
				if (item.getId().equalsIgnoreCase(query) || item.getName().equalsIgnoreCase(query)) {
					System.out.println("Item Found: " + item);
					found = true;
				}
			}
			if (!found) {
				System.out.println("Item not found.");
			}
		});
		executor.shutdown();
	}

	private static void updateItem(Scanner scanner) {
		System.out.print("Enter Item ID to update: ");
		String id = scanner.nextLine();

		if (!inventory.containsKey(id)) {
			System.out.println("Item not found.");
			return;
		}

		boolean updating = true;

		while (updating) {
			System.out.println("\nSelect an option:");
			System.out.println("1. Update Quantity");
			System.out.println("2. Update Price");
			System.out.println("3. Update Both Quantity and Price");
			System.out.println("4. Exit Update");

			System.out.print("Enter your choice: ");
			int choice = scanner.nextInt();
			scanner.nextLine(); // Consume newline

			Item item = inventory.get(id);

			switch (choice) {
				case 1:
					// Update quantity
					System.out.print("Enter new Quantity: ");
					int quantity = scanner.nextInt();
					scanner.nextLine(); // Consume newline
					item.setQuantity(quantity);
					System.out.println("Quantity updated successfully!");
					break;

				case 2:
					// Update price
					System.out.print("Enter new Price: ");
					double price = scanner.nextDouble();
					scanner.nextLine(); // Consume newline
					item.setPrice(price);
					System.out.println("Price updated successfully!");
					break;

				case 3:
					// Update both quantity and price
					System.out.print("Enter new Quantity: ");
					quantity = scanner.nextInt();
					System.out.print("Enter new Price: ");
					price = scanner.nextDouble();
					scanner.nextLine(); // Consume newline
					item.setQuantity(quantity);
					item.setPrice(price);
					System.out.println("Quantity and Price updated successfully!");
					break;

				case 4:
					// Exit update
					System.out.println("Exiting update...");
					updating = false;
					break;

				default:
					System.out.println("Invalid choice. Please try again.");
			}
		}
	}


	private static void deleteItem(Scanner scanner) {
		System.out.print("Enter Item ID to delete: ");
		String id = scanner.nextLine();

		if (inventory.remove(id) != null) {
			System.out.println("Item deleted successfully!");
		} else {
			System.out.println("Item not found.");
		}
	}

	private static void exportInventoryToFile() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
			for (Item item : inventory.values()) {
				writer.write(item.toFileString());
				writer.newLine();
			}
			System.out.println("Inventory exported successfully to " + FILE_NAME);
		} catch (IOException e) {
			System.out.println("Error exporting inventory: " + e.getMessage());
		}
	}

	private static void loadInventoryFromFile() {
		try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(", ");
				if (parts.length == 4) {
					String id = parts[0];
					String name = parts[1];
					int quantity = Integer.parseInt(parts[2]);
					double price = Double.parseDouble(parts[3]);
					inventory.put(id, new Item(id, name, quantity, price));
				}
			}
		} catch (IOException e) {
			System.out.println("No previous inventory file found. Starting fresh.");
		}
	}
}

class Item {
	private final String id;
	private final String name;
	private int quantity;
	private double price;

	public Item(String id, String name, int quantity, double price) {
		this.id = id;
		this.name = name;
		this.quantity = quantity;
		this.price = price;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return String.format("ID: %s, Name: %s, Quantity: %d, Price: %.2f", id, name, quantity, price);
	}

	public String toFileString() {
		return String.format("%s,%s,%d,%.2f", id, name, quantity, price);
	}

}
