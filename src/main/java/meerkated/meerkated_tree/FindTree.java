package meerkated.meerkated_tree;

import jakarta.persistence.*;
import meerkated.meerkated_tree.entities.Category;

import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;

public class FindTree {
    
    public static void main(String[] args) {
        // Введите название категории: Процессоры
        
        // Процессоры
        // - Intel
        // - AMD
        
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Введите название категории: ");
        String categoryNameIn = scanner.nextLine();
        
        TypedQuery<Category> categoryTypedQuery = manager.createQuery(
            "select c from Category c where c.name = ?1", Category.class
        );
        categoryTypedQuery.setParameter(1, categoryNameIn);
        
        Category foundCategory = null;
        
        try {
            foundCategory = categoryTypedQuery.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Категории с введенным названием не найдено!");
        }
        
        TypedQuery<Category> chCategoriesTypedQuery = manager.createQuery(
            "select c from Category c where c.leftKey > ?1 and c.rightKey < ?2", Category.class
        );
        
        if (foundCategory != null) {
            chCategoriesTypedQuery.setParameter(1, foundCategory.getLeftKey());
            chCategoriesTypedQuery.setParameter(2, foundCategory.getRightKey());
            
            List<Category> chCategories = chCategoriesTypedQuery.getResultList();
            
            System.out.println(foundCategory.getName());
    
            Long insertionLevel = 0L;
            for (Category c : chCategories) {
                insertionLevel = c.getInsertionLevel();
        
                while (insertionLevel != 0) {
                    System.out.print("- ");
                    insertionLevel--;
                }
        
                System.out.println(c.getName());
            }
        }
        
        scanner.close();
        manager.close();
        factory.close();
    }
}
