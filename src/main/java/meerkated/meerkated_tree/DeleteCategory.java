package meerkated.meerkated_tree;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import meerkated.meerkated_tree.entities.Category;

import java.util.Scanner;

public class DeleteCategory {
    
    public static void main(String[] args) {
    
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        Scanner scanner = new Scanner(System.in);
    
        System.out.print("Введите id удаляемой категории: ");
        String categoryIdIn = scanner.nextLine();
    
        Category parentCategory  = null;
        parentCategory = manager.find(Category.class, Long.parseLong(categoryIdIn));
    
        Query categoryQueryDeleteSubs = manager.createQuery(
            "delete Category c where c.leftKey >= ?1 and c.rightKey <= ?2"
        );
        
        Query categoryUpdateLeftKey = manager.createQuery(
            "update Category c set c.leftKey = c.leftKey - (?2 - ?1 + 1) where c.leftKey > ?1"
        );
    
        Query categoryUpdateRightKey = manager.createQuery(
            "update Category c set c.rightKey = c.rightKey - (?1 - ?2 + 1) where c.rightKey > ?1"
        );
        if (parentCategory != null) {
            try {
                manager.getTransaction().begin();
    
                Long leftKey = parentCategory.getLeftKey();
                Long rightKey = parentCategory.getRightKey();
                
                categoryQueryDeleteSubs.setParameter(1, leftKey);
                categoryQueryDeleteSubs.setParameter(2, rightKey);
                
                categoryQueryDeleteSubs.executeUpdate();
                
                categoryUpdateLeftKey.setParameter(1, leftKey);
                categoryUpdateLeftKey.setParameter(2, rightKey);
                categoryUpdateRightKey.setParameter(1, rightKey);
                categoryUpdateRightKey.setParameter(2, leftKey);
    
                categoryUpdateLeftKey.executeUpdate();
                categoryUpdateRightKey.executeUpdate();
                
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
