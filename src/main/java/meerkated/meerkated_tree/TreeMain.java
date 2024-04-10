package meerkated.meerkated_tree;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Scanner;

public class TreeMain {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        Scanner scanner = new Scanner(System.in);
    
        while (true) {
            System.out.println("""
                
                Чтобы выполнить действия введите числа соответствующие командам:
                - Найти категорию [1]
                - Создать категорию [2]
                - Удалить категорию [3]
                - Переместить категорию [4]
                - Закрыть приложение [0]
                """);
        
            String command = scanner.nextLine();
        
            if (!command.isEmpty()) {
                try {
                    switch (Integer.parseInt(command)) {
                        case 1:
                            TreeEditorDB.findCategory(scanner, factory);
                            break;
                        case 2:
                            TreeEditorDB.createCategory(scanner, factory);
                            break;
                        case 3:
                            TreeEditorDB.deleteCategory(scanner, factory);
                            break;
                        case 4:
                            TreeEditorDB.moveCategory(scanner, factory);
                            break;
                        case 0:
                            scanner.close();
                            factory.close();
                            break;
                    }
                
                } catch (NumberFormatException e) {
                    System.out.println("Неверный формат ввода числа! Попробуйте снова.");
                }
            }
        }
    }
}
