package meerkated.meerkated_tree;

import jakarta.persistence.*;
import meerkated.meerkated_tree.entities.Category;

import java.util.Scanner;

public class MoveCategory {
    
    public static void main(String[] args) {
        // id перемещаемой
        // id новой родительской
        
        // 1) Все ключи перемещаемой категории сделать отрицательными.
        // 2) Убрать образовавшийся промежуток.
        // 3) Выделить место в новой родительской категории.
        // 4) Преобразовать отрицательные ключи перемещаемой категории в
        // положительные по формуле.
        
        // -2 -7 -> 6 17 -> 11 16
        // -3 -4            12 13
        // -5 -6            14 15
        
        // 0 - <актуальный ключ> + (<правый ключ нового родителя> - <правый ключ основной перемещаемой> - 1)
        
        // 0 - (-2) + (17 - 7 - 1) = 11
        // 0 - (-6) + (17 - 7 - 1) = 15
        
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Введите id перемещаемой категории: ");
        String categoryMovesIdIn = scanner.nextLine();
        
        System.out.print("Введите id категории, в которую будет произведено перемещение: ");
        String categoryDestIdIn = scanner.nextLine();
        
        Category movingCategory  = null;
        movingCategory = manager.find(Category.class, Long.parseLong(categoryMovesIdIn));
    
        Category destCategory  = null;
        destCategory = manager.find(Category.class, Long.parseLong(categoryDestIdIn));
    
        // Запрос для преобразования левого и правого ключей перемещаемой
        // категории в отрицательные.
        Query categoryUpdateKeyMoving = manager.createQuery(
            "update Category c set c.leftKey = -c.leftKey, c.rightKey = -c.rightKey where c.leftKey >= ?1 and c.rightKey <= ?2"
        );
        
        // Запросы для заполнения промежутка ключей.
        Query categoryUpdateLeftKeyDecrease = manager.createQuery(
            "update Category c set c.leftKey = c.leftKey - (?2 - ?1 + 1) where c.leftKey > ?1"
        );
    
        Query categoryUpdateRightKeyDecrease = manager.createQuery(
            "update Category c set c.rightKey = c.rightKey - (?1 - ?2 + 1) where c.rightKey > ?1"
        );
        
        // Запросы для выделения места в новой родительской категории
        Query categoryUpdateLeftKeyIncrease = manager.createQuery(
            "update Category c set c.leftKey = c.leftKey + (?2 - ?1 + 1) where c.leftKey > ?3"
        );
    
        Query categoryUpdateRightKeyIncrease = manager.createQuery(
            "update Category c set c.rightKey = c.rightKey + (?1 - ?2 + 1) where c.rightKey >= ?3"
        );
        
        // Запрос для конечного преобразования ключей перемещаемой категории
        
        Query categoryMovingFinish = manager.createQuery(
            "update Category c set c.leftKey = 0 - c.leftKey + ?1, c.rightKey = 0 - c.rightKey + ?1," +
                "c.insertionLevel = " +
                "c.insertionLevel - ?4 + ?5 " +
                "where c.leftKey <= ?2 and c.rightKey >= ?3"
        );
    
        // Запрос для конечного преобразования ключей перемещаемой категории на нулевой уровень
        
        Query categoryMovingFinishOut = manager.createQuery(
            "update Category c set c.leftKey = 0 - c.leftKey + ?1, c.rightKey = 0 - c.rightKey + ?1," +
                "c.insertionLevel = c.insertionLevel - ?4 " +
                "where c.leftKey <= ?2 and c.rightKey >= ?3"
        );
        
        // -5 -> 24 -> 25
        // -6          26
        
        // 0 - (-5) - 5 + 24 + 1
        // 0 - (-6) - 5 + 24 + 1
        // 0 - (-2) + (20 - 2 + 1)
        
        if (movingCategory != null) {
            try {
    
                if (destCategory != null &&
                    destCategory.getLeftKey() >= movingCategory.getLeftKey() &&
                    destCategory.getRightKey() <= movingCategory.getRightKey()) {
                    
                    throw new RuntimeException("Невозможно переместить категорию в саму себя или в дочерние категории!");
                }
                
                manager.getTransaction().begin();
    
                Long leftKeyMoving = movingCategory.getLeftKey();
                Long rightKeyMoving = movingCategory.getRightKey();
    
                // Запрос для преобразования левого и правого ключей перемещаемой
                // категории в отрицательные.
                categoryUpdateKeyMoving.setParameter(1, leftKeyMoving);
                categoryUpdateKeyMoving.setParameter(2, rightKeyMoving);

                categoryUpdateKeyMoving.executeUpdate();
                
                // Запросы для заполнения промежутка ключей.
                categoryUpdateLeftKeyDecrease.setParameter(1, leftKeyMoving);
                categoryUpdateLeftKeyDecrease.setParameter(2, rightKeyMoving);
                categoryUpdateRightKeyDecrease.setParameter(1, rightKeyMoving);
                categoryUpdateRightKeyDecrease.setParameter(2, leftKeyMoving);

                categoryUpdateLeftKeyDecrease.executeUpdate();
                categoryUpdateRightKeyDecrease.executeUpdate();
                
                if (Long.parseLong(categoryDestIdIn) != 0) {
    
                    manager.refresh(destCategory);
                    
                    // Запрос для выделения места в новой родительской категории
                    categoryUpdateLeftKeyIncrease.setParameter(1, leftKeyMoving);
                    categoryUpdateLeftKeyIncrease.setParameter(2, rightKeyMoving);
                    categoryUpdateLeftKeyIncrease.setParameter(3, destCategory.getRightKey());
                    categoryUpdateRightKeyIncrease.setParameter(1, rightKeyMoving);
                    categoryUpdateRightKeyIncrease.setParameter(2, leftKeyMoving);
                    categoryUpdateRightKeyIncrease.setParameter(3, destCategory.getRightKey());
    
                    categoryUpdateLeftKeyIncrease.executeUpdate();
                    categoryUpdateRightKeyIncrease.executeUpdate();
    
                    manager.refresh(destCategory);
    
                    // Запрос для конечного преобразования ключей перемещаемой категории
    
                    Long formulaPart = destCategory.getRightKey() - movingCategory.getRightKey() - 1;
    
                    Long oldInsLevel = movingCategory.getInsertionLevel();
                    Long newInsLevel = destCategory.getInsertionLevel() + 1;
    
                    manager.refresh(movingCategory);
    
                    categoryMovingFinish.setParameter(1, formulaPart);
                    categoryMovingFinish.setParameter(2, movingCategory.getLeftKey());
                    categoryMovingFinish.setParameter(3, movingCategory.getRightKey());
                    categoryMovingFinish.setParameter(4, oldInsLevel);
                    categoryMovingFinish.setParameter(5, newInsLevel);
    
                    categoryMovingFinish.executeUpdate();
                } else {
                    TypedQuery<Long> maxRightKeyQuery = manager.createQuery(
                        "select max(c.rightKey) from Category c", Long.class
                    );
    
                    Long formulaPart = maxRightKeyQuery.getSingleResult() - movingCategory.getLeftKey() + 1;
    
                    Long oldInsLevel = movingCategory.getInsertionLevel();
                    
                    manager.refresh(movingCategory);
                    
                    categoryMovingFinishOut.setParameter(1, formulaPart);
                    categoryMovingFinishOut.setParameter(2, movingCategory.getLeftKey());
                    categoryMovingFinishOut.setParameter(3, movingCategory.getRightKey());
                    categoryMovingFinishOut.setParameter(4, oldInsLevel);
    
                    categoryMovingFinishOut.executeUpdate();
                }
                
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
