package meerkated.meerkated_tree;

import jakarta.persistence.*;
import meerkated.meerkated_tree.entities.Category;

import java.util.Scanner;

public class CreateCategory {
    
    public static void main(String[] args) {
        // Введите id родительской категории: 2
        // Введите название новой категории: МЦСТ
    
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        Scanner scanner = new Scanner(System.in);
    
        System.out.print("Введите id родительской категории (0 - для отсутствия родительской): ");
        String categoryIdIn = scanner.nextLine();
        
        
        if (Long.parseLong(categoryIdIn) != 0) {
            Query categoryQueryLeftIncrease = manager.createQuery(
                "update Category c set c.leftKey = c.leftKey + 2 " +
                    "where c.leftKey > ?1"
            );
    
            Query categoryQueryRightIncrease = manager.createQuery(
                "update Category c set c.rightKey = c.rightKey + 2 " +
                    "where c.rightKey >= ?1"
            );
            
            Category parentCategory  = null;
            parentCategory = manager.find(Category.class, Long.parseLong(categoryIdIn));
    
            if (parentCategory != null) {
                System.out.print("Введите название новой категории: ");
                String createdCategoryName = scanner.nextLine();
        
                categoryQueryLeftIncrease.setParameter(1, parentCategory.getRightKey());
                categoryQueryRightIncrease.setParameter(1, parentCategory.getRightKey());
        
                Category createdCategory = new Category();
                createdCategory.setName(createdCategoryName);
                createdCategory.setLeftKey(parentCategory.getRightKey());
                createdCategory.setRightKey(parentCategory.getRightKey() + 1);
                createdCategory.setInsertionLevel(parentCategory.getInsertionLevel() + 1);
        
                try {
                    manager.getTransaction().begin();
            
                    categoryQueryLeftIncrease.executeUpdate();
                    categoryQueryRightIncrease.executeUpdate();
            
                    manager.persist(createdCategory);
            
                    manager.getTransaction().commit();
                } catch (Exception e) {
                    manager.getTransaction().rollback();
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Родительской категории с таким id нет в базе!");
            }
        } else {
            System.out.print("Введите название новой категории: ");
            String createdCategoryName = scanner.nextLine();
            
            TypedQuery<Long> maxRightKeyQuery = manager.createQuery(
                "select max(c.rightKey) from Category c", Long.class
            );
    
            Long maxRightKey = maxRightKeyQuery.getSingleResult();
    
            Category createdCategory = new Category();
            createdCategory.setName(createdCategoryName);
            createdCategory.setLeftKey(maxRightKey + 1);
            createdCategory.setRightKey(maxRightKey + 2);
            createdCategory.setInsertionLevel(0L);
    
            try {
                manager.getTransaction().begin();
                
                manager.persist(createdCategory);
        
                manager.getTransaction().commit();
            } catch (Exception e) {
                manager.getTransaction().rollback();
                throw new RuntimeException(e);
            }
        }
        
        
        scanner.close();
        manager.close();
        factory.close();
    }
}
