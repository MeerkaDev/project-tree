package meerkated.meerkated_tree;

import jakarta.persistence.*;
import meerkated.meerkated_tree.entities.Category;

import java.util.List;
import java.util.Scanner;

public class TreeEditorDB {
    
    // Создание категории
    public static void createCategory(Scanner scanner, EntityManagerFactory factory) {
        EntityManager manager = factory.createEntityManager();
    
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
        
        manager.close();
    }
    
    // Удаление категории
    public static void deleteCategory(Scanner scanner, EntityManagerFactory factory) {
        EntityManager manager = factory.createEntityManager();
    
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
        
        manager.close();
    }
    
    // Перемещение категории
    public static void moveCategory(Scanner scanner, EntityManagerFactory factory) {
        EntityManager manager = factory.createEntityManager();
        
        System.out.print("Введите id перемещаемой категории: ");
        String categoryMovesIdIn = scanner.nextLine();
        
        System.out.print("Введите id категории, в которую будет произведено перемещение: ");
        String categoryDestIdIn = scanner.nextLine();
        
        Category movingCategory = null;
        movingCategory = manager.find(Category.class, Long.parseLong(categoryMovesIdIn));
        
        Category destCategory = null;
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
        
        manager.close();
    }
    
    // Поиск категории и ее подкатегорий
    public static void findCategory(Scanner scanner, EntityManagerFactory factory) {
        EntityManager manager = factory.createEntityManager();
        
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
        
        manager.close();
    }
}
